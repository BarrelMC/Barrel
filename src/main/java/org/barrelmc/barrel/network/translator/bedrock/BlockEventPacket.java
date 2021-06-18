package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.mc.protocol.data.game.world.block.value.ChestValue;
import com.github.steveice10.mc.protocol.data.game.world.block.value.ChestValueType;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerBlockValuePacket;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class BlockEventPacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.BlockEventPacket packet = (com.nukkitx.protocol.bedrock.packet.BlockEventPacket) pk;

        if (packet.getEventType() == 1) {
            Vector3i pos = packet.getBlockPosition();
            Position blockPos = new Position(pos.getX(), pos.getY(), pos.getZ());
            if (packet.getEventData() == 2) {
                player.getJavaSession().send(new ServerBlockValuePacket(blockPos, ChestValueType.VIEWING_PLAYER_COUNT, new ChestValue(1), 54));
            } else {
                player.getJavaSession().send(new ServerBlockValuePacket(blockPos, ChestValueType.VIEWING_PLAYER_COUNT, new ChestValue(0), 54));
            }
        }
    }
}
