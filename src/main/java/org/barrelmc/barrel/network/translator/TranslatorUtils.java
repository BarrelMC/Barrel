/*
 * Copyright (c) 2021 BarrelMC
 * BarrelMC/Barrel is licensed under the MIT License
 */

package org.barrelmc.barrel.network.translator;

import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import com.nukkitx.protocol.bedrock.data.GameType;

public class TranslatorUtils {

    public static GameMode translateGamemodeToJE(GameType gameType) {
        String gameTypeString = gameType.toString();
        if (gameTypeString.contains("VIEWER")) {
            gameTypeString = GameMode.SPECTATOR.name();
        }

        return GameMode.valueOf(gameTypeString);
    }
}
