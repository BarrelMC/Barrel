package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.packet.ingame.server.scoreboard.ServerScoreboardObjectivePacket;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class RemoveObjectivePacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.RemoveObjectivePacket packet = (com.nukkitx.protocol.bedrock.packet.RemoveObjectivePacket) pk;
        player.getJavaSession().send(new ServerScoreboardObjectivePacket(packet.getObjectiveId()));
    }
}
