package org.barrelmc.barrel.network.translator.java;

import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundSetCarriedItemPacket;
import com.nukkitx.protocol.bedrock.packet.PlayerHotbarPacket;
import org.barrelmc.barrel.network.translator.interfaces.JavaPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class SetCarriedItemPacket implements JavaPacketTranslator {

    @Override
    public void translate(MinecraftPacket pk, Player player) {
        ServerboundSetCarriedItemPacket packet = (ServerboundSetCarriedItemPacket) pk;

        player.setHotbarSlot(packet.getSlot());

        PlayerHotbarPacket playerHotbarPacket = new PlayerHotbarPacket();
        playerHotbarPacket.setContainerId(0);
        playerHotbarPacket.setSelectedHotbarSlot(packet.getSlot());
        playerHotbarPacket.setSelectHotbarSlot(true);
        player.getBedrockClient().getSession().sendPacket(playerHotbarPacket);
    }
}
