/*
 * Copyright (c) 2021 BarrelMC Team
 * This project is licensed under the MIT License
 */

package org.barrelmc.barrel.auth.server;

import com.github.steveice10.mc.protocol.codec.MinecraftCodec;
import com.github.steveice10.mc.protocol.codec.MinecraftCodecHelper;
import com.github.steveice10.mc.protocol.data.game.chunk.ChunkSection;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.data.game.level.LightUpdateData;
import com.github.steveice10.mc.protocol.data.game.level.block.BlockEntityInfo;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundSetDefaultSpawnPositionPacket;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.LongArrayTag;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.nukkitx.math.vector.Vector3i;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import org.barrelmc.barrel.auth.AuthManager;
import org.barrelmc.barrel.server.ProxyServer;
import org.barrelmc.barrel.utils.Utils;

import java.util.BitSet;
import java.util.Collections;
import java.util.Timer;

public class AuthServer extends SessionAdapter {

    private final String username;

    public AuthServer(Session session, String username) {
        this.username = username;
        session.send(new ClientboundLoginPacket(
                0, false, GameMode.ADVENTURE, GameMode.ADVENTURE,
                1, new String[]{"minecraft:overworld"}, ProxyServer.getInstance().getDimensionTag(),
                "minecraft:overworld", "minecraft:overworld", 100,
                10, 6, 6, false, true, false, false, null
        ));

        ChunkSection emptyChunk = new ChunkSection();
        Utils.fillPalette(emptyChunk.getChunkData());
        Utils.fillPalette(emptyChunk.getBiomeData());

        ChunkSection chunk = new ChunkSection();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    chunk.setBlock(x, y, z, y == 0 ? 14 : 0);
                }
            }
        }
        Utils.fillPalette(chunk.getBiomeData());

        ByteBuf bytebuf = Unpooled.buffer();
        MinecraftCodecHelper helper = MinecraftCodec.CODEC.getHelperFactory().get();
        for (int i = 0; i < 16; i++) {
            if (i == 5) {
                helper.writeChunkSection(bytebuf, chunk);
                continue;
            }
            helper.writeChunkSection(bytebuf, emptyChunk);
        }

        CompoundTag heightMaps = new CompoundTag("");
        heightMaps.put(new LongArrayTag("MOTION_BLOCKING", new long[37]));
        session.send(new ClientboundLevelChunkWithLightPacket(
                0, 0, bytebuf.array(), heightMaps,
                new BlockEntityInfo[0],
                new LightUpdateData(new BitSet(), new BitSet(), new BitSet(), new BitSet(), Collections.emptyList(), Collections.emptyList(), true)
        ));
        bytebuf.release();

        session.send(new ClientboundSetDefaultSpawnPositionPacket(Vector3i.from(8, 82, 8), 0));
        session.send(new ClientboundPlayerPositionPacket(8, 82, 8, 0, 0, 0, false));

        session.send(new ClientboundSystemChatPacket(Component.text("§ePlease login with your Xbox account"), false));
        try {
            Timer timer = AuthManager.getInstance().getXboxLive().requestLiveToken(session, this.username);
            AuthManager.getInstance().getTimers().put(this.username, timer);
        } catch (Exception e) {
            session.disconnect("§cAn error occurred while authenticating to Xbox Live.");
            e.printStackTrace();
        }
    }
}
