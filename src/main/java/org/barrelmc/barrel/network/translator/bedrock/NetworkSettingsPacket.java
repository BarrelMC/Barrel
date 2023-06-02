package org.barrelmc.barrel.network.translator.bedrock;

import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.server.ProxyServer;

public class NetworkSettingsPacket implements BedrockPacketTranslator {
    @Override
    public void translate(BedrockPacket pk, Player player) {
        org.cloudburstmc.protocol.bedrock.packet.NetworkSettingsPacket packet = (org.cloudburstmc.protocol.bedrock.packet.NetworkSettingsPacket) pk;
        player.getBedrockClientSession().setCompression(packet.getCompressionAlgorithm());

        try {
            if (ProxyServer.getInstance().getConfig().getAuth().equals("offline")) {
                player.getBedrockClientSession().sendPacketImmediately(player.getLoginPacket());
            } else {
                player.getBedrockClientSession().sendPacketImmediately(player.getOnlineLoginPacket());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
