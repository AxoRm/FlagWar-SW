package io.github.townyadvanced.flagwar.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.util.Messaging;
import io.github.townyadvanced.flagwar.war.WarProcess;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;

public class AllyCommand extends AbstractCommand {
    public AllyCommand() {
        super("ally");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Данную команду можно выполнять только от имени игрока!");
            return;
        }
        if (!sender.isOp()) return;
        Town town = TownyAPI.getInstance().getTown((Player) sender);
        if (town == null) {
            sender.sendMessage(Messaging.formatForComponent("&cВы не мэр ебучего города"));
            return;
        }
        if (town.getMayor().getPlayer() == null || !town.getMayor().getPlayer().equals((Player)sender)) {
            sender.sendMessage(Messaging.formatForComponent("&cНищенка, стань мэром и выписывай залупу в чат!"));
            return;
        }
        WarProcess process = FlagWar.warManager.getWar(town);
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }
}
