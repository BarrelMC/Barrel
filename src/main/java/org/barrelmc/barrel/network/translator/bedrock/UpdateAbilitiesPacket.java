package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.data.game.level.notify.GameEvent;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerAbilitiesPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundGameEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.data.Ability;
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer;
import org.cloudburstmc.protocol.bedrock.data.GameType;
import org.barrelmc.barrel.network.translator.TranslatorUtils;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

import java.util.Set;

public class UpdateAbilitiesPacket implements BedrockPacketTranslator {
    @Override
    public void translate(BedrockPacket pk, Player player) {
        org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket packet = (org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket) pk;

        if (packet.getUniqueEntityId() == player.getRuntimeEntityId()) {
            for (AbilityLayer abilityLayer : packet.getAbilityLayers().toArray(new AbilityLayer[0])) {
                if (abilityLayer.getLayerType() == AbilityLayer.Type.BASE) {
                    Set<Ability> abilityValues = abilityLayer.getAbilityValues();
                    if (abilityValues.contains(Ability.NO_CLIP) && player.getGameMode() == GameType.CREATIVE) {
                        player.setGameMode(GameType.SPECTATOR);
                        player.getJavaSession().send(new ClientboundGameEventPacket(GameEvent.CHANGE_GAMEMODE, TranslatorUtils.translateGamemodeToJE(GameType.SPECTATOR)));
                    }
                    player.getJavaSession().send(new ClientboundPlayerAbilitiesPacket(abilityValues.contains(Ability.INVULNERABLE), abilityValues.contains(Ability.MAY_FLY), abilityValues.contains(Ability.FLYING), abilityValues.contains(Ability.INSTABUILD), 0.05f, 0.1f));
                }
            }
        }
    }
}
