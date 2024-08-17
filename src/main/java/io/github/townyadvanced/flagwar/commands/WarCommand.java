
package io.github.townyadvanced.flagwar.commands;

import com.google.common.collect.Lists;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.gui.Gui;
import io.github.townyadvanced.flagwar.newconfig.Messages;
import io.github.townyadvanced.flagwar.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class WarCommand extends AbstractCommand {
    public WarCommand() {
        super("war");
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Messaging.formatForComponent(Messages.noArgs));
            return;
        }
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Messages.consoleSender);
                return;
            }
            Town townEnemy = TownyAPI.getInstance().getTown(args[0]);
            if (townEnemy == null || townEnemy.isRuined()) {
                sender.sendMessage(Messaging.formatForComponent(Messages.unknownTown));
                return;
            }
            Town town = TownyAPI.getInstance().getTown((Player) sender);
            if (town == null) {
                sender.sendMessage(Messaging.formatForComponent(Messages.noTown));
                return;
            }
            if (town.getMayor().getPlayer() == null || !town.getMayor().getPlayer().equals((Player)sender)) {
                sender.sendMessage(Messaging.formatForComponent(Messages.notMayor));
                return;
            }

//            if (process(sender, town, "&cВаш город существует менее 3-х дней. Вы не можете объявить войну"))
//                return;
//            if (process(sender, townEnemy, "&cДанный город существует менее 3-х дней. Вы не можете объявить ему войну"))
//                return;

            if (townEnemy.getLevelNumber() < town.getLevelNumber()-1) {
                sender.sendMessage(Messaging.formatForComponent("&cУровень вашего города выше уровня противника на " + String.valueOf(town.getLevelNumber() - townEnemy.getLevelNumber()) + ". Вы не можете объявить ему войну"));
                return;
            }
            if (FlagWar.warManager.getWar(town) != null || FlagWar.warManager.getPreWar(town) != null) {
                sender.sendMessage("&cВы не можете объявить войну, когда у вас уже есть текущая/запланированная война");
                return;
            }
            Gui gui = new Gui((Player) sender, townEnemy, town);
            gui.displayInventory((Player) sender);
        }
    }

    public boolean process(CommandSender sender, Town town, String message) {
        long townCreationTimeMillis = town.getRegistered();
        Instant townCreationInstant = Instant.ofEpochMilli(townCreationTimeMillis);
        Instant currentTime = Instant.now();

        Duration timeSinceCreation = Duration.between(townCreationInstant, currentTime);
        Duration threeDays = Duration.ofDays(3);
        if (timeSinceCreation.compareTo(threeDays) < 0) {
            sender.sendMessage(Messaging.formatForComponent(message));
            return true;
        }
        return false;
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        //if(sender.isOp() && args.length == 1) return Lists.newArrayList("status", "end");
        if(args.length == 1) return TownyAPI.getInstance().getTowns().stream().map(TownyObject::getName).collect(Collectors.toList());
        return List.of();
    }
}
