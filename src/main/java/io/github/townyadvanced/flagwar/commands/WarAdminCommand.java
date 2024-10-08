package io.github.townyadvanced.flagwar.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.newconfig.Messages;
import io.github.townyadvanced.flagwar.storage.NewWar;
import io.github.townyadvanced.flagwar.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class WarAdminCommand extends AbstractCommand {

    public WarAdminCommand() {
        super("warCommand");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Messaging.formatForComponent("&cНеполная команда, пожалуйста укажите &eгород атаки"));
            return;
        }
        if (args.length == 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Данную команду можно выполнять только от имени игрока!");
                return;
            }
            if (!sender.isOp()) return;
            Town townEnemy = TownyAPI.getInstance().getTown(args[1]);
            if (townEnemy == null) {
                sender.sendMessage(Messaging.formatForComponent(Messages.unknownTown));
                return;
            }
            Town town = TownyAPI.getInstance().getTown((Player) sender);
            if (town == null) {
                sender.sendMessage(Messaging.formatForComponent(Messages.adminNoTown));
                return;
            }
            if (town.getMayor().getPlayer() == null || !town.getMayor().getPlayer().equals((Player)sender)) {
                sender.sendMessage(Messaging.formatForComponent(Messages.adminNotMayor));
                return;
            }
            //Bukkit.getLogger().info("Here1");
            if (args[0].equals("war")) {
                NewWar war = new NewWar(town, townEnemy, ZonedDateTime.now(ZoneId.of("GMT+3")).plusSeconds(10));
                FlagWar.warManager.startWar(war);
            }
            if (args[0].equals("prewar")) {
                NewWar war = new NewWar(town, townEnemy, ZonedDateTime.now(ZoneId.of("GMT+3")).plusMinutes(2));
                FlagWar.warManager.startPreWarProcess(war);
            }
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        //if(sender.isOp() && args.length == 1) return Lists.newArrayList("status", "end");
        if(args.length == 1) return List.of("war", "prewar") ;
        if(args.length == 2) return TownyAPI.getInstance().getTowns().stream().map(TownyObject::getName).collect(Collectors.toList());
        return List.of();
    }
}
