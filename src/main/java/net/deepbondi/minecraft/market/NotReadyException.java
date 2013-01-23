package net.deepbondi.minecraft.market;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class NotReadyException extends Exception {
    public NotReadyException(final String msg) {
        super(msg);
    }

    public void explainThis(final Plugin plugin, final CommandSender sender) {
        final String pluginName = plugin.getDescription().getName();
        sender.sendMessage(ChatColor.RED + pluginName + " is not ready: " + getMessage());
    }
}

