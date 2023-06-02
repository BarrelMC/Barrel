package org.barrelmc.barrel.network.translator.java;

import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.cloudburstmc.protocol.bedrock.packet.TextPacket;
import org.barrelmc.barrel.network.translator.interfaces.JavaPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class ChatPacket implements JavaPacketTranslator {

    @Override
    public void translate(MinecraftPacket pk, Player player) {
        ServerboundChatPacket chatPacket = (ServerboundChatPacket) pk;
        TextPacket textPacket = new TextPacket();

        textPacket.setType(TextPacket.Type.CHAT);
        textPacket.setNeedsTranslation(false);
        textPacket.setSourceName(chatPacket.getMessage());
        textPacket.setMessage(chatPacket.getMessage());
        textPacket.setXuid("");
        textPacket.setPlatformChatId("");
        player.getBedrockClientSession().sendPacket(textPacket);
    }
}
