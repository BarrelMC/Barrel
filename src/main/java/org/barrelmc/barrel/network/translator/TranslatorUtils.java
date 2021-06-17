/*
 * Copyright (c) 2021 BarrelMC Team
 * This project is licensed under the MIT License
 */

package org.barrelmc.barrel.network.translator;

import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.nukkitx.protocol.bedrock.data.GameType;
import io.netty.buffer.ByteBuf;
import org.barrelmc.barrel.network.converter.BlockConverter;

public class TranslatorUtils {

    public static GameMode translateGamemodeToJE(GameType gameType) {
        String gameTypeString = gameType.toString();
        if (gameTypeString.contains("VIEWER")) {
            gameTypeString = GameMode.SPECTATOR.name();
        }

        return GameMode.valueOf(gameTypeString);
    }

    /**
     * Used for PocketMine.
     */
    public static void manage0VersionChunk(ByteBuf byteBuf, Chunk chunkSection) {
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
