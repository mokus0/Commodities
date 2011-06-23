package net.deepbondi.minecraft.market;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class NotReadyException extends Exception {
    public NotReadyException(String msg) {
        super(msg);
    }
    
    public void explainThis(Plugin plugin, CommandSender sender) {
        String pluginName = plugin.getDescription().getName();
        sender.sendMessage(ChatColor.RED + pluginName + " is not ready: " + getMessage());
    }
}

