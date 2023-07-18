package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetScorePacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.data.ScoreInfo;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.utils.Utils;

import java.util.List;

public class SetScorePacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        org.cloudburstmc.protocol.bedrock.packet.SetScorePacket packet = (org.cloudburstmc.protocol.bedrock.packet.SetScorePacket) pk;
        List<ScoreInfo> infos = packet.getInfos();
        for (ScoreInfo scoreInfo : infos) {
            int score = scoreInfo.getScore();
            if (player.getScoreSortorder() == 0) {
                score = -score;
            }

            player.getJavaSession().send(new ClientboundSetScorePacket(Utils.lengthCutter(scoreInfo.getName(), 40), scoreInfo.getObjectiveId(), score));
        }
    }
}
