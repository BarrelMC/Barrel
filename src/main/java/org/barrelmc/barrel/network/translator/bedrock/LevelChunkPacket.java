package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.nukkitx.network.VarInts;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.barrelmc.barrel.network.converter.BlockConverter;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.utils.nukkit.BitArray;
import org.barrelmc.barrel.utils.nukkit.BitArrayVersion;

public class LevelChunkPacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.LevelChunkPacket packet = (com.nukkitx.protocol.bedrock.packet.LevelChunkPacket) pk;

        Chunk[] chunkSections = new Chunk[16];

        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(packet.getData());

        for (int sectionIndex = 0; sectionIndex < packet.getSubChunksLength(); sectionIndex++) {
            chunkSections[sectionIndex] = new Chunk();
            int chunkVersion = byteBuf.readByte();
            if (chunkVersion != 1 && chunkVersion != 8) {
                this.manage0VersionChunk(byteBuf, chunkSections[sectionIndex]);
                continue;
            }

            byte storageSize = chunkVersion == 1 ? 1 : byteBuf.readByte();

            for (int storageReadIndex = 0; storageReadIndex < storageSize; storageReadIndex++) {
                byte paletteHeader = byteBuf.readByte();
                int paletteVersion = (paletteHeader | 1) >> 1;

                BitArrayVersion bitArrayVersion = BitArrayVersion.get(paletteVersion, true);

                int maxBlocksInSection = 4096; // 16*16*16
                BitArray bitArray = bitArrayVersion.createPalette(maxBlocksInSection);
                int wordsSize = bitArrayVersion.getWordsForSize(maxBlocksInSection);

                for (int wordIterationIndex = 0; wordIterationIndex < wordsSize; wordIterationIndex++) {
                    int word = byteBuf.readIntLE();
                    bitArray.getWords()[wordIterationIndex] = word;
                }

                int paletteSize = VarInts.readInt(byteBuf);
                int[] sectionPalette = new int[paletteSize];
                for (int i = 0; i < paletteSize; i++) {
                    sectionPalette[i] = VarInts.readInt(byteBuf);
                }

                if (storageReadIndex == 0) {
                    int index = 0;
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = 0; y < 16; y++) {
                                int paletteIndex = bitArray.get(index);
                                int mcbeBlockId = sectionPalette[paletteIndex];
                                if (mcbeBlockId != 0) {
                                    chunkSections[sectionIndex].set(x, y, z, BlockConverter.toJavaStateId(mcbeBlockId));
                                }
                                index++;
                            }
                        }
                    }
                }
            }
        }

        CompoundTag heightMap = new CompoundTag("MOTION_BLOCKING");

        ServerChunkDataPacket chunkPacket = new ServerChunkDataPacket(new Column(packet.getChunkX(), packet.getChunkZ(), chunkSections, new CompoundTag[0], heightMap, new int[1024]));
        player.getJavaSession().send(chunkPacket);
    }

    public void manage0VersionChunk(ByteBuf byteBuf, Chunk chunkSection) {
        byte[] blockIds = new byte[4096];
        byteBuf.readBytes(blockIds);

        byte[] metaIdsTemp = new byte[2048];
        byteBuf.readBytes(metaIdsTemp);
        byte[] metaIds = new byte[2048];

        for (int i = 0; i < metaIdsTemp.length; i++) {
            int value = metaIdsTemp[i] & 15;
            int i1 = i >> 1;
            metaIds[i1] &= 15 << (i + 1 & 1) * 4;
            metaIds[i1] |= value << (i & 1) * 4;
        }

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    int idx = (x << 8) + (z << 4) + y;
                    int id = blockIds[idx];
                    int meta = metaIds[idx >> 1] >> (idx & 1) * 4 & 15;

                    chunkSection.set(x, y, z, BlockConverter.toJavaStateId(id));
                }
            }
        }
    }
}
