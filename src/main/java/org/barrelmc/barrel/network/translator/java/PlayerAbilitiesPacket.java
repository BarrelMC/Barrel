package org.barrelmc.barrel.network.translator.java;

import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.ServerboundPlayerAbilitiesPacket;
import com.nukkitx.protocol.bedrock.data.Ability;
import com.nukkitx.protocol.bedrock.data.AbilityType;
import com.nukkitx.protocol.bedrock.packet.RequestAbilityPacket;
import org.barrelmc.barrel.network.translator.interfaces.JavaPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class PlayerAbilitiesPacket implements JavaPacketTranslator {
    @Override
    public void translate(MinecraftPacket pk, Player player) {
        ServerboundPlayerAbilitiesPacket packet = (ServerboundPlayerAbilitiesPacket) pk;

        RequestAbilityPacket requestAbilityPacket = new RequestAbilityPacket();
        requestAbilityPacket.setAbility(Ability.FLYING);
        requestAbilityPacket.setType(AbilityType.BOOLEAN);
        requestAbilityPacket.setBoolValue(packet.isFlying());
        requestAbilityPacket.setFloatValue(0.0f);
        player.getBedrockClient().getSession().sendPacket(requestAbilityPacket);
    }
}
