package org.barrelmc.barrel.network.translator.java;

import com.github.steveice10.packetlib.packet.Packet;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.data.PlayerActionType;
import com.nukkitx.protocol.bedrock.packet.PlayerActionPacket;
import org.barrelmc.barrel.network.translator.interfaces.JavaPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class ClientPlayerStatePacket implements JavaPacketTranslator {

    @Override
    public void translate(Packet pk, Player player) {
        com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerStatePacket packet = (com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerStatePacket) pk;

        switch (packet.getState()) {
            case START_SNEAKING: {
                PlayerActionPacket playerActionPacket = new PlayerActionPacket();
                playerActionPacket.setAction(PlayerActionType.START_SNEAK);
                playerActionPacket.setBlockPosition(Vector3i.ZERO);
                playerActionPacket.setFace(0);
                playerActionPacket.setRuntimeEntityId(player.getRuntimeEntityId());
                player.getBedrockClient().getSession().sendPacket(playerActionPacket);
                break;
            }
            case STOP_SNEAKING: {
                PlayerActionPacket playerActionPacket = new PlayerActionPacket();
                playerActionPacket.setAction(PlayerActionType.STOP_SNEAK);
                playerActionPacket.setBlockPosition(Vector3i.ZERO);
                playerActionPacket.setFace(0);
                playerActionPacket.setRuntimeEntityId(player.getRuntimeEntityId());
                player.getBedrockClient().getSession().sendPacket(playerActionPacket);
                break;
            }
            case START_SPRINTING: {
                PlayerActionPacket playerActionPacket = new PlayerActionPacket();
                playerActionPacket.setAction(PlayerActionType.START_SPRINT);
                playerActionPacket.setBlockPosition(Vector3i.ZERO);
                playerActionPacket.setFace(0);
                playerActionPacket.setRuntimeEntityId(player.getRuntimeEntityId());
                player.getBedrockClient().getSession().sendPacket(playerActionPacket);
                break;
            }
            case STOP_SPRINTING: {
                PlayerActionPacket playerActionPacket = new PlayerActionPacket();
                playerActionPacket.setAction(PlayerActionType.STOP_SPRINT);
                playerActionPacket.setBlockPosition(Vector3i.ZERO);
                playerActionPacket.setFace(0);
                playerActionPacket.setRuntimeEntityId(player.getRuntimeEntityId());
                player.getBedrockClient().getSession().sendPacket(playerActionPacket);
                break;
            }
        }
    }
}
