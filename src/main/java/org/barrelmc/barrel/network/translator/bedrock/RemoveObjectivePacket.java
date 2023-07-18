package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetObjectivePacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class RemoveObjectivePacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        org.cloudburstmc.protocol.bedrock.packet.RemoveObjectivePacket packet = (org.cloudburstmc.protocol.bedrock.packet.RemoveObjectivePacket) pk;
        player.getJavaSession().send(new ClientboundSetObjectivePacket(packet.getObjectiveId()));
    }
}
