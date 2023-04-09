package org.barrelmc.barrel.auth;

import com.alibaba.fastjson.JSONObject;
import org.barrelmc.barrel.utils.FileManager;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class XboxLogin {
    private static final String XBOX_PRE_AUTH_URL = "https://login.live.com/oauth20_authorize.srf?client_id=00000000441cc96b&redirect_uri=https://login.live.com/oauth20_desktop.srf&response_type=token&display=touch&scope=service::user.auth.xboxlive.com::MBI_SSL&locale=en";

    private JSONObject getPreAuthToken() throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(XBOX_PRE_AUTH_URL).openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept-encoding", "gzip");
        connection.setRequestProperty("Accept-Language", "en-US");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (XboxReplay; XboxLiveAuth/3.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        String responce = FileManager.decompressGZIP(connection.getInputStream());
        JSONObject resJson = new JSONObject();
        resJson.put("urlPost", findArgs(responce, "urlPost:'"));
        String argTmp = findArgs(responce, "sFTTag:'");
        argTmp = argTmp.substring(argTmp.indexOf("value=\"") + 7);
        resJson.put("PPFT", argTmp);
        List<String> cookies = connection.getHeaderFields().get("Set-Cookie");
        StringBuilder allCookie = new StringBuilder();
        for (String cookie : cookies) {
            allCookie.append(cookie.split(";")[0]);
            allCookie.append("; ");
        }
        resJson.put("cookie", allCookie);
        return resJson;
    }

    public String getAccessToken(String username, String password) throws Exception {
        JSONObject preAuthToken = getPreAuthToken();
        HttpsURLConnection connection = (HttpsURLConnection) new URL(preAuthToken.getString("urlPost")).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept-encoding", "gzip");
        connection.setRequestProperty("Accept-Language", "en-US");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (XboxReplay; XboxLiveAuth/3.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Cookie", preAuthToken.getString("cookie"));

        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(true);

        DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
        String postStr = "login=" + username + "&loginfmt=" + username + "&passwd=" + password + "&PPFT=" + preAuthToken.getString("PPFT");
        dataOutputStream.writeBytes(postStr);
        dataOutputStream.flush();

        connection.connect();
        InputStream is = connection.getInputStream();
        String url = connection.getURL().toString(), hash, accessToken = "";
        hash = url.split("#")[1];
        String[] hashes = hash.split("&");
        for (String partHash : hashes) {
            if (partHash.split("=")[0].equals("access_token")) {
                accessToken = partHash.split("=")[1];
                break;
            }
        }
        is.close();
        return accessToken.replaceAll("%2b", "+");
    }

    private String findArgs(String str, String args) {
        if (str.contains(args)) {
            int pos = str.indexOf(args);
            String result = str.substring(pos + args.length());
            pos = result.indexOf("',");
            result = result.substring(0, pos);
            return result;
        } else {
            throw new IllegalArgumentException("Can't find argument");
        }
    }
}
