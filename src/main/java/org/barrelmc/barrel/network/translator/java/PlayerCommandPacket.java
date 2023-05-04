package org.barrelmc.barrel.network.translator.java;

import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundPlayerCommandPacket;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.data.AuthoritativeMovementMode;
import com.nukkitx.protocol.bedrock.data.PlayerActionType;
import com.nukkitx.protocol.bedrock.data.PlayerAuthInputData;
import com.nukkitx.protocol.bedrock.packet.PlayerActionPacket;
import org.barrelmc.barrel.network.translator.interfaces.JavaPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class PlayerCommandPacket implements JavaPacketTranslator {

    @Override
    public void translate(MinecraftPacket pk, Player player) {
        ServerboundPlayerCommandPacket packet = (ServerboundPlayerCommandPacket) pk;

        switch (packet.getState()) {
            case START_SNEAKING: {
                if (player.getStartGamePacketCache().getPlayerMovementSettings().getMovementMode() == AuthoritativeMovementMode.CLIENT) {
                    PlayerActionPacket playerActionPacket = new PlayerActionPacket();
                    playerActionPacket.setAction(PlayerActionType.START_SNEAK);
                    playerActionPacket.setBlockPosition(Vector3i.ZERO);
                    playerActionPacket.setFace(0);
                    playerActionPacket.setRuntimeEntityId(player.getRuntimeEntityId());
                    player.getBedrockClient().getSession().sendPacket(playerActionPacket);
                } else {
                    player.getPlayerAuthInputData().add(PlayerAuthInputData.START_SNEAKING);
                    player.setSneaking(true);
                }
                break;
            }
            case STOP_SNEAKING: {
                if (player.getStartGamePacketCache().getPlayerMovementSettings().getMovementMode() == AuthoritativeMovementMode.CLIENT) {
                    PlayerActionPacket playerActionPacket = new PlayerActionPacket();
                    playerActionPacket.setAction(PlayerActionType.STOP_SNEAK);
                    playerActionPacket.setBlockPosition(Vector3i.ZERO);
                    playerActionPacket.setFace(0);
                    playerActionPacket.setRuntimeEntityId(player.getRuntimeEntityId());
                    player.getBedrockClient().getSession().sendPacket(playerActionPacket);
                } else {
                    player.setSneaking(false);
                    player.getPlayerAuthInputData().add(PlayerAuthInputData.STOP_SNEAKING);
                }
                break;
            }
            case START_SPRINTING: {
                if (player.getStartGamePacketCache().getPlayerMovementSettings().getMovementMode() == AuthoritativeMovementMode.CLIENT) {
                    PlayerActionPacket playerActionPacket = new PlayerActionPacket();
                    playerActionPacket.setAction(PlayerActionType.START_SPRINT);
                    playerActionPacket.setBlockPosition(Vector3i.ZERO);
                    playerActionPacket.setFace(0);
                    playerActionPacket.setRuntimeEntityId(player.getRuntimeEntityId());
                    player.getBedrockClient().getSession().sendPacket(playerActionPacket);
                } else {
                    player.getPlayerAuthInputData().add(PlayerAuthInputData.START_SPRINTING);
                    player.setSprinting(true);
                }
                break;
            }
            case STOP_SPRINTING: {
                if (player.getStartGamePacketCache().getPlayerMovementSettings().getMovementMode() == AuthoritativeMovementMode.CLIENT) {
                    PlayerActionPacket playerActionPacket = new PlayerActionPacket();
                    playerActionPacket.setAction(PlayerActionType.STOP_SPRINT);
                    playerActionPacket.setBlockPosition(Vector3i.ZERO);
                    playerActionPacket.setFace(0);
                    playerActionPacket.setRuntimeEntityId(player.getRuntimeEntityId());
                    player.getBedrockClient().getSession().sendPacket(playerActionPacket);
                } else {
                    player.setSprinting(false);
                    player.getPlayerAuthInputData().add(PlayerAuthInputData.STOP_SPRINTING);
                }
                break;
            }
        }
    }
}
