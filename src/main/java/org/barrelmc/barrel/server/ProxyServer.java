/*
 * Copyright (c) 2021 BarrelMC Team
 * This project is licensed under the MIT License
 */

package org.barrelmc.barrel.server;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.auth.service.SessionService;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.ServerLoginHandler;
import com.github.steveice10.mc.protocol.codec.MinecraftCodec;
import com.github.steveice10.mc.protocol.data.status.PlayerInfo;
import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo;
import com.github.steveice10.mc.protocol.data.status.VersionInfo;
import com.github.steveice10.mc.protocol.data.status.handler.ServerInfoBuilder;
import com.github.steveice10.opennbt.NBTIO;
import com.github.steveice10.opennbt.tag.builtin.*;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.event.server.ServerAdapter;
import com.github.steveice10.packetlib.event.server.ServerClosedEvent;
import com.github.steveice10.packetlib.event.server.SessionAddedEvent;
import com.github.steveice10.packetlib.event.server.SessionRemovedEvent;
import com.github.steveice10.packetlib.tcp.TcpServer;
import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.v560.Bedrock_v560;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.barrelmc.barrel.Barrel;
import org.barrelmc.barrel.auth.AuthManager;
import org.barrelmc.barrel.auth.server.AuthServer;
import org.barrelmc.barrel.config.Config;
import org.barrelmc.barrel.network.JavaPacketHandler;
import org.barrelmc.barrel.player.Player;
import org.barrelmc.barrel.utils.FileManager;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

public class ProxyServer {

    @Getter
    private static ProxyServer instance = null;
    @Getter
    private final Map<String, Player> onlinePlayers = new ConcurrentHashMap<>();
    @Getter
    private final BedrockPacketCodec bedrockPacketCodec = Bedrock_v560.V560_CODEC;

    @Getter
    private final Path dataPath;

    @Getter
    private Config config;

    @Getter
    private String defaultSkinData;
    @Getter
    private String defaultSkinGeometry;

    @Getter
    private final CompoundTag dimensionTag;

    public ProxyServer(String dataPath) {
        instance = this;
        this.dataPath = Paths.get(dataPath);
        if (!this.initConfig()) {
            System.out.println("Failed to read config file! Terminating...");
            System.exit(0);
        }

        try {
            this.dimensionTag = (CompoundTag) NBTIO.readTag(new GZIPInputStream(Objects.requireNonNull(Barrel.class.getClassLoader().getResourceAsStream("registry-codec.dat"))), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            defaultSkinData = FileManager.getFileContents(Objects.requireNonNull(Barrel.class.getClassLoader().getResourceAsStream("skin/skin_data.txt")));
            defaultSkinGeometry = FileManager.getFileContents(Objects.requireNonNull(Barrel.class.getClassLoader().getResourceAsStream("skin/skin_geometry.json")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.startServer();
    }

    private boolean initConfig() {
        Path outputFile = this.dataPath.resolve("config.yml");

        if (!Files.exists(outputFile)) {
            try {
                InputStream stream = Barrel.class.getClassLoader().getResourceAsStream("config.yml");
                if (stream == null) {
                    throw new IOException("Cannot get config from jar file.");
                }

                FileOutputStream resStreamOut = new FileOutputStream(outputFile.toFile());

                int readBytes;
                byte[] buffer = new byte[4096];
                while ((readBytes = stream.read(buffer)) > 0) {
                    resStreamOut.write(buffer, 0, readBytes);
                }

                stream.close();
                resStreamOut.close();
            } catch (IOException e) {
                return false;
            }
        }

        try {
            InputStream inputStream = new FileInputStream(outputFile.toFile());
            this.config = (new Yaml()).loadAs(inputStream, Config.class);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private void startServer() {
        SessionService sessionService = new SessionService();

        Server server = new TcpServer(this.config.getBindAddress(), this.config.getPort(), MinecraftProtocol::new);
        server.setGlobalFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);
        server.setGlobalFlag(MinecraftConstants.VERIFY_USERS_KEY, false);
        server.setGlobalFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY, (ServerInfoBuilder) session -> new ServerStatusInfo(new VersionInfo(MinecraftCodec.CODEC.getMinecraftVersion(), MinecraftCodec.CODEC.getProtocolVersion()), new PlayerInfo(10, 0, new GameProfile[0]), Component.text(this.config.getMotd()), null, false));
        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, (ServerLoginHandler) session -> {
            GameProfile profile = session.getFlag(MinecraftConstants.PROFILE_KEY);
            System.out.println(profile.getName() + " logged in");
            if (AuthManager.getInstance().getLoginPlayers().get(profile.getName()) == null) {
                session.addListener(new AuthServer(session, profile.getName()));
            }
        });
        server.setGlobalFlag(MinecraftConstants.SERVER_COMPRESSION_THRESHOLD, 100);
        server.addListener(new ServerAdapter() {
            @Override
            public void serverClosed(ServerClosedEvent event) {
                for (var entry : ProxyServer.getInstance().getOnlinePlayers().entrySet()) {
                    Player player = entry.getValue();

                    player.disconnect("Proxy closed");
                }
                System.out.println("Server closed.");
            }

            @Override
            public void sessionAdded(SessionAddedEvent event) {
                event.getSession().addListener(new JavaPacketHandler());
            }

            @Override
            public void sessionRemoved(SessionRemovedEvent event) {
                GameProfile profile = event.getSession().getFlag(MinecraftConstants.PROFILE_KEY);
                Player player = getPlayerByName(profile.getName());
                if (AuthManager.getInstance().getLoginPlayers().get(player.getUsername())) {
                    AuthManager.getInstance().getLoginPlayers().remove(player.getUsername());
                }
                if (AuthManager.getInstance().getTimers().get(player.getUsername()) != null) {
                    AuthManager.getInstance().getTimers().get(player.getUsername()).cancel();
                    AuthManager.getInstance().getTimers().remove(player.getUsername());
                }
                System.out.println(profile.getName() + " logged out");
                player.disconnect("logged out");
            }
        });

        System.out.println("Binding to " + this.config.getBindAddress() + " on port " + this.config.getPort());
        server.bind();
        System.out.println("BarrelProxy is running on [" + this.config.getBindAddress() + "::" + this.config.getPort() + "]");
    }

    public Player getPlayerByName(String username) {
        return this.onlinePlayers.get(username);
    }
}
