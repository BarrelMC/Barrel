package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.ClientboundRemoveEntitiesPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.ClientboundTakeItemEntityPacket;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class TakeItemEntityPacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.TakeItemEntityPacket packet = (com.nukkitx.protocol.bedrock.packet.TakeItemEntityPacket) pk;

        int[] entityIds = new int[1];
        entityIds[0] = (int) packet.getItemRuntimeEntityId();

        player.getJavaSession().send(new ClientboundRemoveEntitiesPacket(entityIds));
        player.getJavaSession().send(new ClientboundTakeItemEntityPacket((int) packet.getItemRuntimeEntityId(), (int) packet.getRuntimeEntityId(), 1));
    }
}
