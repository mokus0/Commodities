package net.deepbondi.minecraft.market.exceptions;

import org.bukkit.plugin.Plugin;

public class NotReadyException extends CommoditiesMarketException {
    public NotReadyException(final Plugin plugin, final String msg) {
        super(plugin.getDescription().getName() + " is not ready: " + msg);
    }
}

