package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.data.game.MessageType;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class TextPacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.TextPacket packet = (com.nukkitx.protocol.bedrock.packet.TextPacket) pk;

        switch (packet.getType()) {
            case TIP:
            case POPUP: {
                player.getJavaSession().send(new ServerChatPacket(packet.getMessage(), MessageType.NOTIFICATION));
                break;
            }
            case SYSTEM: {
                player.getJavaSession().send(new ServerChatPacket(packet.getMessage(), MessageType.SYSTEM));
                break;
            }
            default: {
                player.sendMessage(packet.getMessage());
                break;
            }
        }
    }
}
