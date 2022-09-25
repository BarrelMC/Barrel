/*
 * Copyright (c) 2021 BarrelMC Team
 * This project is licensed under the MIT License
 */

package org.barrelmc.barrel.network;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.packet.login.client.LoginStartPacket;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import org.barrelmc.barrel.auth.AuthManager;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.server.ProxyServer;

import java.util.UUID;

public class JavaPacketHandler extends SessionAdapter {

    private Player player = null;

    @Override
    public void packetReceived(PacketReceivedEvent event) {
        //System.out.println("Received Java " + event.getPacket().toString());
        if (this.player == null) {
            if (event.getPacket() instanceof LoginStartPacket) {
                LoginStartPacket loginPacket = event.getPacket();

                if (!ProxyServer.getInstance().getConfig().getAuth().equals("offline") && AuthManager.getInstance().getAccessTokens().containsKey(loginPacket.getUsername())) {
                    new Player(loginPacket, event.getSession());

                    UUID uuid = UUID.nameUUIDFromBytes((loginPacket.getUsername()).getBytes());
                    GameProfile gameProfile = new GameProfile(uuid, loginPacket.getUsername());
                    event.getSession().setFlag(MinecraftConstants.PROFILE_KEY, gameProfile);

                    this.player = ProxyServer.getInstance().getPlayerByName(loginPacket.getUsername());
                }
            }
        } else {
            player.getPacketTranslatorManager().translate((Packet) event.getPacket());
        }
    }
}
