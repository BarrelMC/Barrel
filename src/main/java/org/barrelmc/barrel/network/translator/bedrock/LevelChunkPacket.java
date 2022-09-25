package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
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
import org.barrelmc.barrel.utils.nukkit.BitArray;
import org.barrelmc.barrel.utils.nukkit.BitArrayVersion;

import java.io.IOException;

public class LevelChunkPacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.LevelChunkPacket packet = (com.nukkitx.protocol.bedrock.packet.LevelChunkPacket) pk;

        Chunk[] chunkSections = new Chunk[16];

        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(packet.getData());

        int subChunksLength = packet.getSubChunksLength();

        if (subChunksLength > 15) {
            subChunksLength = 15;
        }

        for (int sectionIndex = 0; sectionIndex < subChunksLength; sectionIndex++) {
            chunkSections[sectionIndex] = new Chunk();
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
        }

        CompoundTag heightMap = new CompoundTag("MOTION_BLOCKING");

        ServerChunkDataPacket chunkPacket = new ServerChunkDataPacket(new Column(packet.getChunkX(), packet.getChunkZ(), chunkSections, new CompoundTag[0], heightMap, new int[1024]));
        player.getJavaSession().send(chunkPacket);
    }

    public void networkDecodeVersionNine(ByteBuf byteBuf, Chunk[] chunkSections, int sectionIndex) {
        byte storageSize = byteBuf.readByte();
        byteBuf.readByte();
        networkDecodeVersionEight(byteBuf, chunkSections, sectionIndex, storageSize);
    }

    public void networkDecodeVersionEight(ByteBuf byteBuf, Chunk[] chunkSections, int sectionIndex, byte storageSize) {
        for (int storageReadIndex = 0; storageReadIndex < storageSize; storageReadIndex++) {
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

            if (storageReadIndex == 0) {
                int index = 0;
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < 16; y++) {
                            int paletteIndex = bitArray.get(index);
                            int mcbeBlockId = sectionPalette[paletteIndex];

                            chunkSections[sectionIndex].set(x, y, z, BlockConverter.bedrockRuntimeToJavaStateId(mcbeBlockId));

                            index++;
                        }
                    }
                }
            }
        }
    }

    public void networkDecodeVersionOne(ByteBuf byteBuf, Chunk chunkSection) {
        networkDecodeVersionEight(byteBuf, new Chunk[]{chunkSection}, 0, (byte) 1);
    }
}
