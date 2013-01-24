package net.deepbondi.minecraft.market.commands;

import net.deepbondi.minecraft.market.CommoditiesMarket;
import net.deepbondi.minecraft.market.exceptions.CommoditiesMarketException;
import net.deepbondi.minecraft.market.exceptions.NotReadyException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class AdminStockCommand extends AdminSubCommand {
    private static final String REQUIRED_PERMISSION = "admin.stock";

    private final CommoditiesMarket plugin;

    public AdminStockCommand(final CommoditiesMarket plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length == 2) {
            final String itemName = args[0];
            final long stockChange;
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

            try {
                plugin.adjustStock(itemName, stockChange);
                sender.sendMessage("Stock successfully changed");
            } catch (CommoditiesMarketException e) {
                e.explainThis(sender);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean isAuthorized(final CommandSender sender) throws NotReadyException {
        return sender.isOp() || plugin.hasPermission(sender, REQUIRED_PERMISSION);
    }
}
