/*
 * Copyright (c) 2021 BarrelMC Team
 * This project is licensed under the MIT License
 */

package org.barrelmc.barrel.network.converter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.barrelmc.barrel.utils.FileManager;

import java.util.HashMap;
import java.util.Map;

public class BlockConverter {

    public static final HashMap<Integer, Integer> BEDROCK_BLOCK_RUNTIME_TO_JAVA_BLOCK_STATE = new HashMap<>();

    public static void init() {
        JsonObject jsonObject = FileManager.getJsonObjectFromResource("runtime_blocks.json");

        assert jsonObject != null;

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            Integer bedrockRuntimeId = Integer.valueOf(entry.getKey());
            JsonObject blockEntry = entry.getValue().getAsJsonObject();
            Integer javaStateId = blockEntry.get("java_default_state").getAsInt();

            BEDROCK_BLOCK_RUNTIME_TO_JAVA_BLOCK_STATE.put(bedrockRuntimeId, javaStateId);
        }
    }

    // Convert mc bedrock runtime block id to java block state id
    public static int bedrockRuntimeToJavaStateId(int bedrockBlockId) {
        return BEDROCK_BLOCK_RUNTIME_TO_JAVA_BLOCK_STATE.getOrDefault(bedrockBlockId, 1);
    }
}
