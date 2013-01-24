package net.deepbondi.minecraft.market.exceptions;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommoditiesMarketException extends Exception {
    public CommoditiesMarketException(final String msg) {
        super(msg);
    }

    public void explainThis(final CommandSender sender) {
        sender.sendMessage(ChatColor.RED + getMessage());
    }
}
