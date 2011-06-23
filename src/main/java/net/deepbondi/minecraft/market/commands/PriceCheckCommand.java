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
        if (args.length == 0) {
            try {
                listItems(sender);
            } catch (NotReadyException e) {
                e.explainThis(plugin, sender);
            }
            
            return true;
        }
        
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
            
            try {
                priceCheck(sender, itemName, qty);
            } catch (NotReadyException e) {
                e.explainThis(plugin, sender);
            }
            
            return true;
        }
        
        return false;
    }
        
    private void priceCheck(CommandSender sender, String itemName, long qty)
    throws NotReadyException {
        Commodity item = plugin.lookupCommodity(itemName);
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "Can't find commodity [" + ChatColor.WHITE + itemName + ChatColor.RED + "]");
            return;
        }
        
        PriceModel model = plugin.getPriceModel();
        iConomy economy = plugin.getIConomy();
        
        long available = item.getInStock();
        long buyQty = qty > available ? available : qty;
        double buyPrice = model.checkBuyPrice(item, buyQty, plugin);
        double sellPrice = model.checkSellPrice(item, qty, plugin);
        
        if (buyQty > 0)
            sender.sendMessage("" + buyQty + " " + item.getName() + " would cost " 
                + economy.format(buyPrice) + " (there are " + item.getInStock() + " available)");
        else
            sender.sendMessage("No " + item.getName() + " available to buy.");
        
        sender.sendMessage("" + qty + " " + item.getName() + " would sell for " 
            + economy.format(sellPrice));
    }
    
    private void listItems(CommandSender sender) 
    throws NotReadyException {
        // TODO: write me
        sender.sendMessage("This command would list the tradeable items, if it were implemented!");
    }
}

