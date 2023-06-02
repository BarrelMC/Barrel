package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundSetDefaultSpawnPositionPacket;
import com.nukkitx.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class SetSpawnPositionPacket implements BedrockPacketTranslator {
    @Override
    public void translate(BedrockPacket pk, Player player) {
        org.cloudburstmc.protocol.bedrock.packet.SetSpawnPositionPacket packet = (org.cloudburstmc.protocol.bedrock.packet.SetSpawnPositionPacket) pk;
        player.getJavaSession().send(new ClientboundSetDefaultSpawnPositionPacket(Vector3i.from(packet.getSpawnPosition().getX(), packet.getSpawnPosition().getY(), packet.getSpawnPosition().getZ()), 0.0f));
    }
}
