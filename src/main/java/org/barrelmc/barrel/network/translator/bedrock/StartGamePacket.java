package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.nukkitx.math.vector.Vector2f;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.packet.SetLocalPlayerAsInitializedPacket;
import org.barrelmc.barrel.network.translator.TranslatorUtils;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.server.ProxyServer;

public class StartGamePacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.StartGamePacket packet = (com.nukkitx.protocol.bedrock.packet.StartGamePacket) pk;

        ServerJoinGamePacket serverJoinGamePacket = new ServerJoinGamePacket(
                (int) packet.getRuntimeEntityId(), false,
                TranslatorUtils.translateGamemodeToJE(packet.getPlayerGameType()),
                TranslatorUtils.translateGamemodeToJE(packet.getPlayerGameType()),
                1, new String[]{"minecraft:world"}, ProxyServer.getInstance().getDimensionTag(),
                ProxyServer.getInstance().getOverworldTag(), "minecraft:world", 100,
                0, 16, false, true, false, false
        );

        Vector3f position = packet.getPlayerPosition();
        Vector2f rotation = packet.getRotation();
        ServerPlayerPositionRotationPacket serverPlayerPositionRotationPacket = new ServerPlayerPositionRotationPacket(position.getX(), position.getY(), position.getZ(), rotation.getY(), rotation.getX(), 1);

        player.getJavaSession().send(serverJoinGamePacket);
        player.getJavaSession().send(serverPlayerPositionRotationPacket);

        SetLocalPlayerAsInitializedPacket setLocalPlayerAsInitializedPacket = new SetLocalPlayerAsInitializedPacket();
        setLocalPlayerAsInitializedPacket.setRuntimeEntityId(packet.getRuntimeEntityId());
        player.getBedrockClient().getSession().sendPacket(setLocalPlayerAsInitializedPacket);

        player.setRuntimeEntityId((int) packet.getRuntimeEntityId());
    }
}
