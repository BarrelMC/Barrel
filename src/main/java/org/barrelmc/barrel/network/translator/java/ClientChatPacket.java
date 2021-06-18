package org.barrelmc.barrel.network.translator.java;

import com.github.steveice10.packetlib.packet.Packet;
import com.nukkitx.protocol.bedrock.packet.TextPacket;
import org.barrelmc.barrel.network.translator.interfaces.JavaPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class ClientChatPacket implements JavaPacketTranslator {

    @Override
    public void translate(Packet pk, Player player) {
        com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket chatPacket = (com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket) pk;
        TextPacket textPacket = new TextPacket();

        textPacket.setType(TextPacket.Type.CHAT);
        textPacket.setNeedsTranslation(false);
        textPacket.setSourceName(chatPacket.getMessage());
        textPacket.setMessage(chatPacket.getMessage());
        textPacket.setXuid("");
        textPacket.setPlatformChatId("");
        player.getBedrockClient().getSession().sendPacket(textPacket);
    }
}
