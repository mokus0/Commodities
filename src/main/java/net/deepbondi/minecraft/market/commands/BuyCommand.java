package net.deepbondi.minecraft.market.commands;

import com.iCo6.iConomy;
import com.iCo6.system.Holdings;
import net.deepbondi.minecraft.market.CommoditiesMarket;
import net.deepbondi.minecraft.market.Commodity;
import net.deepbondi.minecraft.market.NotReadyException;
import net.deepbondi.minecraft.market.PriceModel;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class BuyCommand implements CommandExecutor {
    private final CommoditiesMarket plugin;

    public BuyCommand(final CommoditiesMarket plugin) {
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

            final Commodity item = plugin.lookupCommodity(itemName);
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

    private void buyItems(final CommandSender sender, final Player player, final Commodity item, final int qty)
            throws NotReadyException {
        // TODO: make sure all this happens "atomically" with other iConomy
        // transactions.  'synchronized' should at least make this atomic 
        // relative to other transactions in this plugin.
        long qtyDelivered;
        final double amtCharged;

        synchronized (plugin) {
            // Check whether transaction makes sense.
            // 1. Market stock must be sufficient
            final long stock = item.getInStock();
            if (stock < qty) {
                sender.sendMessage(item.getName() + " only has " + item.getInStock() + " units in stock.");
                return;
            }

            // 2. Player must have enough money
            final PriceModel model = plugin.getPriceModel();
            final double price = model.checkBuyPrice(item, qty);
            final Holdings holdings = plugin.getAccount(player.getName()).getHoldings();

            if (!holdings.hasEnough(price)) {
                sender.sendMessage("You can't afford that!  You need "
                        + iConomy.format(price) + " but only have "
                        + iConomy.format(holdings.getBalance()) + '.');
                return;
            }

            // Transaction is OK: move the goods.
            // 1. Put the items in the player's inventory, deducting
            //    what didn't fit so they don't get charged for it.
            final Inventory inventory = player.getInventory();

            final ItemStack purchasedItems = new ItemStack(
                    item.getItemId(), qty,
                    (short) 0,
                    item.getByteData());
            final Map<Integer, ItemStack> leftovers = inventory.addItem(purchasedItems);
            qtyDelivered = qty;
            for (final ItemStack leftover : leftovers.values()) {
                qtyDelivered -= leftover.getAmount();
            }
            if (qtyDelivered == 0) {
                sender.sendMessage(ChatColor.RED + "Your inventory is full.");
                return;
            }

            amtCharged = model.checkBuyPrice(item, qtyDelivered);

            // 2. Update market stock to reflect the items transferred
            final StringBuilder outErr = new StringBuilder();
            if (plugin.adjustStock(item.getName(), -qtyDelivered, outErr)) {
                // 3. Charge them for what they received
                holdings.subtract(amtCharged);
            } else {
                // TODO: restore items that were taken from inventory
                sender.sendMessage(ChatColor.RED + "Transaction failed: " + outErr);
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
                            + iConomy.format(amtCharged) + '.');
        } else {
            sender.sendMessage(
                    qtyDelivered + " " + item.getName() + " purchased for "
                            + iConomy.format(amtCharged) + '.');
        }
    }
}

