package net.deepbondi.minecraft.market.commands;

import net.deepbondi.minecraft.market.CommoditiesMarket;
import net.deepbondi.minecraft.market.Commodity;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class AdminListCommand extends AdminSubCommand {
    private final CommoditiesMarket plugin;

    public AdminListCommand(final CommoditiesMarket plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length != 0) {
            return false;
        }

        final List<Commodity> commodities =
                plugin.getDatabase()
                        .find(Commodity.class)
                        .orderBy("name")
                        .findList();

        final StringBuilder itemNames = new StringBuilder();
        for (final Commodity item : commodities) {
            if (itemNames.length() > 0)
                itemNames.append(", ");

            itemNames.append(item.getName());
        }

        sender.sendMessage("Commodities available for trade: " + itemNames);

        return true;
    }

    @Override
    public boolean isAuthorized(final CommandSender sender) {
        return true;
    }
}

