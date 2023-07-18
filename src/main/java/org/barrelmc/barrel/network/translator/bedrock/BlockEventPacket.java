package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.data.game.level.block.value.ChestValue;
import com.github.steveice10.mc.protocol.data.game.level.block.value.ChestValueType;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundBlockEventPacket;
import com.nukkitx.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class BlockEventPacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        org.cloudburstmc.protocol.bedrock.packet.BlockEventPacket packet = (org.cloudburstmc.protocol.bedrock.packet.BlockEventPacket) pk;

        if (packet.getEventType() == 1) {
            Vector3i pos = Vector3i.from(packet.getBlockPosition().getX(), packet.getBlockPosition().getY(), packet.getBlockPosition().getZ());
            if (packet.getEventData() == 2) {
                player.getJavaSession().send(new ClientboundBlockEventPacket(pos, ChestValueType.VIEWING_PLAYER_COUNT, new ChestValue(1), 54));
            } else {
                player.getJavaSession().send(new ClientboundBlockEventPacket(pos, ChestValueType.VIEWING_PLAYER_COUNT, new ChestValue(0), 54));
            }
        }
    }
}
