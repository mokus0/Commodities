package net.deepbondi.minecraft.market.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AdminHelpCommand extends AdminSubCommand {
    private final AdminCommand admin;
    public AdminHelpCommand(AdminCommand admin) {
        this.admin = admin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("This would be helpful if it weren't a placeholder!");
            return true;
        }
        
        String subCmdName = args[0];
        if (args.length == 1 && admin.subCommands.containsKey(subCmdName)) {
            AdminSubCommand subCmd = admin.subCommands.get(subCmdName);
            subCmd.helpPage(sender);
            return true;
        }
        
        return false;
    }
}

