package io.github.townyadvanced.flagwar.war;

import io.github.townyadvanced.flagwar.FlagWar;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FlagWarPlaceholderExtension extends PlaceholderExpansion {

    private FlagWar plugin;

    public FlagWarPlaceholderExtension(FlagWar plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "flagwar";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        // %flagwar_prefix%
        if ("prefix".equals(identifier)) {
            String prefix = FlagWar.warManager.getStatus(player);
            return String.valueOf(prefix);
        }

        // %prefix_suffix%
        if ("suffix".equals(identifier)) {
            String prefix = FlagWar.warManager.getStatus(player);
            return String.valueOf(prefix);
        }

        return null;
    }
}
