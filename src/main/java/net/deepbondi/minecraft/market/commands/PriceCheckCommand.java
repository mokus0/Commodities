package net.deepbondi.minecraft.market.commands;

import com.iCo6.iConomy;
import net.deepbondi.minecraft.market.CommoditiesMarket;
import net.deepbondi.minecraft.market.Commodity;
import net.deepbondi.minecraft.market.NotReadyException;
import net.deepbondi.minecraft.market.PriceModel;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PriceCheckCommand implements CommandExecutor {
    private final CommoditiesMarket plugin;

    public PriceCheckCommand(final CommoditiesMarket plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length >= 1 && args.length <= 2) {
            final String itemName = args[0];
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

    private void priceCheck(final CommandSender sender, final String itemName, final long qty)
            throws NotReadyException {
        final Commodity item = plugin.lookupCommodity(itemName);
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "Can't find commodity [" + ChatColor.WHITE + itemName + ChatColor.RED + ']');
            return;
        }

        final PriceModel model = plugin.getPriceModel();

        final long available = item.getInStock();
        final long buyQty = qty > available ? available : qty;
        final double buyPrice = model.checkBuyPrice(item, buyQty);
        final double sellPrice = model.checkSellPrice(item, qty);

        if (buyQty > 0)
            sender.sendMessage(buyQty + " " + item.getName() + " would cost "
                    + iConomy.format(buyPrice) + " (there are " + item.getInStock() + " available)");
        else
            sender.sendMessage("No " + item.getName() + " available to buy.");

        sender.sendMessage(qty + " " + item.getName() + " would sell for "
                + iConomy.format(sellPrice));
    }
}

