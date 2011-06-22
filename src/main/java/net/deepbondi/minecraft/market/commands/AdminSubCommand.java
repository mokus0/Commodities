package net.deepbondi.minecraft.market.commands;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class AdminSubCommand implements CommandExecutor {
    public String requiredPermission() {
        return null;
    }
    
    public void helpPage(CommandSender sender) {
        sender.sendMessage("No help yet configured for this subcommand!");
    }
}

