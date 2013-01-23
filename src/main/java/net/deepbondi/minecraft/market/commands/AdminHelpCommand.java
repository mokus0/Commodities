package net.deepbondi.minecraft.market.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class AdminHelpCommand extends AdminSubCommand {
    private final AdminCommand admin;

    public AdminHelpCommand(final AdminCommand admin) {
        this.admin = admin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length == 0) {
            sender.sendMessage("This would be helpful if it weren't a placeholder!");
            return true;
        }

        final String subCmdName = args[0];
        if (args.length == 1 && admin.subCommands.containsKey(subCmdName)) {
            final AdminSubCommand subCmd = admin.subCommands.get(subCmdName);
            subCmd.helpPage(sender);
            return true;
        }

        return false;
    }

    @Override
    public boolean isAuthorized(final CommandSender sender) {
        return true;
    }
}

