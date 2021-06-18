package org.barrelmc.barrel.network.translator.interfaces;

import com.github.steveice10.packetlib.packet.Packet;
import org.barrelmc.barrel.player.Player;

public interface JavaPacketTranslator {

    void translate(Packet pk, Player player);
}
