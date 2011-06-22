package net.deepbondi.minecraft.market.commands;

import com.iConomy.*;
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
    
    // package scope
    final CommoditiesMarket plugin;
    
    public AdminCommand(CommoditiesMarket plugin) {
        this.plugin = plugin;
        
        subCommands = new HashMap<String,AdminSubCommand>();
        subCommands.put("help", new AdminHelpCommand(this));
        subCommands.put("add",  new AdminAddCommand(this));
        subCommands.put("stock",  new AdminStockCommand(this));
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0) {
            String subCmd = args[0];
            if (subCommands.containsKey(subCmd)) {
                AdminSubCommand subCmdExecutor = subCommands.get(subCmd);
                String[] subArgs = new String[args.length - 1];
                System.arraycopy(args, 1, subArgs, 0, subArgs.length);
                
                String action = subCmdExecutor.requiredPermission();
                if (action != null) {
                    try {
                        if (!plugin.hasPermission(sender, action)) {
                            sender.sendMessage("You don't have permission to run that admin command.");
                            return true;
                        }
                    } catch (NotReadyException e) {
                        if (!sender.isOp()) {
                            sender.sendMessage("Commodities plugin is not properly loaded: " + e.getMessage());
                        }
                    }
                }
                
                boolean subCmdExit = subCmdExecutor.onCommand(sender, cmd, subCmd, subArgs);
                
                if (subCmdExit == false) {
                    subCmdExecutor.helpPage(sender);
                }
                
                return true;
            } 
        }
        
        return false;
    }
}

