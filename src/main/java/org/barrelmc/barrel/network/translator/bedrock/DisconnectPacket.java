package org.barrelmc.barrel.network.translator.bedrock;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class DisconnectPacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.DisconnectPacket packet = (com.nukkitx.protocol.bedrock.packet.DisconnectPacket) pk;

        player.disconnect(packet.getKickMessage());
    }
}
