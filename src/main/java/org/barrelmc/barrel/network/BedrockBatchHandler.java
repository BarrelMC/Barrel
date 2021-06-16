/*
 * Copyright (c) 2021 BarrelMC
 * BarrelMC/Barrel is licensed under the MIT License
 */

package org.barrelmc.barrel.network;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockSession;
import com.nukkitx.protocol.bedrock.handler.BatchHandler;
import io.netty.buffer.ByteBuf;
import org.barrelmc.barrel.network.translator.PacketTranslator;
import org.barrelmc.barrel.player.Player;

import java.util.Collection;

public class BedrockBatchHandler implements BatchHandler {

    private final Player player;

    public BedrockBatchHandler(Player player) {
        this.player = player;
    }

    @Override
    public void handle(BedrockSession bedrockSession, ByteBuf byteBuf, Collection<BedrockPacket> collection) {
        for (BedrockPacket packet : collection) {
            PacketTranslator.translateToJava(packet, this.player);
        }
    }
}
