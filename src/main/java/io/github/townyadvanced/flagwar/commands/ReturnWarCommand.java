package io.github.townyadvanced.flagwar.commands;

import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.war.WarProcess;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ReturnWarCommand extends AbstractCommand {

    public ReturnWarCommand() {
        super("returnwar");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        WarProcess process = FlagWar.warManager.getWarProcessByPlayer(player);

        if (process == null) return;
        process.teleportToWarSpawn(player);
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }
}
