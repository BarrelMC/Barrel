/*
 * Copyright (c) 2021 BarrelMC Team
 * This project is licensed under the MIT License
 */

package org.barrelmc.barrel.network;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.mc.protocol.packet.login.serverbound.ServerboundHelloPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import org.barrelmc.barrel.auth.AuthManager;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.server.ProxyServer;

import java.util.UUID;

public class JavaPacketHandler extends SessionAdapter {

    private Player player = null;

    @Override
    public void packetSent(Session session, Packet packet) {
        //System.out.println("Sent Java " + packet.toString());
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        //System.out.println("Received Java " + packet.toString());
        if (this.player == null) {
            if (packet instanceof ServerboundHelloPacket) {
                ServerboundHelloPacket loginPacket = (ServerboundHelloPacket) packet;

                if (!ProxyServer.getInstance().getConfig().getAuth().equals("offline") && AuthManager.getInstance().getAccessTokens().containsKey(loginPacket.getUsername())) {
                    new Player(loginPacket, session);

                    UUID uuid = UUID.nameUUIDFromBytes((loginPacket.getUsername()).getBytes());
                    GameProfile gameProfile = new GameProfile(uuid, loginPacket.getUsername());
                    session.setFlag(MinecraftConstants.PROFILE_KEY, gameProfile);

                    this.player = ProxyServer.getInstance().getPlayerByName(loginPacket.getUsername());
                }
            }
        } else {
            player.getPacketTranslatorManager().translate((MinecraftPacket) packet);
        }
    }
}
