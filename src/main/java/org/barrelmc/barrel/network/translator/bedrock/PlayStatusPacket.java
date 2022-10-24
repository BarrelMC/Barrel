package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import com.nukkitx.math.vector.Vector2f;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.data.AuthoritativeMovementMode;
import com.nukkitx.protocol.bedrock.packet.SetLocalPlayerAsInitializedPacket;
import com.nukkitx.protocol.bedrock.packet.TickSyncPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class PlayStatusPacket implements BedrockPacketTranslator {

    @Override
    public boolean immediate() {
        return true;
    }

    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.PlayStatusPacket packet = (com.nukkitx.protocol.bedrock.packet.PlayStatusPacket) pk;

        if (packet.getStatus() == com.nukkitx.protocol.bedrock.packet.PlayStatusPacket.Status.PLAYER_SPAWN) {
            TickSyncPacket tickSyncPacket = new TickSyncPacket();
            tickSyncPacket.setRequestTimestamp(0);
            tickSyncPacket.setResponseTimestamp(0);
            player.getBedrockClient().getSession().sendPacketImmediately(tickSyncPacket);

            if (player.getStartGamePacketCache().getPlayerMovementSettings().getMovementMode() == AuthoritativeMovementMode.SERVER) {
                player.startSendingPlayerInput();
            }

            SetLocalPlayerAsInitializedPacket setLocalPlayerAsInitializedPacket = new SetLocalPlayerAsInitializedPacket();
            setLocalPlayerAsInitializedPacket.setRuntimeEntityId(player.getRuntimeEntityId());
            player.getBedrockClient().getSession().sendPacket(setLocalPlayerAsInitializedPacket);

            Vector3f pos = player.getLastServerPosition();
            Vector2f rotation = player.getLastServerRotation();
            player.getJavaSession().send(new ClientboundPlayerPositionPacket(pos.getX(), pos.getY(), pos.getZ(), rotation.getY(), pos.getX(), 0, false));
        }
    }
}
