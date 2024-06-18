
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
            Gui gui = new Gui((Player) sender, townEnemy, town);
            gui.displayInventory((Player) sender);
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        //if(sender.isOp() && args.length == 1) return Lists.newArrayList("status", "end");
        if(args.length == 1) return TownyAPI.getInstance().getTowns().stream().map(TownyObject::getName).collect(Collectors.toList());
        return List.of();
    }
}
