package org.barrelmc.barrel.network.translator.java;

import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.mc.protocol.data.game.ClientCommand;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundClientCommandPacket;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.packet.RespawnPacket;
import org.barrelmc.barrel.network.translator.interfaces.JavaPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class ClientCommandPacket implements JavaPacketTranslator {

    @Override
    public void translate(MinecraftPacket pk, Player player) {
        ServerboundClientCommandPacket packet = (ServerboundClientCommandPacket) pk;

        if (packet.getRequest() == ClientCommand.RESPAWN) {
            RespawnPacket respawnPacket = new RespawnPacket();

            respawnPacket.setPosition(Vector3f.from(0, 0, 0));
            respawnPacket.setRuntimeEntityId(player.getRuntimeEntityId());
            respawnPacket.setState(RespawnPacket.State.CLIENT_READY);
            player.getBedrockClientSession().sendPacket(respawnPacket);
        }
    }
}
