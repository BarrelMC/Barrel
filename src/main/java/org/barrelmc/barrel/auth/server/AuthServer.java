/*
 * Copyright (c) 2021 BarrelMC Team
 * This project is licensed under the MIT License
 */

package org.barrelmc.barrel.auth.server;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerSpawnPositionPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import net.kyori.adventure.text.Component;
import org.barrelmc.barrel.auth.AuthManager;
import org.barrelmc.barrel.server.ProxyServer;

public class AuthServer extends SessionAdapter {

    private final String username;

    public AuthServer(Session session, String username) {
        this.username = username;
        session.send(new ServerJoinGamePacket(
                69420, false, GameMode.ADVENTURE, GameMode.ADVENTURE,
                1, new String[]{"minecraft:world"}, ProxyServer.getInstance().getDimensionTag(),
                ProxyServer.getInstance().getOverworldTag(), "minecraft:world", 100,
                0, 4, false, true, false, false
        ));

        session.send(new ServerSpawnPositionPacket(new Position(0, 82, 0)));
        session.send(new ServerPlayerPositionRotationPacket(0, 82, 0, 0, 0, 0));
        session.send(new ServerChatPacket(Component.text("§ePlease input your email and password.\n§aEx: account@mail.com:password123")));
    }

    @Override
    public void packetReceived(PacketReceivedEvent event) {
        if (event.getPacket() instanceof ClientChatPacket) {
            String messageStr = ((ClientChatPacket) event.getPacket()).getMessage();

            String[] message = messageStr.split(":");
            if (message.length != 2) {
                event.getSession().send(new ServerChatPacket(Component.text("§cWrong format")));
                return;
            }

            if (message[1].length() < 8) {
                event.getSession().send(new ServerChatPacket(Component.text("§cInvalid password length")));
                return;
            }

            event.getSession().send(new ServerChatPacket(Component.text("§eLogging in...")));

            try {
                String token = AuthManager.getInstance().getXboxLogin().getAccessToken(message[0], message[1]);
                AuthManager.getInstance().getAccessTokens().put(this.username, token);
                AuthManager.getInstance().getLoginPlayers().put(this.username, true);
            } catch (Exception e) {
                event.getSession().send(new ServerChatPacket(Component.text("§cLogin failed! Account or password invalid, please re-input the email and password")));
                return;
            }

            event.getSession().send(new ServerChatPacket(Component.text("§aLogin successfull! Please re-join.")));
        }
    }
}
