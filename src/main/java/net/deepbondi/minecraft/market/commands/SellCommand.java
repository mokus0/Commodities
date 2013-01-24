package net.deepbondi.minecraft.market.commands;

import com.iCo6.iConomy;
import com.iCo6.system.Holdings;
import net.deepbondi.minecraft.market.CommoditiesMarket;
import net.deepbondi.minecraft.market.Commodity;
import net.deepbondi.minecraft.market.PriceModel;
import net.deepbondi.minecraft.market.exceptions.CommoditiesMarketException;
import net.deepbondi.minecraft.market.exceptions.NoSuchCommodityException;
import net.deepbondi.minecraft.market.exceptions.NotReadyException;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SellCommand implements CommandExecutor {
    private final CommoditiesMarket plugin;

    public SellCommand(final CommoditiesMarket plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        final Player player;
        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            sender.sendMessage("Cannot shop on the console");
            // TODO: make it so the console shops using an iConomy bank account
            // specified in the config.yml?
            return true;
        }

        if (args.length > 0 && args.length <= 2) {
            final String itemName = args[0];
            int qty = 1;

            if (args.length > 1) {
                try {
                    qty = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            if (qty <= 0) return false;

            final Commodity item;
            try {
                item = plugin.lookupCommodity(sender, itemName);
            } catch (NoSuchCommodityException e) {
                e.explainThis(sender);
                return true;
            }

            try {
                sellItems(sender, player, item, qty);
            } catch (NotReadyException e) {
                e.explainThis(sender);
            }

            return true;
        }

        return false;
    }

    private void sellItems(final CommandSender sender, final Player player, final Commodity item, final int qty)
            throws NotReadyException {
        long qtyRemoved;
        final double amtPaid;

        synchronized (plugin) {
            final PriceModel model = plugin.getPriceModel();
            final Holdings holdings = plugin.getAccount(player.getName()).getHoldings();

            final Inventory inventory = player.getInventory();
            final ItemStack sellingItems = new ItemStack(
                    item.getItemId(),
                    qty,
                    (short) 0,
                    item.getByteData());
            final Map<Integer, ItemStack> leftovers = inventory.removeItem(sellingItems);

            qtyRemoved = qty;
            for (final ItemStack leftover : leftovers.values()) {
                qtyRemoved -= leftover.getAmount();
            }
            if (qtyRemoved == 0) {
                sender.sendMessage(ChatColor.RED + "You don't have any of those to sell.");
                return;
            }

            amtPaid = model.checkSellPrice(item, qtyRemoved);

            try {
                plugin.adjustStock(item.getName(), qtyRemoved);

                holdings.add(amtPaid);
            } catch (CommoditiesMarketException e) {
                // TODO: remove items that were added to inventory
                e.explainThis(sender);
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
                            + iConomy.format(amtPaid) + '.');
        } else {
            sender.sendMessage(
                    qtyRemoved + " " + item.getName() + " sold for "
                            + iConomy.format(amtPaid) + '.');
        }
    }
}

