package net.deepbondi.minecraft.market.commands;

import net.deepbondi.minecraft.market.CommoditiesMarket;
import net.deepbondi.minecraft.market.NotReadyException;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.regex.Pattern;

public class AdminAddCommand extends AdminSubCommand {
    private static final String REQUIRED_PERMISSION = "admin.add";

    private static final Pattern CAMEL_CASE_BOUNDARY = Pattern.compile(
            String.format("%s|%s|%s",
                    "(?<=[A-Z])(?=[A-Z][a-z])",
                    "(?<=[^A-Z])(?=[A-Z])",
                    "(?<=[A-Za-z])(?=[^A-Za-z])"
            ));

    private final CommoditiesMarket plugin;

    public AdminAddCommand(final CommoditiesMarket plugin) {
        this.plugin = plugin;
    }

    @Override
    public void helpPage(final CommandSender sender) {
        sender.sendMessage("Adds an item to the Commodities Exchange, making it tradeable by players");
        sender.sendMessage("The item id and subId may already be known by the server based on the name.");
        sender.sendMessage("If not, they must be specified.  The default byteData is 0");
        sender.sendMessage("Usage: /cm add <name> [<id> [<byteData>]]");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length > 0 && args.length <= 3) {
            final String itemName = args[0];
            final String itemId = mangleMaterialName(args.length > 1 ? args[1] : itemName);
            final Material itemMaterial = lookupMaterial(itemId);

            if (itemMaterial == null) {
                sender.sendMessage("Item " + itemId + " not found!");
                return true;
            }

            byte byteData = 0;
            if (args.length > 2) {
                try {
                    byteData = (byte) Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            final StringBuilder outErr = new StringBuilder();
            if (plugin.addCommodity(itemName, itemMaterial, byteData, outErr)) {
                sender.sendMessage("Commodity added successfully.");
            } else {
                sender.sendMessage("Commodity could not be added.  " + outErr);
            }

            return true;
        }

        return false;
    }

    // shamelessly cribbed from http://stackoverflow.com/questions/2559759
    // and modified to split the output instead of inserting spaces.
    private static String[] splitCamelCase(final String s) {
        return CAMEL_CASE_BOUNDARY.split(s);
    }

    private static String mangleMaterialName(final String name) {
        // Apply a fairly liberal transformation to ALL_CAPS_UNDERSCORED
        final String[] parts = splitCamelCase(name);
        final StringBuilder mangled = new StringBuilder();
        for (final String part : parts) {
            if (mangled.length() > 0) {
                mangled.append('_');
            }

            mangled.append(part.toUpperCase());
        }

        return mangled.toString();
    }

    private static Material lookupMaterial(final String name) {
        return Material.matchMaterial(name);
    }

    @Override
    public boolean isAuthorized(final CommandSender sender) throws NotReadyException {
        return plugin.hasPermission(sender, REQUIRED_PERMISSION);
    }
}

