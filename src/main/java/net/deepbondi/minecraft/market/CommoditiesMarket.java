package net.deepbondi.minecraft.market;

import com.avaje.ebean.EbeanServer;
import com.iConomy.*;
import com.iConomy.system.Account;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.PersistenceException;
import net.deepbondi.minecraft.market.commands.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class CommoditiesMarket extends JavaPlugin {
    private iConomy economy = null;
    private PermissionHandler permissions = null;
    private PriceModel model = new BondilandPriceModel();
    private int initialItemQty = 64;
    
    public void onEnable() {
        loadConfig();
        setupDatabase();
        registerPluginListener();
        
        getCommand("commodities").setExecutor(new AdminCommand(this));
        getCommand("pricecheck").setExecutor(new PriceCheckCommand(this));
        getCommand("buy").setExecutor(new BuyCommand(this));
        getCommand("sell").setExecutor(new SellCommand(this));
    }
    
    public void onDisable() {
        saveConfig();
    }
    
    private void setupDatabase() {
        try {
            getDatabase().find(Commodity.class).findRowCount();
            getDatabase().find(PlayerCommodityStats.class).findRowCount();
        } catch (PersistenceException ex) {
            installDDL();
        }
    }
    
    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Commodity.class);
        list.add(PlayerCommodityStats.class);
        return list;
    }
    
    public void loadConfig() {
        try {
            Configuration config = this.getConfiguration();
            initialItemQty = config.getInt("itemdefaults.instock", initialItemQty);
        } catch (Exception e) {
            String pluginName = getDescription().getName();
            getServer()
                .getLogger()
                .severe("Exception while loading " + pluginName + "/config.yml");
        }
    }

    public void saveConfig() {
        Configuration config = getConfiguration();
        config.setProperty("itemdefaults.instock", initialItemQty);
        config.save();
    }
    
    private PluginListener pl = new PluginListener();
    private class PluginListener extends ServerListener {
        public void onPluginEnable(PluginEnableEvent event) {
            discover(event.getPlugin());
        }
        
        public void onPluginDisable(PluginDisableEvent event) {
            undiscover(event.getPlugin());
        }
        
        void discover(Plugin plugin) {
            discover(plugin, plugin);
        }
        void undiscover(Plugin plugin) {
            discover(plugin, null);
        }
        private void discover(Plugin plugin, Object surrogate) {
            if (plugin instanceof iConomy) discoverEconomy((iConomy) surrogate);
            if (plugin instanceof Permissions) discoverPermissions((Permissions) surrogate);
        }
        
        void discoverEconomy(iConomy plugin) {
            economy = plugin;
        }
        void discoverPermissions(Permissions plugin) {
            permissions = plugin.getHandler();
        }
    }
    
    private void registerPluginListener() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLUGIN_ENABLE, pl, Priority.Monitor, this);
        
        Plugin ic = pm.getPlugin("iConomy");
        if (ic.isEnabled()) pl.discover(ic);
        
        Plugin perms = pm.getPlugin("Permissions");
        if (perms.isEnabled()) pl.discover(perms);
    }
    
    public PriceModel getPriceModel()
    throws NotReadyException {
        return model;
    }
    
    public iConomy getIConomy()
    throws NotReadyException {
        if (economy != null) return economy;
        throw new NotReadyException("iConomy is not yet enabled");
    }
    
    public Account getAccount(String name)
    throws NotReadyException {
        getIConomy();
        return iConomy.getAccount(name);
    }
    
    public PermissionHandler getPermissions()
    throws NotReadyException {
        if (permissions != null) return permissions;
        throw new NotReadyException("Permissions is not yet enabled");
    }
    
    public boolean hasPermission(CommandSender sender, String action)
    throws NotReadyException {
        if (sender instanceof Player) {
            return getPermissions()
                .has((Player)sender, "commodities." + action);
        }
        return true;
    }
    
    synchronized
    public Commodity lookupCommodity(Material material, byte byteData) {
        return getDatabase()
            .find(Commodity.class)
            .where().eq("itemId", material.getId())
            .where().eq("byteData", byteData)
            .findUnique();
    }
    
    synchronized
    public Commodity lookupCommodity(String name) {
        // Accept "names" in several forms:
        // Material:byte and associated forms like in ScrapBukkit's /give
        // Commodity name from database
        Commodity commodity = getDatabase()
            .find(Commodity.class)
            .where().ieq("name", name)
            .findUnique();
        
        if (commodity == null) {
            String[] parts = name.split(":");
            Material material;
            byte byteData = 0;
            
            switch (parts.length) {
                case 2:
                    try {
                        byteData = (byte) Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        break;
                    }
                    
                    // fall through
                case 1:
                    material = Material.matchMaterial(parts[0]);
                    
                    if (material == null) break;
                    
                    commodity = lookupCommodity(material, byteData);
                    break;
                default:
                    break;
            }
        }
        
        return commodity;
        
    }
    
    synchronized
    public boolean addCommodity(String name, Material material, byte byteData, StringBuilder outErr) {
        EbeanServer db = getDatabase();
        db.beginTransaction();
        try {
            // Check if commodity already exists
            if (lookupCommodity(name) != null) {
                outErr.append("Commodity already exists.");
                return false;
            } else {
                Commodity item = new Commodity();
                
                item.setName(name);
                item.setItemId(material.getId());
                item.setByteData(byteData);
                item.setInStock(initialItemQty);
                
                db.save(item);
            }
            
            db.commitTransaction();
            
            return true;
        } finally {
            db.endTransaction();
        }
    }

    synchronized
    public boolean adjustStock(String name, long stockChange, StringBuilder outErr) {
        EbeanServer db = getDatabase();
        db.beginTransaction();
        try {
            Commodity item = lookupCommodity(name);
            if (item == null) {
                outErr.append("No commodity by that name was found.");
                return false;
            } else {
                long stock = item.getInStock() + stockChange;
                if (stock < 0) {
                    outErr.append("Stock level cannot be reduced past zero.");
                    return false;
                }
                
                item.setInStock(stock);
                db.update(item);
            }
            
            db.commitTransaction();
            
            return true;
        } finally {
            db.endTransaction();
        }
    }
}

