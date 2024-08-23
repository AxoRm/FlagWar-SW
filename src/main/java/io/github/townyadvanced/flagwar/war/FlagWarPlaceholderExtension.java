package io.github.townyadvanced.flagwar.war;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
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
        if (player == null)
            return "";
        Town town = TownyAPI.getInstance().getTown(player);
        if (town == null)
            return "";
        WarProcess process = FlagWar.warManager.getWarProcessByPlayer(player);
        if (process == null)
            return "";
        // %flagwar_prefix%
        if ("prefix".equals(identifier)) {
            return process.getPrefix(town);
        }
        // %prefix_suffix%
        if ("suffix".equals(identifier)) {
            return process.getSuffix(town);
        }
        if ("status".equals(identifier)) {
            return process.getStatus(town);
        }
        return null;
    }
}
