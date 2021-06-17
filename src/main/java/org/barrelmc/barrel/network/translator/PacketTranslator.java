/*
 * Copyright (c) 2021 BarrelMC Team
 * This project is licensed under the MIT License
 */

package org.barrelmc.barrel.network.translator;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.data.game.ClientRequest;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntryAction;
import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.data.game.entity.EntityStatus;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.mc.protocol.data.game.entity.player.Animation;
import com.github.steveice10.mc.protocol.data.game.entity.player.BlockBreakStage;
import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.github.steveice10.mc.protocol.data.game.scoreboard.ObjectiveAction;
import com.github.steveice10.mc.protocol.data.game.scoreboard.ScoreType;
import com.github.steveice10.mc.protocol.data.game.scoreboard.ScoreboardPosition;
import com.github.steveice10.mc.protocol.data.game.world.block.value.ChestValue;
import com.github.steveice10.mc.protocol.data.game.world.block.value.ChestValueType;
import com.github.steveice10.mc.protocol.data.game.world.notify.ClientNotification;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientRequestPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientSettingsPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.*;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPlayerPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.scoreboard.ServerDisplayScoreboardPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.scoreboard.ServerScoreboardObjectivePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.scoreboard.ServerUpdateScorePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.*;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.packetlib.packet.Packet;
import com.nimbusds.jwt.SignedJWT;
import com.nukkitx.math.vector.Vector2f;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.network.VarInts;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.data.LevelEventType;
import com.nukkitx.protocol.bedrock.data.PlayerActionType;
import com.nukkitx.protocol.bedrock.data.ScoreInfo;
import com.nukkitx.protocol.bedrock.data.entity.EntityData;
import com.nukkitx.protocol.bedrock.packet.*;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import org.barrelmc.barrel.network.converter.BlockConverter;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.server.ProxyServer;
import org.barrelmc.barrel.utils.Utils;
import org.barrelmc.barrel.utils.nukkit.BitArray;
import org.barrelmc.barrel.utils.nukkit.BitArrayVersion;

