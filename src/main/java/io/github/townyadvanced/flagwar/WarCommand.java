package io.github.townyadvanced.flagwar;

import io.github.townyadvanced.flagwar.util.AbstractCommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WarCommand extends AbstractCommand {
    public WarCommand(String prefix, String commandLabel) {
        super(FlagWar.getFlagWar().getName(), "war");
    }

    @Override
    public void toExecute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            // TODO: Add help message
        } else if ()
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("cancel");
            list.add("new");
            return list;
        }
        return null;
    }
}
