/*
 * Copyright (c) 2021 BarrelMC Team
 * This project is licensed under the MIT License
 */

package org.barrelmc.barrel.network.translator;

import com.github.steveice10.mc.protocol.data.game.entity.player.GameMode;
import org.cloudburstmc.protocol.bedrock.data.GameType;

public class TranslatorUtils {

    public static GameMode translateGamemodeToJE(GameType gameType) {
        String gameTypeString = gameType.toString();

        if (gameTypeString.contains("VIEWER")) {
            return GameMode.SPECTATOR;
        }

        return GameMode.valueOf(gameTypeString);
    }
}
