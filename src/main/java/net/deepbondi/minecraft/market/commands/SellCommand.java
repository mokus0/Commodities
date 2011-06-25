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
                sellItems(sender, player, item, qty);
            } catch (NotReadyException e) {
                e.explainThis(plugin, sender);
            }
            
            return true;
        }
        
        return false;
    }
    
    private void sellItems(CommandSender sender, Player player, Commodity item, int qty)
    throws NotReadyException {
        iConomy economy = plugin.getIConomy();
        
        long qtyRemoved;
        double amtPaid;
        
        synchronized(plugin) {
            PriceModel model = plugin.getPriceModel();
            double price = model.checkSellPrice(item, qty, plugin);
            Holdings holdings = plugin.getAccount(player.getName()).getHoldings();
            
            Inventory inventory = player.getInventory();
            ItemStack sellingItems = new ItemStack(
                item.getItemId(),
                (int)qty,
                (short) 0,
                item.getByteData());
            Map<Integer, ItemStack> leftovers = inventory.removeItem(sellingItems);
            
            qtyRemoved = qty;
            for (ItemStack leftover : leftovers.values()) {
                qtyRemoved -= leftover.getAmount();
            }
            if (qtyRemoved == 0) {
                sender.sendMessage(ChatColor.RED + "You don't have any of those to sell.");
                return;
            }
            
            amtPaid = model.checkSellPrice(item, qtyRemoved, plugin);
            
            StringBuilder outErr = new StringBuilder();
            if (plugin.adjustStock(item.getName(), qtyRemoved, outErr)) {
                holdings.add(amtPaid);
            } else {
                // TODO: remove items that were added to inventory
                sender.sendMessage(ChatColor.RED + "Transaction failed: " + outErr.toString());
            }
        } // end synchronized
        
        plugin.recordPlayerCommodityStats(
            player,
            item,
            (long) 0,
            qtyRemoved,
            0.0,
            amtPaid);
        
        if (qtyRemoved != qty) {
            sender.sendMessage(
                "You didn't have that many.  You sold " + qtyRemoved + " for "
                 + economy.format(amtPaid) + ".");
        } else {
            sender.sendMessage(
                "" + qtyRemoved + " " + item.getName() + " sold for "
                + economy.format(amtPaid) + ".");
        }
    }
}

