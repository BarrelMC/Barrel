package org.barrelmc.barrel.network.translator.java;

import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundMovePlayerPosRotPacket;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.data.AuthoritativeMovementMode;
import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;
import org.barrelmc.barrel.network.translator.interfaces.JavaPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class MovePlayerPosRotPacket implements JavaPacketTranslator {

    @Override
    public void translate(MinecraftPacket pk, Player player) {
        ServerboundMovePlayerPosRotPacket packet = (ServerboundMovePlayerPosRotPacket) pk;

        if (player.isImmobile()) {
            player.getJavaSession().send(new ClientboundPlayerPositionPacket(player.x, player.y, player.z, player.yaw, player.pitch, 1, false));
            return;
        }
        player.setOldPosition(player.getVector3f());
        player.setLocation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());

        if (player.getStartGamePacketCache().getPlayerMovementSettings().getMovementMode() == AuthoritativeMovementMode.CLIENT) {
            MovePlayerPacket movePlayerPacket = new MovePlayerPacket();

            movePlayerPacket.setRuntimeEntityId(player.getRuntimeEntityId());
            movePlayerPacket.setPosition(player.getVector3f());
            movePlayerPacket.setRotation(Vector3f.from(packet.getPitch(), packet.getYaw(), packet.getYaw()));
            movePlayerPacket.setMode(MovePlayerPacket.Mode.NORMAL);
            movePlayerPacket.setOnGround(packet.isOnGround());
            movePlayerPacket.setRidingRuntimeEntityId(0);
            movePlayerPacket.setTeleportationCause(MovePlayerPacket.TeleportationCause.UNKNOWN);
            movePlayerPacket.setEntityType(0);

            player.getBedrockClient().getSession().sendPacket(movePlayerPacket);
        }
    }
}
