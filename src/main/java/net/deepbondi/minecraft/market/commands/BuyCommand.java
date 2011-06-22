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

public class BuyCommand implements CommandExecutor {
    final CommoditiesMarket plugin;
    public BuyCommand(CommoditiesMarket plugin) {
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
            long stock = item.getInStock();
            if (stock < qty) {
                sender.sendMessage(item.getName() + " only has " + item.getInStock() + " units in stock.");
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
            
            if (!holdings.hasEnough(price)) {
                sender.sendMessage("You can't afford that!  You need "
                    + economy.format(price) + " but only have "
                    + economy.format(holdings.balance()) + ".");
                return true;
            }
            
            // Transaction is OK
            // TODO: make sure all this happens "atomically"
            
            Inventory inventory = player.getInventory();
            
            ItemStack purchasedItems = new ItemStack(
                item.getItemId(),
                (int)qty, // TODO: check for overflow
                (short) 0,
                item.getByteData());
            Map<Integer, ItemStack> leftovers = inventory.addItem(purchasedItems);
            
            long qtyDelivered = qty;
            for (ItemStack leftover : leftovers.values()) {
                qtyDelivered -= leftover.getAmount();
            }
            double charge = model.checkBuyPrice(item, qtyDelivered, plugin);
            
            holdings.subtract(charge);
            item.setInStock(stock - qtyDelivered);
            plugin.getDatabase().update(item);
            
            if (qtyDelivered < qty) {
                sender.sendMessage(
                    "You didn't have room for everything.  You bought " 
                    + qtyDelivered + " for "
                    + economy.format(charge) + ".");
            } else {
                sender.sendMessage(
                    "" + qtyDelivered + " " + item.getName() + " purchased for "
                    + economy.format(charge) + ".");
            }
            
            return true;
        }
        
        return false;
    }
}

