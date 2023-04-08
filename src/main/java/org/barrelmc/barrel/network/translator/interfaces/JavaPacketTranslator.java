package org.barrelmc.barrel.network.translator.interfaces;

import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import org.barrelmc.barrel.player.Player;

public interface JavaPacketTranslator {

    void translate(MinecraftPacket pk, Player player);
}
