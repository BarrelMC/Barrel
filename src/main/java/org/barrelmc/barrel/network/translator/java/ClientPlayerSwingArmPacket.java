package org.barrelmc.barrel.network.translator.java;

import com.github.steveice10.packetlib.packet.Packet;
import com.nukkitx.protocol.bedrock.packet.AnimatePacket;
import org.barrelmc.barrel.network.translator.interfaces.JavaPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class ClientPlayerSwingArmPacket implements JavaPacketTranslator {

    @Override
    public void translate(Packet pk, Player player) {
        AnimatePacket animatePacket = new AnimatePacket();

        animatePacket.setAction(AnimatePacket.Action.SWING_ARM);
        animatePacket.setRuntimeEntityId(player.getRuntimeEntityId());
        player.getBedrockClient().getSession().sendPacket(animatePacket);
    }
}
