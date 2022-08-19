package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerRemoveEntitiesPacket;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class RemoveEntityPacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.RemoveEntityPacket packet = (com.nukkitx.protocol.bedrock.packet.RemoveEntityPacket) pk;

        int[] entityIds = new int[1];
        entityIds[0] = (int) packet.getUniqueEntityId();
        player.getJavaSession().send(new ServerRemoveEntitiesPacket(entityIds));
    }
}
