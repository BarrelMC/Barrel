package org.barrelmc.barrel.network.translator.bedrock;

import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class DisconnectPacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket packet = (org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket) pk;

        player.disconnect(packet.getKickMessage());
    }
}
