package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.codec.MinecraftCodec;
import com.github.steveice10.mc.protocol.data.game.chunk.ChunkSection;
import com.github.steveice10.mc.protocol.data.game.level.LightUpdateData;
import com.github.steveice10.mc.protocol.data.game.level.block.BlockEntityInfo;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.LongArrayTag;
import com.nukkitx.nbt.NBTInputStream;
import com.nukkitx.nbt.NbtMap;
import com.nukkitx.nbt.NbtMapBuilder;
import com.nukkitx.nbt.util.stream.NetworkDataInputStream;
import com.nukkitx.network.VarInts;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import org.barrelmc.barrel.network.converter.BlockConverter;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.utils.Utils;
import org.barrelmc.barrel.utils.nukkit.BitArray;
import org.barrelmc.barrel.utils.nukkit.BitArrayVersion;

import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;

public class LevelChunkPacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.LevelChunkPacket packet = (com.nukkitx.protocol.bedrock.packet.LevelChunkPacket) pk;

        ChunkSection[] chunkSections = new ChunkSection[16];

        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(packet.getData());

        int subChunksLength = packet.getSubChunksLength();

        if (subChunksLength > 15) {
            subChunksLength = 15;
        }

        ByteBuf chunkByteBuf = Unpooled.buffer();
        for (int sectionIndex = 0; sectionIndex < subChunksLength; sectionIndex++) {
            chunkSections[sectionIndex] = new ChunkSection();
            int chunkVersion = byteBuf.readByte();

            if (chunkVersion != 1 && chunkVersion != 8 && chunkVersion != 9) {
                // TODO: Support chunk version 0 (pm 3.0.0 legacy chunk)
                continue;
            }

            if (chunkVersion == 1) {
                networkDecodeVersionOne(byteBuf, chunkSections[sectionIndex]);
                continue;
            }

            if (chunkVersion == 9) {
                networkDecodeVersionNine(byteBuf, chunkSections, sectionIndex);
                continue;
            }

            networkDecodeVersionEight(byteBuf, chunkSections, sectionIndex, byteBuf.readByte());
            Utils.fillPalette(chunkSections[sectionIndex].getBiomeData()); //TODO: Read biome
        }

        CompoundTag heightMap = new CompoundTag("");
        heightMap.put(new LongArrayTag("MOTION_BLOCKING", new long[37]));

        for (int i = 0; i < subChunksLength; i++) {
            MinecraftCodec.CODEC.getHelperFactory().get().writeChunkSection(chunkByteBuf, chunkSections[i]);
        }
        ClientboundLevelChunkWithLightPacket chunkPacket = new ClientboundLevelChunkWithLightPacket(
                packet.getChunkX(), packet.getChunkZ(),
                chunkByteBuf.array(), heightMap, new BlockEntityInfo[0],
                new LightUpdateData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), Collections.emptyList(), Collections.emptyList(), true)
        );
        player.getJavaSession().send(chunkPacket);
        byteBuf.release();
        chunkByteBuf.release();
    }

    public void networkDecodeVersionNine(ByteBuf byteBuf, ChunkSection[] chunkSections, int sectionIndex) {
        byte storageSize = byteBuf.readByte();
        byteBuf.readByte(); // height
        networkDecodeVersionEight(byteBuf, chunkSections, sectionIndex, storageSize);
    }

    public void networkDecodeVersionEight(ByteBuf byteBuf, ChunkSection[] chunkSections, int sectionIndex, byte storageSize) {
        for (int storageReadIndex = 0; storageReadIndex < storageSize; storageReadIndex++) {
            if (storageReadIndex > 1) {
                return;
            }
            byte paletteHeader = byteBuf.readByte();
            boolean isRuntime = (paletteHeader & 1) == 1;
            int paletteVersion = (paletteHeader | 1) >> 1;

            BitArrayVersion bitArrayVersion = BitArrayVersion.get(paletteVersion, true);

            int maxBlocksInSection = 4096;
            BitArray bitArray = bitArrayVersion.createPalette(maxBlocksInSection);
            int wordsSize = bitArrayVersion.getWordsForSize(maxBlocksInSection);

            for (int wordIterationIndex = 0; wordIterationIndex < wordsSize; wordIterationIndex++) {
                int word = byteBuf.readIntLE();
                bitArray.getWords()[wordIterationIndex] = word;
            }

            int paletteSize = VarInts.readInt(byteBuf);
            int[] sectionPalette = new int[paletteSize];
            NBTInputStream nbtStream = isRuntime ? null : new NBTInputStream(new NetworkDataInputStream(new ByteBufInputStream(byteBuf)));
            for (int i = 0; i < paletteSize; i++) {
                if (isRuntime) {
                    sectionPalette[i] = VarInts.readInt(byteBuf);
                } else {
                    try {
                        NbtMapBuilder map = ((NbtMap) nbtStream.readTag()).toBuilder();
                        map.replace("name", "minecraft:" + map.get("name").toString());

                        System.out.println(map.build().toString());
                        //sectionPalette[i] = BlockPaletteTranslator.getBedrockBlockId(BlockPaletteTranslator.bedrockStateFromNBTMap(map.build()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            int index = 0;
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 16; y++) {
                        int paletteIndex = bitArray.get(index);
                        int mcbeBlockId = sectionPalette[paletteIndex];
                        int javaStateId = BlockConverter.bedrockRuntimeToJavaStateId(mcbeBlockId);

                        if (storageReadIndex == 0) {
                            chunkSections[sectionIndex].setBlock(x, y, z, javaStateId);
                        } else {
                            if (javaStateId == 34 || javaStateId == 35) { // water
                                int layer0 = chunkSections[sectionIndex].getBlock(x, y, z);
                                if (layer0 != 0) {
                                    int waterlogged = BlockConverter.javaBlockToWaterlogged(layer0);
                                    if (waterlogged != 1) {
                                        chunkSections[sectionIndex].setBlock(x, y, z, waterlogged);
                                    }
                                } else {
                                    chunkSections[sectionIndex].setBlock(x, y, z, javaStateId);
                                }
                            }
                        }

                        index++;
                    }
                }
            }
        }
    }

    public void networkDecodeVersionOne(ByteBuf byteBuf, ChunkSection chunkSection) {
        networkDecodeVersionEight(byteBuf, new ChunkSection[]{chunkSection}, 0, (byte) 1);
    }
}
