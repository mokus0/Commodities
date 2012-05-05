package net.deepbondi.minecraft.market.commands;

import com.iCo6.*;
import com.iCo6.system.*;
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
            int qty = 1;
            
            if (args.length > 1) {
                try {
                    qty = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            
            if (qty <= 0) return false;
            
            Commodity item = plugin.lookupCommodity(itemName);
            if (item == null) {
                sender.sendMessage("Can't find a commodity by that name.");
                return true;
            }
            
            try {
                buyItems(sender, player, item, qty);
            } catch (NotReadyException e) {
                e.explainThis(plugin, sender);
            }
            return true;
        }
        
        return false;
    }
    
    private void buyItems(CommandSender sender, Player player, Commodity item, int qty)
    throws NotReadyException {
        // TODO: make sure all this happens "atomically" with other iConomy
        // transactions.  'synchronized' should at least make this atomic 
        // relative to other transactions in this plugin.
        iConomy economy = plugin.getIConomy();
        
        long qtyDelivered;
        double amtCharged;
        
        synchronized(plugin) {
                    // Check whether transaction makes sense.
            // 1. Market stock must be sufficient
            long stock = item.getInStock();
            if (stock < qty) {
                sender.sendMessage(item.getName() + " only has " + item.getInStock() + " units in stock.");
                return;
            }
        
            // 2. Player must have enough money
            PriceModel model = plugin.getPriceModel();
            double price = model.checkBuyPrice(item, qty, plugin);
            Holdings holdings = plugin.getAccount(player.getName()).getHoldings();
        
            if (!holdings.hasEnough(price)) {
                sender.sendMessage("You can't afford that!  You need "
                    + economy.format(price) + " but only have "
                    + economy.format(holdings.getBalance()) + ".");
                return;
            }
            
            // Transaction is OK: move the goods.
            // 1. Put the items in the player's inventory, deducting
            //    what didn't fit so they don't get charged for it.
            Inventory inventory = player.getInventory();
            
            ItemStack purchasedItems = new ItemStack(
                item.getItemId(), qty,
                (short) 0,
                item.getByteData());
            Map<Integer, ItemStack> leftovers = inventory.addItem(purchasedItems);
            qtyDelivered = qty;
            for (ItemStack leftover : leftovers.values()) {
                qtyDelivered -= leftover.getAmount();
            }
            if (qtyDelivered == 0) {
                sender.sendMessage(ChatColor.RED + "Your inventory is full.");
                return;
            }
            
            amtCharged = model.checkBuyPrice(item, qtyDelivered, plugin);
            
            // 2. Update market stock to reflect the items transferred
            StringBuilder outErr = new StringBuilder();
            if (plugin.adjustStock(item.getName(), -qtyDelivered, outErr)) {
                // 3. Charge them for what they received
                holdings.subtract(amtCharged);
            } else {
                // TODO: restore items that were taken from inventory
                sender.sendMessage(ChatColor.RED + "Transaction failed: " + outErr.toString());
            }
        } // end synchronized
        
        plugin.recordPlayerCommodityStats(
            player,
            item,
            qtyDelivered,
            (long) 0,
            amtCharged,
            0.0);
        
        if (qtyDelivered < qty) {
            sender.sendMessage(
                "You didn't have room for everything.  You bought " 
                + qtyDelivered + " for "
                + economy.format(amtCharged) + ".");
        } else {
            sender.sendMessage(
                "" + qtyDelivered + " " + item.getName() + " purchased for "
                + economy.format(amtCharged) + ".");
        }
    }
}

