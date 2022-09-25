package org.barrelmc.barrel.network.translator.bedrock;

import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.data.entity.EntityFlag;
import com.nukkitx.protocol.bedrock.data.entity.EntityFlags;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class SetEntityDataPacket implements BedrockPacketTranslator {
    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.SetEntityDataPacket packet = (com.nukkitx.protocol.bedrock.packet.SetEntityDataPacket) pk;
        if (packet.getRuntimeEntityId() == player.getRuntimeEntityId() && packet.getMetadata().getFlags() != null) {
            player.setImmobile(packet.getMetadata().getFlags().getFlag(EntityFlag.NO_AI));
        }
    }
}
