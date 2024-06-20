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

        // Выполняем асинхронно проверку и вычисление новой локации для спавна
        CompletableFuture.runAsync(() -> {
            if (!process.hasNoEnemyPlayersNearby(process.getSpawnLocation().getChunk())) {
                // Асинхронно вычисляем новую локацию для спавна
                process.calculateWarStartLocationAsync().thenAccept(newSpawnLocation -> {
                    if (newSpawnLocation != null) {
                        process.setSpawnLocation(newSpawnLocation);
                    }
                    // Выполняем телепортацию обратно в основном потоке
                    Bukkit.getScheduler().runTask(FlagWar.getInstance(), () -> process.teleportToWarSpawn(player));
                }).exceptionally(ex -> {
                    Bukkit.getLogger().severe("Failed to calculate new spawn location: " + ex.getMessage());
                    return null;
                });
            } else {
                // Если врагов поблизости нет, выполняем телепортацию сразу
                Bukkit.getScheduler().runTask(FlagWar.getInstance(), () -> process.teleportToWarSpawn(player));
            }
        });
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }
}
