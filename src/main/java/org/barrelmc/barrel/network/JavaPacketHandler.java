/*
 * Copyright (c) 2021 BarrelMC
 * BarrelMC/Barrel is licensed under the MIT License
 */

package org.barrelmc.barrel.network;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.packet.login.client.LoginStartPacket;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import org.barrelmc.barrel.network.translator.PacketTranslator;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.server.ProxyServer;

import java.util.UUID;

public class JavaPacketHandler extends SessionAdapter {

    private Player player = null;

    @Override
    public void packetReceived(PacketReceivedEvent event) {
        if (this.player == null) {
            if (event.getPacket() instanceof LoginStartPacket) {
                LoginStartPacket loginPacket = event.getPacket();
                new Player(loginPacket, event.getSession());

                UUID uuid = UUID.nameUUIDFromBytes((loginPacket.getUsername()).getBytes());
                GameProfile gameProfile = new GameProfile(uuid, loginPacket.getUsername());
                event.getSession().setFlag(MinecraftConstants.PROFILE_KEY, gameProfile);

                this.player = ProxyServer.getInstance().getPlayerByName(loginPacket.getUsername());
            }
        } else {
            PacketTranslator.translateToBedrock(event.getPacket(), this.player);
        }
    }
}
