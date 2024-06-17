package io.github.townyadvanced.flagwar.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public class ReturnWarCommand extends AbstractCommand {

    public ReturnWarCommand() {
        super("returnwar");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }
}