import javax.crypto.SecretKey;
import java.net.URI;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class PacketTranslator {

    public static void translateToJava(BedrockPacket pk, Player player) {
        // Login Process Start
        // ------------------- Server To Client Handshake Packet
        if (pk instanceof ServerToClientHandshakePacket) {
            try {
                SignedJWT saltJwt = SignedJWT.parse(((ServerToClientHandshakePacket) pk).getJwt());
                URI x5u = saltJwt.getHeader().getX509CertURL();
                ECPublicKey serverKey = EncryptionUtils.generateKey(x5u.toASCIIString());
                SecretKey key = EncryptionUtils.getSecretKey(
                        player.getPrivateKey(),
                        serverKey,
                        Base64.getDecoder().decode(saltJwt.getJWTClaimsSet().getStringClaim("salt"))
                );
                player.getBedrockClient().getSession().enableEncryption(key);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            ClientToServerHandshakePacket clientToServerHandshake = new ClientToServerHandshakePacket();
            player.getBedrockClient().getSession().sendPacketImmediately(clientToServerHandshake);
        }

        // ------------------- Resource Packs Info Packet
        if (pk instanceof ResourcePacksInfoPacket) {
            ResourcePackClientResponsePacket response = new ResourcePackClientResponsePacket();
            response.setStatus(ResourcePackClientResponsePacket.Status.HAVE_ALL_PACKS);
            player.getBedrockClient().getSession().sendPacketImmediately(response);
        }

        // ------------------- Resource Pack Stack Packet
        if (pk instanceof ResourcePackStackPacket) {
            ResourcePackClientResponsePacket response = new ResourcePackClientResponsePacket();
            response.setStatus(ResourcePackClientResponsePacket.Status.COMPLETED);
            player.getBedrockClient().getSession().sendPacketImmediately(response);
        }

        // ------------------- Start Game Packet
        if (pk instanceof StartGamePacket) {
            StartGamePacket packet = (StartGamePacket) pk;
            ServerEntityStatusPacket serverEntityStatusPacket = new ServerEntityStatusPacket((int) packet.getRuntimeEntityId(), EntityStatus.PLAYER_OP_PERMISSION_LEVEL_0);

            ServerJoinGamePacket serverJoinGamePacket = new ServerJoinGamePacket(
                    (int) packet.getRuntimeEntityId(), false,
                    TranslatorUtils.translateGamemodeToJE(packet.getPlayerGameType()),
                    TranslatorUtils.translateGamemodeToJE(packet.getPlayerGameType()),
                    1, new String[]{"minecraft:world"}, ProxyServer.getInstance().getDimensionTag(),
                    ProxyServer.getInstance().getOverworldTag(), "minecraft:world", 100,
                    0, 16, false, true, false, false
            );

            Vector3f position = packet.getPlayerPosition();
            Vector2f rotation = packet.getRotation();
            ServerPlayerPositionRotationPacket serverPlayerPositionRotationPacket = new ServerPlayerPositionRotationPacket(position.getX(), position.getY(), position.getZ(), rotation.getY(), rotation.getX(), 1);

            player.getJavaSession().send(serverJoinGamePacket);
            player.getJavaSession().send(serverEntityStatusPacket);
            player.getJavaSession().send(serverPlayerPositionRotationPacket);

            SetLocalPlayerAsInitializedPacket setLocalPlayerAsInitializedPacket = new SetLocalPlayerAsInitializedPacket();
            setLocalPlayerAsInitializedPacket.setRuntimeEntityId(packet.getRuntimeEntityId());
            player.getBedrockClient().getSession().sendPacket(setLocalPlayerAsInitializedPacket);

            player.setRuntimeEntityId((int) packet.getRuntimeEntityId());
        }
        // Login process end

        if (pk instanceof AddPlayerPacket) {
            AddPlayerPacket packet = (AddPlayerPacket) pk;

            GameProfile gameProfile = new GameProfile(packet.getUuid(), Utils.lengthCutter(packet.getUsername(), 16));
            player.getJavaSession().send(new ServerPlayerListEntryPacket(PlayerListEntryAction.ADD_PLAYER, new PlayerListEntry[]{new PlayerListEntry(gameProfile, GameMode.SURVIVAL, 10, Component.text(Utils.lengthCutter(packet.getMetadata().getString(EntityData.NAMETAG), 16)))}));

            Vector3f position = packet.getPosition();
            Vector3f rotation = packet.getRotation();

            player.getJavaSession().send(new ServerSpawnPlayerPacket((int) packet.getRuntimeEntityId(), packet.getUuid(), position.getX(), position.getY(), position.getZ(), rotation.getY(), rotation.getX()));
        }

        if (pk instanceof SetTimePacket) {
            player.getJavaSession().send(new ServerUpdateTimePacket(0, ((SetTimePacket) pk).getTime()));
        }

        if (pk instanceof RemoveEntityPacket) {
            RemoveEntityPacket packet = (RemoveEntityPacket) pk;

            int[] entityIds = new int[1];
            entityIds[0] = (int) packet.getUniqueEntityId();
            player.getJavaSession().send(new ServerEntityDestroyPacket(entityIds));
        }

        if (pk instanceof SetScorePacket) {
            SetScorePacket packet = (SetScorePacket) pk;
            List<ScoreInfo> infos = packet.getInfos();
            for (ScoreInfo scoreInfo : infos) {
                int score = scoreInfo.getScore();
                if (player.getScoreSortorder() == 0) {
                    score = -score;
                }
                player.getJavaSession().send(new ServerUpdateScorePacket(Utils.lengthCutter(scoreInfo.getName(), 40), scoreInfo.getObjectiveId(), score));
            }
        }

        if (pk instanceof SetDisplayObjectivePacket) {
            SetDisplayObjectivePacket packet = (SetDisplayObjectivePacket) pk;

            String name = Utils.lengthCutter(packet.getDisplayName(), 32);

            if ("sidebar".equals(packet.getDisplaySlot())) {
                player.setScoreSortorder(packet.getSortOrder());
                player.getJavaSession().send(new ServerScoreboardObjectivePacket(packet.getObjectiveId(), ObjectiveAction.ADD, Component.text(name), ScoreType.INTEGER));
                player.getJavaSession().send(new ServerDisplayScoreboardPacket(ScoreboardPosition.SIDEBAR, packet.getObjectiveId()));
            }
        }

        if (pk instanceof RemoveObjectivePacket) {
            RemoveObjectivePacket packet = (RemoveObjectivePacket) pk;
            player.getJavaSession().send(new ServerScoreboardObjectivePacket(packet.getObjectiveId()));
        }

        if (pk instanceof MovePlayerPacket) {
            MovePlayerPacket packet = (MovePlayerPacket) pk;
            Vector3f position = packet.getPosition(), rotation = packet.getRotation();

            if (packet.getRuntimeEntityId() == player.getRuntimeEntityId()) {
                player.getJavaSession().send(new ServerPlayerPositionRotationPacket(position.getX(), position.getY() - 1.62, position.getZ(), rotation.getY(), rotation.getX(), 1));
                player.setPosition(position.getX(), position.getY() - 1.62, position.getZ());
            } else {
                player.getJavaSession().send(new ServerEntityTeleportPacket((int) packet.getRuntimeEntityId(), position.getX(), position.getY() - 1.62F, position.getZ(), rotation.getY(), rotation.getX(), packet.isOnGround()));
                player.getJavaSession().send(new ServerEntityHeadLookPacket((int) packet.getRuntimeEntityId(), rotation.getZ()));
            }
        }

        if (pk instanceof MoveEntityAbsolutePacket) {
            MoveEntityAbsolutePacket packet = (MoveEntityAbsolutePacket) pk;
            Vector3f position = packet.getPosition(), rotation = packet.getRotation();

            player.getJavaSession().send(new ServerEntityTeleportPacket((int) packet.getRuntimeEntityId(), position.getX(), position.getY() - 1.62F, position.getZ(), rotation.getY(), rotation.getX(), packet.isOnGround()));
            player.getJavaSession().send(new ServerEntityHeadLookPacket((int) packet.getRuntimeEntityId(), rotation.getZ()));
        }

        if (pk instanceof UpdatePlayerGameTypePacket) {
            UpdatePlayerGameTypePacket packet = (UpdatePlayerGameTypePacket) pk;

            if (packet.getEntityId() == player.getRuntimeEntityId()) {
                player.getJavaSession().send(new ServerNotifyClientPacket(ClientNotification.CHANGE_GAMEMODE, TranslatorUtils.translateGamemodeToJE(packet.getGameType())));
            }
        }

        if (pk instanceof AnimatePacket) {
            AnimatePacket packet = (AnimatePacket) pk;

            switch (packet.getAction()) {
                case SWING_ARM: {
                    player.getJavaSession().send(new ServerEntityAnimationPacket((int) packet.getRuntimeEntityId(), Animation.SWING_ARM));
                    break;
                }
                case WAKE_UP: {
                    player.getJavaSession().send(new ServerEntityAnimationPacket((int) packet.getRuntimeEntityId(), Animation.LEAVE_BED));
                    break;
                }
                case CRITICAL_HIT: {
                    player.getJavaSession().send(new ServerEntityAnimationPacket((int) packet.getRuntimeEntityId(), Animation.CRITICAL_HIT));
                    break;
                }
                case MAGIC_CRITICAL_HIT: {
                    player.getJavaSession().send(new ServerEntityAnimationPacket((int) packet.getRuntimeEntityId(), Animation.ENCHANTMENT_CRITICAL_HIT));
                    break;
                }
            }
        }

        if (pk instanceof LevelChunkPacket) {
            LevelChunkPacket packet = (LevelChunkPacket) pk;

            Chunk[] chunkSections = new Chunk[16];

            ByteBuf byteBuf = Unpooled.buffer();
            byteBuf.writeBytes(packet.getData());

            for (int sectionIndex = 0; sectionIndex < packet.getSubChunksLength(); sectionIndex++) {
                chunkSections[sectionIndex] = new Chunk();
                int chunkVersion = byteBuf.readByte();
                if (chunkVersion != 1 && chunkVersion != 8) {
                    TranslatorUtils.manage0VersionChunk(byteBuf, chunkSections[sectionIndex]);
                    continue;
                }

                byte storageSize = chunkVersion == 1 ? 1 : byteBuf.readByte();

                for (int storageReadIndex = 0; storageReadIndex < storageSize; storageReadIndex++) {
                    byte paletteHeader = byteBuf.readByte();
                    int paletteVersion = (paletteHeader | 1) >> 1;

                    BitArrayVersion bitArrayVersion = BitArrayVersion.get(paletteVersion, true);

                    int maxBlocksInSection = 4096; // 16*16*16
                    BitArray bitArray = bitArrayVersion.createPalette(maxBlocksInSection);
                    int wordsSize = bitArrayVersion.getWordsForSize(maxBlocksInSection);

                    for (int wordIterationIndex = 0; wordIterationIndex < wordsSize; wordIterationIndex++) {
                        int word = byteBuf.readIntLE();
                        bitArray.getWords()[wordIterationIndex] = word;
                    }

                    int paletteSize = VarInts.readInt(byteBuf);
                    int[] sectionPalette = new int[paletteSize];
                    for (int i = 0; i < paletteSize; i++) {
                        sectionPalette[i] = VarInts.readInt(byteBuf);
                    }

                    if (storageReadIndex == 0) {
                        int index = 0;
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                for (int y = 0; y < 16; y++) {
                                    int paletteIndex = bitArray.get(index);
                                    int mcbeBlockId = sectionPalette[paletteIndex];
                                    if (mcbeBlockId != 0) {
                                        chunkSections[sectionIndex].set(x, y, z, BlockConverter.toJavaStateId(mcbeBlockId));
                                    }
                                    index++;
                                }
                            }
                        }
                    }
                }
            }

            CompoundTag heightMap = new CompoundTag("MOTION_BLOCKING");

            ServerChunkDataPacket chunkPacket = new ServerChunkDataPacket(new Column(packet.getChunkX(), packet.getChunkZ(), chunkSections, new CompoundTag[0], heightMap, new int[1024]));
            player.getJavaSession().send(chunkPacket);
        }

        if (pk instanceof LevelEventPacket) {
            LevelEventPacket packet = (LevelEventPacket) pk;

            if (packet.getType() == LevelEventType.BLOCK_START_BREAK) {
                Vector3f pos = packet.getPosition();
                player.getJavaSession().send(new ServerBlockBreakAnimPacket(0, new Position((int) pos.getX(), (int) pos.getY(), (int) pos.getZ()), BlockBreakStage.STAGE_1));
            } else if (packet.getType() == LevelEventType.BLOCK_STOP_BREAK) {
                Vector3f pos = packet.getPosition();
                player.getJavaSession().send(new ServerBlockBreakAnimPacket(0, new Position((int) pos.getX(), (int) pos.getY(), (int) pos.getZ()), BlockBreakStage.RESET));
            }
        }

        if (pk instanceof BlockEventPacket) {
            BlockEventPacket packet = (BlockEventPacket) pk;

            if (packet.getEventType() == 1) {
                Vector3i pos = packet.getBlockPosition();
                Position blockPos = new Position(pos.getX(), pos.getY(), pos.getZ());
                if (packet.getEventData() == 2) {
                    player.getJavaSession().send(new ServerBlockValuePacket(blockPos, ChestValueType.VIEWING_PLAYER_COUNT, new ChestValue(1), 54));
                } else {
                    player.getJavaSession().send(new ServerBlockValuePacket(blockPos, ChestValueType.VIEWING_PLAYER_COUNT, new ChestValue(0), 54));
                }
            }
        }

        if (pk instanceof TakeItemEntityPacket) {
            TakeItemEntityPacket packet = (TakeItemEntityPacket) pk;

            int[] entityIds = new int[1];
            entityIds[0] = (int) packet.getItemRuntimeEntityId();

            player.getJavaSession().send(new ServerEntityDestroyPacket(entityIds));
            player.getJavaSession().send(new ServerEntityCollectItemPacket((int) packet.getItemRuntimeEntityId(), (int) packet.getRuntimeEntityId(), 1));
        }

        if (pk instanceof PlayerListPacket) {
            PlayerListPacket packet = (PlayerListPacket) pk;
            ArrayList<PlayerListEntry> playerListEntries = new ArrayList<>();

            for (PlayerListPacket.Entry entry : packet.getEntries()) {
                GameProfile gameProfile = new GameProfile(entry.getUuid(), Utils.lengthCutter(entry.getName(), 16));
                playerListEntries.add(new PlayerListEntry(gameProfile, GameMode.SURVIVAL, 0, Component.text(Utils.lengthCutter(entry.getName(), 16))));
            }

            PlayerListEntry[] playerListEntriesL = playerListEntries.toArray(new PlayerListEntry[0]);
            switch (packet.getAction()) {
                case ADD: {
                    player.getJavaSession().send(new ServerPlayerListEntryPacket(PlayerListEntryAction.ADD_PLAYER, playerListEntriesL));
                    break;
                }
                case REMOVE: {
                    player.getJavaSession().send(new ServerPlayerListEntryPacket(PlayerListEntryAction.REMOVE_PLAYER, playerListEntriesL));
                    break;
                }
            }
        }

        if (pk instanceof TextPacket) {
            TextPacket packet = (TextPacket) pk;

            player.sendMessage(packet.getMessage());
        }
    }

    public static void translateToBedrock(Packet pk, Player player) {
        if (pk instanceof ClientPlayerStatePacket) {
            ClientPlayerStatePacket packet = (ClientPlayerStatePacket) pk;
            switch (packet.getState()) {
                case START_SNEAKING: {
                    PlayerActionPacket playerActionPacket = new PlayerActionPacket();
                    playerActionPacket.setAction(PlayerActionType.START_SNEAK);
                    playerActionPacket.setBlockPosition(Vector3i.ZERO);
                    playerActionPacket.setFace(0);
                    playerActionPacket.setRuntimeEntityId(player.getRuntimeEntityId());
                    player.getBedrockClient().getSession().sendPacket(playerActionPacket);
                    break;
                }
                case STOP_SNEAKING: {
                    PlayerActionPacket playerActionPacket = new PlayerActionPacket();
                    playerActionPacket.setAction(PlayerActionType.STOP_SNEAK);
                    playerActionPacket.setBlockPosition(Vector3i.ZERO);
                    playerActionPacket.setFace(0);
                    playerActionPacket.setRuntimeEntityId(player.getRuntimeEntityId());
                    player.getBedrockClient().getSession().sendPacket(playerActionPacket);
                    break;
                }
                case START_SPRINTING: {
                    PlayerActionPacket playerActionPacket = new PlayerActionPacket();
                    playerActionPacket.setAction(PlayerActionType.START_SPRINT);
                    playerActionPacket.setBlockPosition(Vector3i.ZERO);
                    playerActionPacket.setFace(0);
                    playerActionPacket.setRuntimeEntityId(player.getRuntimeEntityId());
                    player.getBedrockClient().getSession().sendPacket(playerActionPacket);
                    break;
                }
                case STOP_SPRINTING: {
                    PlayerActionPacket playerActionPacket = new PlayerActionPacket();
                    playerActionPacket.setAction(PlayerActionType.STOP_SPRINT);
                    playerActionPacket.setBlockPosition(Vector3i.ZERO);
                    playerActionPacket.setFace(0);
                    playerActionPacket.setRuntimeEntityId(player.getRuntimeEntityId());
                    player.getBedrockClient().getSession().sendPacket(playerActionPacket);
                    break;
                }
            }
        }

        if (pk instanceof ClientPlayerSwingArmPacket) {
            AnimatePacket animatePacket = new AnimatePacket();

            animatePacket.setAction(AnimatePacket.Action.SWING_ARM);
            animatePacket.setRuntimeEntityId(player.getRuntimeEntityId());
            player.getBedrockClient().getSession().sendPacket(animatePacket);
        }

        if (pk instanceof ClientChatPacket) {
            ClientChatPacket chatPacket = (ClientChatPacket) pk;
            TextPacket textPacket = new TextPacket();

            textPacket.setType(TextPacket.Type.CHAT);
            textPacket.setNeedsTranslation(false);
            textPacket.setSourceName(chatPacket.getMessage());
            textPacket.setMessage(chatPacket.getMessage());
            textPacket.setXuid("");
            textPacket.setPlatformChatId("");
            player.getBedrockClient().getSession().sendPacket(textPacket);
        }

        if (pk instanceof ClientPlayerChangeHeldItemPacket) {
            ClientPlayerChangeHeldItemPacket packet = (ClientPlayerChangeHeldItemPacket) pk;
            PlayerHotbarPacket playerHotbarPacket = new PlayerHotbarPacket();

            playerHotbarPacket.setContainerId(0);
            playerHotbarPacket.setSelectedHotbarSlot(packet.getSlot());
            playerHotbarPacket.setSelectHotbarSlot(true);
            player.getBedrockClient().getSession().sendPacket(playerHotbarPacket);
        }

        if (pk instanceof ClientSettingsPacket) {
            ClientSettingsPacket settingsPacket = (ClientSettingsPacket) pk;
            RequestChunkRadiusPacket chunkRadiusPacket = new RequestChunkRadiusPacket();

            chunkRadiusPacket.setRadius(settingsPacket.getRenderDistance());
            player.getBedrockClient().getSession().sendPacket(chunkRadiusPacket);
        }

        if (pk instanceof ClientRequestPacket) {
            ClientRequestPacket packet = (ClientRequestPacket) pk;

            if (packet.getRequest() == ClientRequest.RESPAWN) {
                RespawnPacket respawnPacket = new RespawnPacket();

                respawnPacket.setPosition(Vector3f.from(0, 0, 0));
                respawnPacket.setRuntimeEntityId(player.getRuntimeEntityId());
                respawnPacket.setState(RespawnPacket.State.CLIENT_READY);
                player.getBedrockClient().getSession().sendPacket(respawnPacket);
            }
        }

        if (pk instanceof ClientPlayerRotationPacket) {
            ClientPlayerRotationPacket packet = (ClientPlayerRotationPacket) pk;
            MovePlayerPacket movePlayerPacket = new MovePlayerPacket();

            movePlayerPacket.setMode(MovePlayerPacket.Mode.HEAD_ROTATION);
            movePlayerPacket.setOnGround(packet.isOnGround());
            movePlayerPacket.setRuntimeEntityId(player.getRuntimeEntityId());
            movePlayerPacket.setRidingRuntimeEntityId(0);
            movePlayerPacket.setPosition(player.getVector3f());
            movePlayerPacket.setRotation(Vector3f.from(packet.getPitch(), packet.getYaw(), 0));
            movePlayerPacket.setTeleportationCause(MovePlayerPacket.TeleportationCause.UNKNOWN);
            movePlayerPacket.setEntityType(0);

            player.setRotation(packet.getYaw(), packet.getPitch());
            player.getBedrockClient().getSession().sendPacket(movePlayerPacket);
        }

        if (pk instanceof ClientPlayerPositionRotationPacket) {
            ClientPlayerPositionRotationPacket packet = (ClientPlayerPositionRotationPacket) pk;
            MovePlayerPacket movePlayerPacket = new MovePlayerPacket();

            movePlayerPacket.setRuntimeEntityId(player.getRuntimeEntityId());
            movePlayerPacket.setPosition(player.getVector3f());
            movePlayerPacket.setRotation(Vector3f.from(packet.getPitch(), packet.getYaw(), packet.getYaw()));
            movePlayerPacket.setMode(MovePlayerPacket.Mode.NORMAL);
            movePlayerPacket.setOnGround(packet.isOnGround());
            movePlayerPacket.setRidingRuntimeEntityId(0);
            movePlayerPacket.setTeleportationCause(MovePlayerPacket.TeleportationCause.UNKNOWN);
            movePlayerPacket.setEntityType(0);

            player.setLocation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch());
            player.getBedrockClient().getSession().sendPacket(movePlayerPacket);
        }

        if (pk instanceof ClientPlayerPositionPacket) {
            ClientPlayerPositionPacket packet = (ClientPlayerPositionPacket) pk;
            MovePlayerPacket movePlayerPacket = new MovePlayerPacket();

            movePlayerPacket.setRuntimeEntityId(player.getRuntimeEntityId());
            movePlayerPacket.setPosition(player.getVector3f());
            movePlayerPacket.setRotation(Vector3f.from(player.getPitch(), player.getYaw(), player.getYaw()));
            movePlayerPacket.setMode(MovePlayerPacket.Mode.NORMAL);
            movePlayerPacket.setOnGround(packet.isOnGround());
            movePlayerPacket.setRidingRuntimeEntityId(0);
            movePlayerPacket.setTeleportationCause(MovePlayerPacket.TeleportationCause.UNKNOWN);
            movePlayerPacket.setEntityType(0);

            player.setPosition(packet.getX(), packet.getY(), packet.getZ());
            player.getBedrockClient().getSession().sendPacket(movePlayerPacket);
        }
    }
}
