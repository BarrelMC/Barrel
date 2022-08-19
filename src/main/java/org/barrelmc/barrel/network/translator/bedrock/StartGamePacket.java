package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.data.game.entity.EntityStatus;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityStatusPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.nukkitx.math.vector.Vector2f;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import org.barrelmc.barrel.network.translator.TranslatorUtils;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.server.ProxyServer;

public class StartGamePacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.StartGamePacket packet = (com.nukkitx.protocol.bedrock.packet.StartGamePacket) pk;

        player.setRuntimeEntityId(packet.getRuntimeEntityId());
        player.setStartGamePacketCache(packet);

        ServerJoinGamePacket serverJoinGamePacket = new ServerJoinGamePacket(
                (int) packet.getRuntimeEntityId(), false,
                TranslatorUtils.translateGamemodeToJE(packet.getPlayerGameType()),
                TranslatorUtils.translateGamemodeToJE(packet.getPlayerGameType()),
                1, new String[]{"minecraft:world"}, ProxyServer.getInstance().getDimensionTag(),
                ProxyServer.getInstance().getOverworldTag(), "minecraft:world", 100,
                10, 16, false, true, false, false
        );

        player.getJavaSession().send(serverJoinGamePacket);

        Vector3f position = packet.getPlayerPosition();
        Vector2f rotation = packet.getRotation();
        ServerPlayerPositionRotationPacket serverPlayerPositionRotationPacket = new ServerPlayerPositionRotationPacket(position.getX(), position.getY(), position.getZ(), rotation.getY(), rotation.getX(), 0, false);
        player.getJavaSession().send(serverPlayerPositionRotationPacket);
        player.getJavaSession().send(new ServerEntityStatusPacket((int) packet.getRuntimeEntityId(), EntityStatus.PLAYER_OP_PERMISSION_LEVEL_0));
    }
}
