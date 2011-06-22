package net.deepbondi.minecraft.market.commands;

import com.iConomy.*;
import net.deepbondi.minecraft.market.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PriceCheckCommand implements CommandExecutor {
    final CommoditiesMarket plugin;
    public PriceCheckCommand(CommoditiesMarket plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length >= 1 && args.length <= 2) {
            String itemName = args[0];
            long qty = 1;
            
            if (args.length == 2) {
                try {
                    qty = Long.parseLong(args[1]);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            
            Commodity item = plugin.lookupCommodity(itemName);
            if (item == null) {
                sender.sendMessage(ChatColor.RED + "Can't find commodity [" + ChatColor.WHITE + itemName + ChatColor.RED + "]");
                return true;
            }
            
            PriceModel model;
            iConomy economy;
            
            try {
                model = plugin.getPriceModel();
                economy = plugin.getIConomy();
                
            } catch (NotReadyException e) {
                String pluginName = plugin.getDescription().getName();
                sender.sendMessage(ChatColor.RED + pluginName + " is not ready: " + e.getMessage());
                
                return true;
            }
            
            // TODO: don't show buy price unless can buy that many
            double buyPrice = model.checkBuyPrice(item, qty, plugin);
            double sellPrice = model.checkSellPrice(item, qty, plugin);
            sender.sendMessage("" + qty + " " + item.getName() + " would cost " 
                + economy.format(buyPrice) + " (" + item.getInStock() + " available), would sell for "
                + economy.format(sellPrice));
            
            return true;
        }
        
        return false;
    }
}

