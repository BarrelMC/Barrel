package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundSetDefaultSpawnPositionPacket;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class SetSpawnPositionPacket implements BedrockPacketTranslator {
    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.SetSpawnPositionPacket packet = (com.nukkitx.protocol.bedrock.packet.SetSpawnPositionPacket) pk;
        Vector3i pos = packet.getSpawnPosition();
        player.getJavaSession().send(new ClientboundSetDefaultSpawnPositionPacket(pos, 0.0f));
    }
}
