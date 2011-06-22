package net.deepbondi.minecraft.market.commands;

import com.iConomy.*;
import com.iConomy.system.*;
import java.util.Map;
import net.deepbondi.minecraft.market.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SellCommand implements CommandExecutor {
    final CommoditiesMarket plugin;
    public SellCommand(CommoditiesMarket plugin) {
        this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player;
        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            sender.sendMessage("Cannot shop on the console");
            // TODO: make it so the console shops using an iConomy bank account
            // specified in the config.yml?
            return true;
        }
        
        if (args.length > 0 && args.length <= 2) {
            String itemName = args[0];
            long qty = 1;
            
            if (args.length > 1) {
                try {
                    qty = Long.parseLong(args[1]);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            
            Commodity item = plugin.lookupCommodity(itemName);
            if (item == null) {
                sender.sendMessage("Can't find a commodity by that name.");
                return true;
            }
            
            PriceModel model;
            double price;
            iConomy economy;
            Holdings holdings;
            try {
                model = plugin.getPriceModel();
                price = model.checkBuyPrice(item, qty, plugin);
                economy = plugin.getIConomy();
                holdings = plugin.getAccount(player.getName()).getHoldings();
            } catch (NotReadyException e) {
                sender.sendMessage("Commodities market is not ready yet: " + e.getMessage());
                return true;
            }
            
            Inventory inventory = player.getInventory();
            ItemStack sellingItems = new ItemStack(
                item.getItemId(),
                (int)qty, // TODO: check for overflow
                (short) 0,
                item.getByteData());
            Map<Integer, ItemStack> leftovers = inventory.removeItem(sellingItems);
            
            long qtyRemoved = qty;
            for (ItemStack leftover : leftovers.values()) {
                qtyRemoved -= leftover.getAmount();
            }
            double payment = model.checkSellPrice(item, qtyRemoved, plugin);
            
            holdings.add(payment);
            item.setInStock(item.getInStock() + qtyRemoved);
            plugin.getDatabase().update(item);
            
            if (qtyRemoved != qty) {
                sender.sendMessage(
                    "You didn't have that many.  You sold " + qtyRemoved + " for "
                     + economy.format(payment) + ".");
            } else {
                sender.sendMessage(
                    "" + qtyRemoved + " " + item.getName() + " sold for "
                    + economy.format(payment) + ".");
            }
            
            return true;
        }
        
        return false;
    }
}

