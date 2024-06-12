package io.github.townyadvanced.flagwar.util;

import io.github.townyadvanced.flagwar.FlagWar;
import org.bukkit.Bukkit;
import org.bukkit.command.*;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCommand extends Command {
    public AbstractCommand(String prefix, String commandLabel) {
        super(commandLabel);
        FlagWar.commandMap.register(prefix, this);
    }

    public AbstractCommand(String prefix, String commandLabel, String permission) {
        this(prefix, commandLabel);
        this.setPermission(permission);
    }

    public abstract void toExecute(CommandSender sender, String label, String[] args);

    @Override
    public boolean execute(CommandSender sender,String label, String[] args) {
        toExecute(sender, label, args);
        return true;
    }

    public abstract List<String> complete(CommandSender sender, String[] args);

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return filter(complete(sender, args), args);
    }

    private List<String> filter(List<String> list, String[] args) {
        if (list == null) return null;
        String last = args[args.length - 1];
        List<String> result = new ArrayList<>();
        for (String arg : list) {
            if (arg.toLowerCase().startsWith(last.toLowerCase()))
                result.add(arg);
        }
        return result;
    }
}
