package net.deepbondi.minecraft.market.commands;

import java.util.List;
import net.deepbondi.minecraft.market.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AdminListCommand extends AdminSubCommand {
    private AdminCommand admin;
    public AdminListCommand(AdminCommand admin) {
        this.admin = admin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 0) {
            return false;
        }
        
        List<Commodity> commodities =
            admin.plugin.getDatabase()
                .find(Commodity.class)
                .orderBy("name")
                .findList();
        
        StringBuilder itemNames = new StringBuilder();
        for (Commodity item : commodities) {
            if (itemNames.length() > 0)
                itemNames.append(", ");
            
            itemNames.append(item.getName());
        }
        
        sender.sendMessage("Commodities available for trade: " + itemNames);
        
        return true;
    }
}

