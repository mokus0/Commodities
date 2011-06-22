package net.deepbondi.minecraft.market.commands;

import net.deepbondi.minecraft.market.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.Material;

public class AdminStockCommand extends AdminSubCommand {
    private AdminCommand admin;
    
    public AdminStockCommand(AdminCommand admin) {
        this.admin = admin;
    }
    
    @Override
    public String requiredPermission() {
        return "admin.stock";
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 2) {
            String itemName = args[0];
            long stockChange;
            try {
                String sc = args[1];
                boolean pos = false;
                // accept "+123" as a valid number format, but not "+-123"
                if (sc.startsWith("+")) {
                    sc = sc.substring(1, sc.length());
                    pos = true;
                }
                
                stockChange = Long.parseLong(sc);
                
                if (pos && stockChange < 0) return false;
            } catch (NumberFormatException e) {
                return false;
            }
            
            StringBuilder outErr = new StringBuilder();
            if (admin.plugin.adjustStock(itemName, stockChange, outErr)) {
                sender.sendMessage("Stock successfully changed");
            } else {
                sender.sendMessage("Stock change failed - " + outErr.toString());
            }
            
            return true;
        }
        
        return false;
    }
}
