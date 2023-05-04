package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import net.kyori.adventure.text.Component;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class TextPacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.TextPacket packet = (com.nukkitx.protocol.bedrock.packet.TextPacket) pk;

        switch (packet.getType()) {
            case TIP:
            case POPUP: {
                player.sendTip(packet.getMessage());
                break;
            }
            case SYSTEM: {
                player.getJavaSession().send(new ClientboundSystemChatPacket(Component.text(packet.getMessage()), false));
                break;
            }
            default: {
                player.sendMessage(packet.getMessage());
                break;
            }
        }
    }
}
