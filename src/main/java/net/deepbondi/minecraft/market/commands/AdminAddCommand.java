package net.deepbondi.minecraft.market.commands;

import net.deepbondi.minecraft.market.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.Material;

public class AdminAddCommand extends AdminSubCommand {
    private AdminCommand admin;
    
    public AdminAddCommand(AdminCommand admin) {
        this.admin = admin;
    }
    
    @Override
    public String requiredPermission() {
        return "admin.add";
    }
    
    @Override
    public void helpPage(CommandSender sender) {
        sender.sendMessage("Adds an item to the Commodities Exchange, making it tradeable by players");
        sender.sendMessage("The item id and subId may already be known by the server based on the name.");
        sender.sendMessage("If not, they must be specified.  The default byteData is 0");
        sender.sendMessage("Usage: /cm add <name> [<id> [<byteData>]]");
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String itemName;
        String itemId;
        Material itemMaterial;
        byte byteData = 0;
        
        if (args.length > 0 && args.length <= 3) {
            itemName = args[0];
            
            if (args.length > 1) {
                itemId = mangleMaterialName(args[1]);
            } else {
                itemId = mangleMaterialName(itemName);
            }
            
            itemMaterial = lookupMaterial(itemId);
            
            if (itemMaterial == null) {
                sender.sendMessage("Item " + itemId + " not found!");
                return true;
            }
            
            if (args.length > 2) {
                try {
                    byteData = (byte) Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            
            StringBuilder outErr = new StringBuilder();
            if (admin.plugin.addCommodity(itemName, itemMaterial, byteData, outErr)) {
                sender.sendMessage("Commodity added successfully.");
            } else {
                sender.sendMessage("Commodity could not be added.  " + outErr.toString());
            }
            
            return true;
        }
        
        return false;
    }
    
    private String mangleMaterialName(String name) {
        // Apply a fairly liberal transformation to ALL_CAPS_UNDERSCORED
        String[] parts = Util.splitCamelCase(name);
        StringBuilder mangled = new StringBuilder();
        for (String part : parts) {
            if (mangled.length() > 0) {
                mangled.append("_");
            }
            
            mangled.append(part.toUpperCase());
        }
        
        return mangled.toString();
    }
    
    private Material lookupMaterial(String name) {
        return Material.matchMaterial(name);
    }
}

