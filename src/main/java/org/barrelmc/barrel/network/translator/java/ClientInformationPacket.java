package org.barrelmc.barrel.network.translator.java;

import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundClientInformationPacket;
import com.nukkitx.protocol.bedrock.packet.RequestChunkRadiusPacket;
import org.barrelmc.barrel.network.translator.interfaces.JavaPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class ClientInformationPacket implements JavaPacketTranslator {

    @Override
    public void translate(MinecraftPacket pk, Player player) {
        ServerboundClientInformationPacket settingsPacket = (ServerboundClientInformationPacket) pk;
        RequestChunkRadiusPacket chunkRadiusPacket = new RequestChunkRadiusPacket();

        chunkRadiusPacket.setRadius(settingsPacket.getRenderDistance());
        player.getBedrockClient().getSession().sendPacketImmediately(chunkRadiusPacket);
    }
}
