package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.protocol.data.game.level.notify.GameEvent;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundGameEventPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.data.GameType;
import org.barrelmc.barrel.network.translator.TranslatorUtils;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;

public class SetPlayerGameTypePacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        org.cloudburstmc.protocol.bedrock.packet.SetPlayerGameTypePacket packet = (org.cloudburstmc.protocol.bedrock.packet.SetPlayerGameTypePacket) pk;

        player.setGameMode(GameType.from(packet.getGamemode()));
        player.getJavaSession().send(new ClientboundGameEventPacket(GameEvent.CHANGE_GAMEMODE, TranslatorUtils.translateGamemodeToJE(GameType.from(packet.getGamemode()))));
    }
}
