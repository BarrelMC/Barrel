package org.barrelmc.barrel.network.translator;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientRequestPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientSettingsPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.*;
import com.github.steveice10.packetlib.packet.Packet;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.packet.*;
import lombok.Getter;
import org.barrelmc.barrel.network.translator.interfaces.BedrockPacketTranslator;
import org.barrelmc.barrel.network.translator.interfaces.JavaPacketTranslator;
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

    public void translate(Packet pk) {
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

        // Java packets
        javaTranslators.put(ClientChatPacket.class, new org.barrelmc.barrel.network.translator.java.ClientChatPacket());
        javaTranslators.put(ClientPlayerChangeHeldItemPacket.class, new org.barrelmc.barrel.network.translator.java.ClientPlayerChangeHeldItemPacket());
        javaTranslators.put(ClientPlayerPositionPacket.class, new org.barrelmc.barrel.network.translator.java.ClientPlayerPositionPacket());
        javaTranslators.put(ClientPlayerPositionRotationPacket.class, new org.barrelmc.barrel.network.translator.java.ClientPlayerPositionRotationPacket());
        javaTranslators.put(ClientPlayerRotationPacket.class, new org.barrelmc.barrel.network.translator.java.ClientPlayerRotationPacket());
        javaTranslators.put(ClientPlayerStatePacket.class, new org.barrelmc.barrel.network.translator.java.ClientPlayerStatePacket());
        javaTranslators.put(ClientPlayerSwingArmPacket.class, new org.barrelmc.barrel.network.translator.java.ClientPlayerSwingArmPacket());
        javaTranslators.put(ClientRequestPacket.class, new org.barrelmc.barrel.network.translator.java.ClientRequestPacket());
        javaTranslators.put(ClientSettingsPacket.class, new org.barrelmc.barrel.network.translator.java.ClientSettingsPacket());
    }
}
