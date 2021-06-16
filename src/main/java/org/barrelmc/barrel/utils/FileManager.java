/*
 * Copyright (c) 2021 BarrelMC
 * BarrelMC/Barrel is licensed under the MIT License
 */

package org.barrelmc.barrel.utils;

import java.nio.file.Files;
import java.nio.file.Paths;

public class FileManager {

    public static String getFileContents(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception ignored) {
        }

        return null;
    }
}
