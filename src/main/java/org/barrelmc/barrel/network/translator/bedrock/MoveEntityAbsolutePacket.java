package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.ClientboundRotateHeadPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.ClientboundTeleportEntityPacket;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class MoveEntityAbsolutePacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.MoveEntityAbsolutePacket packet = (com.nukkitx.protocol.bedrock.packet.MoveEntityAbsolutePacket) pk;
        Vector3f position = packet.getPosition(), rotation = packet.getRotation();

        player.getJavaSession().send(new ClientboundTeleportEntityPacket((int) packet.getRuntimeEntityId(), position.getX(), position.getY() - 1.62F, position.getZ(), rotation.getY(), rotation.getX(), packet.isOnGround()));
        player.getJavaSession().send(new ClientboundRotateHeadPacket((int) packet.getRuntimeEntityId(), rotation.getZ()));
    }
}
