package org.barrelmc.barrel.network.translator.bedrock;

import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class SetEntityDataPacket implements BedrockPacketTranslator {
    @Override
    public void translate(BedrockPacket pk, Player player) {
        org.cloudburstmc.protocol.bedrock.packet.SetEntityDataPacket packet = (org.cloudburstmc.protocol.bedrock.packet.SetEntityDataPacket) pk;
        if (packet.getRuntimeEntityId() == player.getRuntimeEntityId() && packet.getMetadata().getFlags() != null) {
            player.setImmobile(packet.getMetadata().getFlags().contains(EntityFlag.NO_AI));
        }
    }
}
