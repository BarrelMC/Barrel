/*
 * Copyright (c) 2021 BarrelMC Team
 * This project is licensed under the MIT License
 */

package org.barrelmc.barrel.network;

import org.barrelmc.barrel.player.Player;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketHandler;
import org.cloudburstmc.protocol.common.PacketSignal;

public class BedrockBatchHandler implements BedrockPacketHandler {

    private final Player player;

    public BedrockBatchHandler(Player player) {
        this.player = player;
    }

    @Override
    public PacketSignal handlePacket(BedrockPacket packet) {
        player.getPacketTranslatorManager().translate(packet);
        return PacketSignal.HANDLED;
    }
}
