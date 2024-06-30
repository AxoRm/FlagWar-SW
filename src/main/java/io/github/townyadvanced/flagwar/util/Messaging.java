/*
 * Copyright (c) 2021 TownyAdvanced
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.townyadvanced.flagwar.util;

import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.config.FlagWarConfig;
import io.github.townyadvanced.flagwar.newconfig.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Formatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles messaging for FlagWar.
 * Useful for debugging or sending a String to a Player.
 */
public final class Messaging {
    /** Sets the logger to the Bukkit-provided logger. */
    private static final Logger LOGGER = FlagWar.getInstance().getLogger();

    private Messaging() {
        throw new IllegalStateException("Utility Class");
    }

    /**
     * Sends a simple {@link String} to a given {@link Player}.
     * @param recipient Player receiving message.
     * @param str A simple String.
     */
    public static void send(@NotNull final Player recipient, @NotNull final String str) {
        recipient.sendMessage(str);
    }

    /**
     * Send a debugMessage (FW_DEBUG: [{@link String}]) over the {@link Logger} via {@link Level#WARNING}.
     * <p>
     * Must have the extra.debug config node set to true for the message to be sent.
     * @param debugMessage Simple String to pass to the logger.
     */
    public static void debug(@NotNull final String debugMessage) {
        if (FlagWarConfig.isDebugging()) {
            LOGGER.log(Level.WARNING, () -> String.format("FW_DEBUG: %s", debugMessage));
        }
    }

    /**
     * Send a debugMessage (FW_DEBUG: [{@link String}]) over the WARN channel, passing the supplied messageFormat and
     * arguments to a {@link Formatter}.
     *
     * @param messageFormat A String, compatible with {@link Formatter#format(String, Object...)}.
     * @param args Arguments to pass to the String for formatting.
     */
    public static void debug(@NotNull final String messageFormat, @NotNull final Object... args) {
        String debugString;
        try (Formatter formatter = new Formatter()) {
            debugString = formatter.format(messageFormat, args).toString();
        }
        debug(debugString);
    }

    public static List<Component> formatForList(List<String> s) {
        return s.stream().map(str -> formatForComponent(str, false)).toList();
    }

    public static String formatForString(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static com.palmergames.adventure.text.Component formatForComponentPalmergames(String s) {
        return com.palmergames.adventure.text.Component.text(formatForString(s));
    }

    public static Component formatForComponent(String s) {
        return formatForComponent(s, true);
    }

    public static Component formatForComponent(String s, boolean prefix) {
        return Component.text(ChatColor.translateAlternateColorCodes('&', (prefix ? Messages.prefix : "") +  s));
    }

    public static String parsePlaceholders(String str, String ...strings) {
        for (int i = 0; i < strings.length; i++) {
            str = str.replace("{"+i+"}", strings[i]);
        }
        return str;
    }
}
