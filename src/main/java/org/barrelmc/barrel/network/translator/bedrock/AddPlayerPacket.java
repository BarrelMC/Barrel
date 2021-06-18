package org.barrelmc.barrel.network.translator.bedrock;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntryAction;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.data.entity.EntityData;
import net.kyori.adventure.text.Component;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.utils.Utils;

public class AddPlayerPacket implements BedrockPacketTranslator {

    @Override
    public void translate(BedrockPacket pk, Player player) {
        com.nukkitx.protocol.bedrock.packet.AddPlayerPacket packet = (com.nukkitx.protocol.bedrock.packet.AddPlayerPacket) pk;

        Vector3f position = packet.getPosition();
        Vector3f rotation = packet.getRotation();

        GameProfile gameProfile = new GameProfile(packet.getUuid(), Utils.lengthCutter(packet.getUsername(), 16));

        player.getJavaSession().send(new ServerPlayerListEntryPacket(PlayerListEntryAction.ADD_PLAYER, new PlayerListEntry[]{new PlayerListEntry(gameProfile, GameMode.SURVIVAL, 10, Component.text(Utils.lengthCutter(packet.getMetadata().getString(EntityData.NAMETAG), 16)))}));
        player.getJavaSession().send(new ServerSpawnPlayerPacket((int) packet.getRuntimeEntityId(), packet.getUuid(), position.getX(), position.getY(), position.getZ(), rotation.getY(), rotation.getX()));
    }
}
