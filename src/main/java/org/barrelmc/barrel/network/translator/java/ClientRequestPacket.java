package org.barrelmc.barrel.network.translator.java;

import com.github.steveice10.mc.protocol.data.game.ClientRequest;
import com.github.steveice10.packetlib.packet.Packet;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.packet.RespawnPacket;
import org.barrelmc.barrel.network.translator.interfaces.JavaPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class ClientRequestPacket implements JavaPacketTranslator {

    @Override
    public void translate(Packet pk, Player player) {
        com.github.steveice10.mc.protocol.packet.ingame.client.ClientRequestPacket packet = (com.github.steveice10.mc.protocol.packet.ingame.client.ClientRequestPacket) pk;

        if (packet.getRequest() == ClientRequest.RESPAWN) {
            RespawnPacket respawnPacket = new RespawnPacket();

            respawnPacket.setPosition(Vector3f.from(0, 0, 0));
            respawnPacket.setRuntimeEntityId(player.getRuntimeEntityId());
            respawnPacket.setState(RespawnPacket.State.CLIENT_READY);
            player.getBedrockClient().getSession().sendPacket(respawnPacket);
        }
    }
}
