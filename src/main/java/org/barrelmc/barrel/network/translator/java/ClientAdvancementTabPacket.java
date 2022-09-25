package org.barrelmc.barrel.network.translator.java;

import com.github.steveice10.mc.protocol.data.game.window.AdvancementTabAction;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerAdvancementTabPacket;
import com.github.steveice10.packetlib.packet.Packet;
import org.barrelmc.barrel.network.translator.interfaces.JavaPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class ClientAdvancementTabPacket implements JavaPacketTranslator {
    @Override
    public void translate(Packet pk, Player player) {
        com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientAdvancementTabPacket packet = (com.github.steveice10.mc.protocol.packet.ingame.client.window.ClientAdvancementTabPacket) pk;

        if (packet.getAction() == AdvancementTabAction.OPENED_TAB) {
            player.getJavaSession().send(new ServerAdvancementTabPacket(packet.getTabId()));
        }
    }
}
