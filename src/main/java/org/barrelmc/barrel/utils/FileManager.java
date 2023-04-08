/*
 * Copyright (c) 2021 BarrelMC Team
 * This project is licensed under the MIT License
 */

package org.barrelmc.barrel.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.barrelmc.barrel.Barrel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

public class FileManager {

    public static JsonParser jsonParser = new JsonParser();

    public static String getFileContents(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception ignored) {
        }

        return null;
    }

    public static String getFileContents(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }

        byteArrayOutputStream.close();
        inputStream.close();

        return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
    }

    public static String decompressGZIP(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPInputStream gZIPInputStream = new GZIPInputStream(inputStream);
        byte[] buffer = new byte[1024];

        int length;
        while ((length = gZIPInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }

        byteArrayOutputStream.close();
        inputStream.close();
        gZIPInputStream.close();
        return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
    }

    public static JsonObject getJsonObjectFromResource(String resourceName) {
        InputStream inputStream = Barrel.class.getClassLoader().getResourceAsStream(resourceName);
        if (inputStream == null) {
            System.out.println("Resource \"" + resourceName + "\" does not exist!");
            return null;
        }

        try {
            return jsonParser.parse(getFileContents(inputStream)).getAsJsonObject();
        } catch (Exception e) {
            System.out.println("Failed to read \"" + resourceName + "\": " + e.getMessage());
            return null;
        }
    }
}
