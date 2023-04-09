package org.barrelmc.barrel.auth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import com.github.steveice10.packetlib.Session;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Timer;
import java.util.TimerTask;

public class Live {

    private static final String LIVE_CONNECT_URL = "https://login.live.com/oauth20_connect.srf";
    private static final String LIVE_TOKEN_URL = "https://login.live.com/oauth20_token.srf";

    public Timer requestLiveToken(Session session, String username) throws Exception {
        JSONObject d = AuthManager.getInstance().getXboxLive().startDeviceAuth();

        Component linkComponent = Component.text(d.getString("verification_uri"))
                .clickEvent(ClickEvent.openUrl(d.getString("verification_uri")))
                .color(NamedTextColor.GREEN)
                .decorate(TextDecoration.UNDERLINED);

        Component codeComponent = Component.text(d.getString("user_code"))
                .color(NamedTextColor.GREEN)
                .hoverEvent(Component.text("Click to copy code to clipboard."))
                .clickEvent(ClickEvent.copyToClipboard(d.getString("user_code")));

        TextComponent textComponent = Component.text()
                .append(Component.text("§eAuthenticate at ").append(linkComponent))
                .append(Component.text(" §eusing the code ").append(codeComponent))
                .append(Component.text(". §eThis code will expire in ")
                        .append(Component.text(d.getString("expires_in") + " seconds.")))
                .build();
        session.send(new ClientboundSystemChatPacket(textComponent, false));

        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    JSONObject r = AuthManager.getInstance().getXboxLive().pollDeviceAuth(d.getString("device_code"));

                    if (r.containsKey("error")) {
                        System.out.println(r.getString("error_description"));
                        return;
                    }

                    AuthManager.getInstance().getAccessTokens().put(username, r.getString("access_token"));
                    AuthManager.getInstance().getLoginPlayers().put(username, true);

                    AuthManager.getInstance().getTimers().remove(username);

                    session.send(new ClientboundSystemChatPacket(Component.text("§eSuccessfully authenticated with Xbox Live. Please rejoin!"), false));

                    timer.cancel();
                } catch (Exception ignored) {
                }
            }
        }, 0, d.getLong("interval") * 1000);

        return timer;
    }

    public JSONObject startDeviceAuth() throws Exception {
        HttpClient httpClient = HttpClient.newBuilder().build();

        String requestBody = "client_id=00000000441cc96b&scope=service::user.auth.xboxlive.com::MBI_SSL&response_type=device_code";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LIVE_CONNECT_URL))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();

        if (statusCode != 200) {
            throw new Exception("Failed to start device auth");
        }

        return JSON.parseObject(response.body());
    }

    public JSONObject pollDeviceAuth(String deviceCode) throws Exception {
        HttpClient client = HttpClient.newBuilder().build();

        String requestBody = "client_id=00000000441cc96b&grant_type=urn:ietf:params:oauth:grant-type:device_code&device_code=" + deviceCode;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(LIVE_TOKEN_URL))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception(response.body());
        }

        JSONObject json = JSON.parseObject(response.body());
        if (json.containsKey("error")) {
            if (json.getString("error").equals("authorization_pending")) {
                return null;
            } else {
                throw new Exception("non-empty unknown poll error: " + json.getString("error"));
            }
        } else {
            return JSON.parseObject(response.body());
        }
    }
}