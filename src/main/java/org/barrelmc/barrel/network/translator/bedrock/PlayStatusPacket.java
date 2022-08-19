package org.barrelmc.barrel.network.translator.bedrock;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.packet.SetLocalPlayerAsInitializedPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class PlayStatusPacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.PlayStatusPacket packet = (com.nukkitx.protocol.bedrock.packet.PlayStatusPacket) pk;

        if (packet.getStatus() == com.nukkitx.protocol.bedrock.packet.PlayStatusPacket.Status.PLAYER_SPAWN) {
            SetLocalPlayerAsInitializedPacket setLocalPlayerAsInitializedPacket = new SetLocalPlayerAsInitializedPacket();
            setLocalPlayerAsInitializedPacket.setRuntimeEntityId(player.getRuntimeEntityId());
            player.getBedrockClient().getSession().sendPacket(setLocalPlayerAsInitializedPacket);
        }
    }
}
