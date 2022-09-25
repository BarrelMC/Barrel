package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.data.game.world.notify.ClientNotification;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerAbilitiesPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerNotifyClientPacket;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.data.Ability;
import com.nukkitx.protocol.bedrock.data.AbilityLayer;
import com.nukkitx.protocol.bedrock.data.GameType;
import org.barrelmc.barrel.network.translator.TranslatorUtils;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

import java.util.Set;

public class UpdateAbilitiesPacket implements BedrockPacketTranslator {
    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.UpdateAbilitiesPacket packet = (com.nukkitx.protocol.bedrock.packet.UpdateAbilitiesPacket) pk;

        if (packet.getUniqueEntityId() == player.getRuntimeEntityId()) {
            for (AbilityLayer abilityLayer : packet.getAbilityLayers().toArray(new AbilityLayer[0])) {
                if (abilityLayer.getLayerType() == AbilityLayer.Type.BASE) {
                    Set<Ability> abilityValues = abilityLayer.getAbilityValues();
                    if (abilityValues.contains(Ability.NO_CLIP) && player.getGameMode() == GameType.CREATIVE) {
                        player.setGameMode(GameType.SURVIVAL_VIEWER);
                        player.getJavaSession().send(new ServerNotifyClientPacket(ClientNotification.CHANGE_GAMEMODE, TranslatorUtils.translateGamemodeToJE(GameType.SURVIVAL_VIEWER)));
                    }
                    player.getJavaSession().send(new ServerPlayerAbilitiesPacket(abilityValues.contains(Ability.INVULNERABLE), abilityValues.contains(Ability.MAY_FLY), abilityValues.contains(Ability.FLYING), abilityValues.contains(Ability.INSTABUILD), 0.05f, 0.1f));
                }
            }
        }
    }
}
