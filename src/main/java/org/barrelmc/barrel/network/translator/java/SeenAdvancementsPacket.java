package org.barrelmc.barrel.network.translator.java;

import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.mc.protocol.data.game.inventory.AdvancementTabAction;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundSelectAdvancementsTabPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.inventory.ServerboundSeenAdvancementsPacket;
import org.barrelmc.barrel.network.translator.interfaces.JavaPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class SeenAdvancementsPacket implements JavaPacketTranslator {
    @Override
    public void translate(MinecraftPacket pk, Player player) {
        ServerboundSeenAdvancementsPacket packet = (ServerboundSeenAdvancementsPacket) pk;

        if (packet.getAction() == AdvancementTabAction.OPENED_TAB) {
            player.getJavaSession().send(new ClientboundSelectAdvancementsTabPacket(packet.getTabId()));
        }
    }
}
