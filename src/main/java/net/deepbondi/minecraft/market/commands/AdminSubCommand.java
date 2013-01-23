package net.deepbondi.minecraft.market.commands;

import net.deepbondi.minecraft.market.NotReadyException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

abstract class AdminSubCommand implements CommandExecutor {
    public void helpPage(final CommandSender sender) {
        sender.sendMessage("No help yet configured for this sub-command!");
    }

    public abstract boolean isAuthorized(CommandSender sender) throws NotReadyException;
}

