package io.github.townyadvanced.flagwar.commands;

import io.github.townyadvanced.flagwar.war.WarManager;
import io.github.townyadvanced.flagwar.war.WarProcess;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ReturnWarCommand extends AbstractCommand {

    public ReturnWarCommand() {
        super("returnwar");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        WarProcess process = WarManager.getWarProcessByPlayer((Player) sender);
        Bukkit.getLogger().info("Here");
        if (process == null) return;
        Bukkit.getLogger().info("Here");
        if (!process.hasNoEnemyPlayersNearby(process.getSpawnLocation().getChunk()))
            process.setSpawnLocation(process.calculateWarStartLocation());
        process.teleportToWarSpawn((Player) sender);
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }
}
