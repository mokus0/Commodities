package net.deepbondi.minecraft.market.commands;

import com.iCo6.*;
import java.util.HashMap;
import java.util.Map;
import net.deepbondi.minecraft.market.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommand implements CommandExecutor {
    // package scope
    final Map<String,AdminSubCommand> subCommands;
    public AdminSubCommand defaultSubCommand;
    
    // package scope
    final CommoditiesMarket plugin;
    
    public AdminCommand(CommoditiesMarket plugin) {
        this.plugin = plugin;
        
        subCommands = new HashMap<String,AdminSubCommand>();
        subCommands.put("help", new AdminHelpCommand(this));
        subCommands.put("add",  new AdminAddCommand(this));
        subCommands.put("stock",  new AdminStockCommand(this));
        
        defaultSubCommand = new AdminListCommand(this);
        subCommands.put("list", defaultSubCommand);
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        AdminSubCommand subCmd;
        String subCmdName;
        String subCmdArgs[];
        
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
    
    public boolean dispatchSubCommand(CommandSender sender, Command cmd, AdminSubCommand subCmd, String subCmdName, String[] subCmdArgs)
    throws NotReadyException {
        String action = subCmd.requiredPermission();
        if (action != null) {
            if (!sender.isOp() && !plugin.hasPermission(sender, action)) {
                sender.sendMessage("You don't have permission to do that.");
                return false;
            }
        }
        
        boolean subCmdExit = subCmd.onCommand(sender, cmd, subCmdName, subCmdArgs);
        
        if (subCmdExit == false) {
            subCmd.helpPage(sender);
        }
        
        return true;
    } 
    
}

