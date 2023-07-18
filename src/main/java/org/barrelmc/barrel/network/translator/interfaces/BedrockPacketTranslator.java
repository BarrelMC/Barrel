package org.barrelmc.barrel.network.translator.interfaces;

import org.barrelmc.barrel.player.Player;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;

public interface BedrockPacketTranslator {

    default boolean immediate() {
        return false;
    }

    void translate(BedrockPacket pk, Player player);
}
