package org.barrelmc.barrel.network.translator;

import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundClientCommandPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundClientInformationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.inventory.ServerboundSeenAdvancementsPacket;
import com.github.steveice10.mc.protocol.packet.ingame.serverbound.player.*;
import com.github.steveice10.packetlib.packet.Packet;
import org.cloudburstmc.protocol.bedrock.packet.*;
import lombok.Getter;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.network.translator.interfaces.JavaPacketTranslator;
import org.barrelmc.barrel.network.translator.java.*;
import org.barrelmc.barrel.network.translator.java.PlayerActionPacket;
import org.barrelmc.barrel.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PacketTranslatorManager {

    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(),
            Integer.MAX_VALUE,
            60,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Getter
    private final Map<Class<? extends Packet>, JavaPacketTranslator> javaTranslators = new HashMap<>();
    @Getter
    private final Map<Class<? extends BedrockPacket>, BedrockPacketTranslator> bedrockTranslators = new HashMap<>();

    private final Player player;

    public PacketTranslatorManager(Player player) {
        this.player = player;
        this.registerDefaultPackets();
    }

    public void translate(BedrockPacket pk) {
        BedrockPacketTranslator translator = bedrockTranslators.get(pk.getClass());

        if (translator != null) {
            if (translator.immediate()) {
                translator.translate(pk, player);
            } else {
                threadPoolExecutor.execute(() -> translator.translate(pk, player));
            }
        }
    }

    public void translate(MinecraftPacket pk) {
        JavaPacketTranslator translator = javaTranslators.get(pk.getClass());

        if (translator != null) {
            threadPoolExecutor.execute(() -> translator.translate(pk, player));
        }
    }

    private void registerDefaultPackets() {
        // Bedrock packets
        bedrockTranslators.put(AddPlayerPacket.class, new org.barrelmc.barrel.network.translator.bedrock.AddPlayerPacket());
        bedrockTranslators.put(AnimatePacket.class, new org.barrelmc.barrel.network.translator.bedrock.AnimatePacket());
        bedrockTranslators.put(BlockEventPacket.class, new org.barrelmc.barrel.network.translator.bedrock.BlockEventPacket());
        bedrockTranslators.put(LevelChunkPacket.class, new org.barrelmc.barrel.network.translator.bedrock.LevelChunkPacket());
        bedrockTranslators.put(LevelEventPacket.class, new org.barrelmc.barrel.network.translator.bedrock.LevelEventPacket());
        bedrockTranslators.put(MoveEntityAbsolutePacket.class, new org.barrelmc.barrel.network.translator.bedrock.MoveEntityAbsolutePacket());
        bedrockTranslators.put(MovePlayerPacket.class, new org.barrelmc.barrel.network.translator.bedrock.MovePlayerPacket());
        bedrockTranslators.put(PlayerListPacket.class, new org.barrelmc.barrel.network.translator.bedrock.PlayerListPacket());
        bedrockTranslators.put(RemoveEntityPacket.class, new org.barrelmc.barrel.network.translator.bedrock.RemoveEntityPacket());
        bedrockTranslators.put(RemoveObjectivePacket.class, new org.barrelmc.barrel.network.translator.bedrock.RemoveObjectivePacket());
        bedrockTranslators.put(ResourcePacksInfoPacket.class, new org.barrelmc.barrel.network.translator.bedrock.ResourcePacksInfoPacket());
        bedrockTranslators.put(ResourcePackStackPacket.class, new org.barrelmc.barrel.network.translator.bedrock.ResourcePackStackPacket());
        bedrockTranslators.put(ServerToClientHandshakePacket.class, new org.barrelmc.barrel.network.translator.bedrock.ServerToClientHandshakePacket());
        bedrockTranslators.put(SetDisplayObjectivePacket.class, new org.barrelmc.barrel.network.translator.bedrock.SetDisplayObjectivePacket());
        bedrockTranslators.put(SetScorePacket.class, new org.barrelmc.barrel.network.translator.bedrock.SetScorePacket());
        bedrockTranslators.put(SetTimePacket.class, new org.barrelmc.barrel.network.translator.bedrock.SetTimePacket());
        bedrockTranslators.put(StartGamePacket.class, new org.barrelmc.barrel.network.translator.bedrock.StartGamePacket());
        bedrockTranslators.put(TakeItemEntityPacket.class, new org.barrelmc.barrel.network.translator.bedrock.TakeItemEntityPacket());
        bedrockTranslators.put(TextPacket.class, new org.barrelmc.barrel.network.translator.bedrock.TextPacket());
        bedrockTranslators.put(PlayStatusPacket.class, new org.barrelmc.barrel.network.translator.bedrock.PlayStatusPacket());
        bedrockTranslators.put(UpdateBlockPacket.class, new org.barrelmc.barrel.network.translator.bedrock.UpdateBlockPacket());
        bedrockTranslators.put(DisconnectPacket.class, new org.barrelmc.barrel.network.translator.bedrock.DisconnectPacket());
        bedrockTranslators.put(SetPlayerGameTypePacket.class, new org.barrelmc.barrel.network.translator.bedrock.SetPlayerGameTypePacket());
        bedrockTranslators.put(ChangeDimensionPacket.class, new org.barrelmc.barrel.network.translator.bedrock.ChangeDimensionPacket());
        bedrockTranslators.put(SetEntityDataPacket.class, new org.barrelmc.barrel.network.translator.bedrock.SetEntityDataPacket());
        bedrockTranslators.put(SetSpawnPositionPacket.class, new org.barrelmc.barrel.network.translator.bedrock.SetSpawnPositionPacket());
        bedrockTranslators.put(UpdateAbilitiesPacket.class, new org.barrelmc.barrel.network.translator.bedrock.UpdateAbilitiesPacket());
        bedrockTranslators.put(NetworkSettingsPacket.class, new org.barrelmc.barrel.network.translator.bedrock.NetworkSettingsPacket());

        // Java packets
        javaTranslators.put(ServerboundChatPacket.class, new ChatPacket());
        javaTranslators.put(ServerboundSetCarriedItemPacket.class, new SetCarriedItemPacket());
        javaTranslators.put(ServerboundMovePlayerPosPacket.class, new MovePlayerPosPacket());
        javaTranslators.put(ServerboundMovePlayerPosRotPacket.class, new MovePlayerPosRotPacket());
        javaTranslators.put(ServerboundMovePlayerRotPacket.class, new MovePlayerRotPacket());
        javaTranslators.put(ServerboundPlayerCommandPacket.class, new PlayerCommandPacket());
        javaTranslators.put(ServerboundSwingPacket.class, new SwingPacket());
        javaTranslators.put(ServerboundClientCommandPacket.class, new ClientCommandPacket());
        javaTranslators.put(ServerboundClientInformationPacket.class, new ClientInformationPacket());
        javaTranslators.put(ServerboundPlayerActionPacket.class, new PlayerActionPacket());
        javaTranslators.put(ServerboundSeenAdvancementsPacket.class, new SeenAdvancementsPacket());
        javaTranslators.put(ServerboundPlayerAbilitiesPacket.class, new PlayerAbilitiesPacket());
    }
}
