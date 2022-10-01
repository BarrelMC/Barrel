package org.barrelmc.barrel.network.translator.bedrock;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.server.ProxyServer;

public class NetworkSettingsPacket implements BedrockPacketTranslator {
    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.NetworkSettingsPacket packet = (com.nukkitx.protocol.bedrock.packet.NetworkSettingsPacket) pk;
        player.getBedrockClient().getSession().setCompression(packet.getCompressionAlgorithm());

        if (ProxyServer.getInstance().getConfig().getAuth().equals("offline")) {
            player.getBedrockClient().getSession().sendPacketImmediately(player.getLoginPacket());
        } else {
            try {
                player.getBedrockClient().getSession().sendPacketImmediately(player.getOnlineLoginPacket());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
