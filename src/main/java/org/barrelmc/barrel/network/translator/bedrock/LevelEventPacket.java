package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.data.game.entity.player.BlockBreakStage;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundBlockDestructionPacket;
import com.nukkitx.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.LevelEvent;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class LevelEventPacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket packet = (org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket) pk;

        if (packet.getType() == LevelEvent.BLOCK_START_BREAK) {
            Vector3f pos = Vector3f.from(packet.getPosition().getX(), packet.getPosition().getY(), packet.getPosition().getZ());
            player.getJavaSession().send(new ClientboundBlockDestructionPacket(0, pos.toInt(), BlockBreakStage.STAGE_1));
        } else if (packet.getType() == LevelEvent.BLOCK_STOP_BREAK) {
            Vector3f pos = Vector3f.from(packet.getPosition().getX(), packet.getPosition().getY(), packet.getPosition().getZ());
            player.getJavaSession().send(new ClientboundBlockDestructionPacket(0, pos.toInt(), BlockBreakStage.RESET));
        }
    }
}
