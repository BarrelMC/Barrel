package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntryAction;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundPlayerInfoPacket;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddPlayerPacket;
import net.kyori.adventure.text.Component;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.utils.Utils;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;

public class AddPlayerPacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket packet = (org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket) pk;

        Vector3f position = packet.getPosition();
        Vector3f rotation = packet.getRotation();

        GameProfile gameProfile = new GameProfile(packet.getUuid(), Utils.lengthCutter(packet.getUsername(), 16));

        player.getJavaSession().send(new ClientboundPlayerInfoPacket(PlayerListEntryAction.ADD_PLAYER, new PlayerListEntry[]{new PlayerListEntry(gameProfile, GameMode.SURVIVAL, 10, Component.text(Utils.lengthCutter(packet.getMetadata().get(EntityDataTypes.NAME), 16)), 0L, null, null)}));
        player.getJavaSession().send(new ClientboundAddPlayerPacket((int) packet.getRuntimeEntityId(), packet.getUuid(), position.getX(), position.getY(), position.getZ(), rotation.getY(), rotation.getX()));
    }
}
