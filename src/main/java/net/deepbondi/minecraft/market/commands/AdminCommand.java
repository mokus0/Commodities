package net.deepbondi.minecraft.market.commands;

import net.deepbondi.minecraft.market.CommoditiesMarket;
import net.deepbondi.minecraft.market.NotReadyException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class AdminCommand implements CommandExecutor {
    // package scope
    final Map<String, AdminSubCommand> subCommands;
    private final AdminSubCommand defaultSubCommand;

    private final CommoditiesMarket plugin;

    public AdminCommand(final CommoditiesMarket plugin) {
        this.plugin = plugin;

        subCommands = new HashMap<String, AdminSubCommand>();
        subCommands.put("help", new AdminHelpCommand(this));
        subCommands.put("add", new AdminAddCommand(plugin));
        subCommands.put("stock", new AdminStockCommand(plugin));

        defaultSubCommand = new AdminListCommand(plugin);
        subCommands.put("list", defaultSubCommand);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        final AdminSubCommand subCmd;
        final String subCmdName;
        final String[] subCmdArgs;

        if (args.length > 0) {
            subCmdName = args[0];
            if (subCommands.containsKey(subCmdName)) {
                subCmd = subCommands.get(subCmdName);
                subCmdArgs = new String[args.length - 1];
                System.arraycopy(args, 1, subCmdArgs, 0, subCmdArgs.length);
            } else {
                return false;
            }
        } else {
            subCmdName = label;
            subCmd = defaultSubCommand;
            subCmdArgs = args;
        }

        try {
            return dispatchSubCommand(sender, cmd, subCmd, subCmdName, subCmdArgs);
        } catch (NotReadyException e) {
            e.explainThis(plugin, sender);
            return true;
        }
    }

    private static boolean dispatchSubCommand(final CommandSender sender, final Command cmd, final AdminSubCommand subCmd, final String subCmdName, final String[] subCmdArgs)
            throws NotReadyException {
        if (!sender.isOp() && !subCmd.isAuthorized(sender)) {
            sender.sendMessage("You don't have permission to do that.");
            return false;
        }

        final boolean subCmdExit = subCmd.onCommand(sender, cmd, subCmdName, subCmdArgs);

        if (!subCmdExit) {
            subCmd.helpPage(sender);
        }

        return true;
    }

}

