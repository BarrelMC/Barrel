package org.barrelmc.barrel.network.translator.java;

import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerRotPacket;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.data.AuthoritativeMovementMode;
import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;
import org.barrelmc.barrel.network.translator.interfaces.JavaPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class MovePlayerRotPacket implements JavaPacketTranslator {

    @Override
    public void translate(MinecraftPacket pk, Player player) {
        ServerboundMovePlayerRotPacket packet = (ServerboundMovePlayerRotPacket) pk;

        player.setRotation(packet.getYaw(), packet.getPitch());

        if (player.getStartGamePacketCache().getPlayerMovementSettings().getMovementMode() == AuthoritativeMovementMode.CLIENT) {
            MovePlayerPacket movePlayerPacket = new MovePlayerPacket();

            movePlayerPacket.setMode(MovePlayerPacket.Mode.HEAD_ROTATION);
            movePlayerPacket.setOnGround(packet.isOnGround());
            movePlayerPacket.setRuntimeEntityId(player.getRuntimeEntityId());
            movePlayerPacket.setRidingRuntimeEntityId(0);
            movePlayerPacket.setPosition(player.getVector3f());
            movePlayerPacket.setRotation(Vector3f.from(packet.getPitch(), packet.getYaw(), 0));
            movePlayerPacket.setTeleportationCause(MovePlayerPacket.TeleportationCause.UNKNOWN);
            movePlayerPacket.setEntityType(0);

            player.getBedrockClient().getSession().sendPacket(movePlayerPacket);
        }
    }
}
