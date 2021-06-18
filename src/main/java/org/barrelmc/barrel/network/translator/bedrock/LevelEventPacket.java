package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.mc.protocol.data.game.entity.player.BlockBreakStage;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerBlockBreakAnimPacket;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.data.LevelEventType;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class LevelEventPacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.LevelEventPacket packet = (com.nukkitx.protocol.bedrock.packet.LevelEventPacket) pk;

        if (packet.getType() == LevelEventType.BLOCK_START_BREAK) {
            Vector3f pos = packet.getPosition();
            player.getJavaSession().send(new ServerBlockBreakAnimPacket(0, new Position((int) pos.getX(), (int) pos.getY(), (int) pos.getZ()), BlockBreakStage.STAGE_1));
        } else if (packet.getType() == LevelEventType.BLOCK_STOP_BREAK) {
            Vector3f pos = packet.getPosition();
            player.getJavaSession().send(new ServerBlockBreakAnimPacket(0, new Position((int) pos.getX(), (int) pos.getY(), (int) pos.getZ()), BlockBreakStage.RESET));
        }
    }
}
