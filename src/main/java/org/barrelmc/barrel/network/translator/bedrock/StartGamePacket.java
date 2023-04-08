package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.data.game.entity.EntityEvent;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.ClientboundEntityEventPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
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
        player.setOldPosition(packet.getPlayerPosition());
        player.setPosition(packet.getPlayerPosition());
        player.setLastServerPosition(packet.getPlayerPosition());
        player.setLastServerRotation(packet.getRotation());

        player.setStartGamePacketCache(packet);

        player.setGameMode(packet.getPlayerGameType());

        ClientboundLoginPacket serverJoinGamePacket = new ClientboundLoginPacket(
                (int) packet.getRuntimeEntityId(), false,
                TranslatorUtils.translateGamemodeToJE(packet.getPlayerGameType()),
                TranslatorUtils.translateGamemodeToJE(packet.getPlayerGameType()),
                3, new String[]{"minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"}, ProxyServer.getInstance().getDimensionTag(),
                "minecraft:overworld", "minecraft:overworld", 100,
                10, 16, 16, false, true, false, false, null
        );

        player.getJavaSession().send(serverJoinGamePacket);

        Vector3f position = packet.getPlayerPosition();
        Vector2f rotation = packet.getRotation();
        ClientboundPlayerPositionPacket serverPlayerPositionRotationPacket = new ClientboundPlayerPositionPacket(position.getX(), position.getY(), position.getZ(), rotation.getY(), rotation.getX(), 0, false);
        player.getJavaSession().send(serverPlayerPositionRotationPacket);
        player.getJavaSession().send(new ClientboundEntityEventPacket((int) packet.getRuntimeEntityId(), EntityEvent.PLAYER_OP_PERMISSION_LEVEL_0));
    }
}
