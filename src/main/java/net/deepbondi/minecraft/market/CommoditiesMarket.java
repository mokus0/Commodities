package net.deepbondi.minecraft.market;

import com.avaje.ebean.EbeanServer;
import com.iCo6.iConomy;
import com.iCo6.system.Account;
import com.iCo6.system.Accounts;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import net.deepbondi.minecraft.market.commands.AdminCommand;
import net.deepbondi.minecraft.market.commands.BuyCommand;
import net.deepbondi.minecraft.market.commands.PriceCheckCommand;
import net.deepbondi.minecraft.market.commands.SellCommand;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class CommoditiesMarket extends JavaPlugin {
    private static final Pattern COLON_PATTERN = Pattern.compile(":");

    private iConomy economy;
    private Accounts accounts;
    private PermissionHandler permissions;
    private final PriceModel model = new BondilandPriceModel();
    private int initialItemQty = 200;

    @Override
    public void onEnable() {
        loadConfig();
        setupDatabase();
        registerPluginListener();

        getCommand("commodities").setExecutor(new AdminCommand(this));
        getCommand("pricecheck").setExecutor(new PriceCheckCommand(this));
        getCommand("buy").setExecutor(new BuyCommand(this));
        getCommand("sell").setExecutor(new SellCommand(this));
    }

    @Override
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
        final List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Commodity.class);
        list.add(PlayerCommodityStats.class);
        return list;
    }

    public void loadConfig() {
        try {
            final Configuration config = getConfig();
            initialItemQty = config.getInt("itemdefaults.instock", initialItemQty);
        } catch (Exception e) {
            final String pluginName = getDescription().getName();
            getServer()
                    .getLogger()
                    .severe("Exception while loading " + pluginName + "/config.yml");
        }
    }

    @Override
    public void saveConfig() {
        getConfig().set("itemdefaults.instock", initialItemQty);
        super.saveConfig();
    }

    private final PluginListener pl = new PluginListener();

    private class PluginListener implements EventExecutor {
        @Override
        public void execute(final Listener l, final Event e) {
            if (e instanceof PluginEnableEvent)
                onPluginEnable((PluginEnableEvent) e);
            if (e instanceof PluginDisableEvent)
                onPluginDisable((PluginDisableEvent) e);
        }

        public void onPluginEnable(final PluginEnableEvent event) {
            discover(event.getPlugin());
        }

        public void onPluginDisable(final PluginDisableEvent event) {
            undiscover(event.getPlugin());
        }

        void discover(final Plugin plugin) {
            discover(plugin, plugin);
        }

        void undiscover(final Plugin plugin) {
            discover(plugin, null);
        }

        private void discover(final Plugin plugin, final Object surrogate) {
            if (plugin instanceof iConomy) discoverEconomy((iConomy) surrogate);
            if (plugin instanceof Permissions) discoverPermissions((Permissions) surrogate);
        }

        void discoverEconomy(final iConomy plugin) {
            economy = plugin;
            accounts = new Accounts();
        }

        void discoverPermissions(final Permissions plugin) {
            permissions = plugin.getHandler();
        }
    }

    private void registerPluginListener() {
        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(PluginEnableEvent.class, new Listener() {
        }, EventPriority.MONITOR, pl, this);

        final Plugin ic = pm.getPlugin("iConomy");
        if (ic.isEnabled()) pl.discover(ic);

        final Plugin perms = pm.getPlugin("Permissions");
        if (perms.isEnabled()) pl.discover(perms);
    }

    public PriceModel getPriceModel() {
        return model;
    }

    public iConomy getIConomy()
            throws NotReadyException {
        if (economy != null) return economy;
        throw new NotReadyException("iConomy is not yet enabled");
    }

    public Account getAccount(final String name)
            throws NotReadyException {
        getIConomy();
        return accounts.get(name);
    }

    public PermissionHandler getPermissions()
            throws NotReadyException {
        if (permissions != null) return permissions;
        throw new NotReadyException("Permissions is not yet enabled");
    }

    public boolean hasPermission(final CommandSender sender, final String action)
            throws NotReadyException {
        return !(sender instanceof Player) || getPermissions().has((Player) sender, "commodities." + action);
    }

    public synchronized Commodity lookupCommodity(final Material material, final byte byteData) {
        return getDatabase()
                .find(Commodity.class)
                .where().eq("itemId", material.getId())
                .where().eq("byteData", byteData)
                .findUnique();
    }

    public synchronized Commodity lookupCommodity(final String name) {
        // Accept "names" in several forms:
        // Material:byte and associated forms like in ScrapBukkit's /give
        // Commodity name from database
        Commodity commodity = getDatabase()
                .find(Commodity.class)
                .where().ieq("name", name)
                .findUnique();

        if (commodity == null) {
            final String[] parts = COLON_PATTERN.split(name);
            final Material material;
            byte byteData = 0;

            switch (parts.length) {
                case 2:
                    try {
                        byteData = (byte) Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        break;
                    }

                    // fall through
                    //noinspection fallthrough
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

    public synchronized boolean addCommodity(final String name, final Material material, final byte byteData, final StringBuilder outErr) {
        final EbeanServer db = getDatabase();
        db.beginTransaction();
        try {
            // Check if commodity already exists
            if (lookupCommodity(name) != null) {
                outErr.append("Commodity already exists.");
                return false;
            } else {
                final Commodity item = new Commodity();

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

    public synchronized boolean adjustStock(final String name, final long stockChange, final StringBuilder outErr) {
        final EbeanServer db = getDatabase();
        db.beginTransaction();
        try {
            final Commodity item = lookupCommodity(name);
            if (item == null) {
                outErr.append("No commodity by that name was found.");
                return false;
            } else {
                final long stock = item.getInStock() + stockChange;
                if (stock < 0) {
                    outErr.append("Stock level cannot be reduced past zero.");
                    return false;
                }

                item.setInStock(stock);
                db.update(item, adjStkUpdateProps);
            }

            db.commitTransaction();

            return true;
        } finally {
            db.endTransaction();
        }
    }

    private static final Set<String> pcsUpdateProps;
    private static final Set<String> adjStkUpdateProps;

    static {
        pcsUpdateProps = new HashSet<String>();
        pcsUpdateProps.add("numBought");
        pcsUpdateProps.add("numSold");
        pcsUpdateProps.add("moneySpent");
        pcsUpdateProps.add("moneyGained");

        adjStkUpdateProps = new HashSet<String>();
        adjStkUpdateProps.add("inStock");
    }

    public synchronized void recordPlayerCommodityStats(
            final Player player,
            final Commodity item,
            final long numBought,
            final long numSold,
            final double moneySpent,
            final double moneyGained) {
        final EbeanServer db = getDatabase();

        db.beginTransaction();

        try {
            final PlayerCommodityStats existing = db
                    .find(PlayerCommodityStats.class)
                    .where()
                    .ieq("playerName", player.getName())
                    .eq("commodityId", item.getId())
                    .findUnique();

            if (existing == null) {
                final PlayerCommodityStats stats = new PlayerCommodityStats();
                stats.setPlayerName(player.getName());
                stats.setCommodityId(item.getId());
                stats.setNumBought(numBought);
                stats.setNumSold(numSold);
                stats.setMoneySpent(moneySpent);
                stats.setMoneyGained(moneyGained);
                db.save(stats);
            } else {
                existing.setNumBought(existing.getNumBought() + numBought);
                existing.setNumSold(existing.getNumSold() + numSold);
                existing.setMoneySpent(existing.getMoneySpent() + moneySpent);
                existing.setMoneyGained(existing.getMoneyGained() + moneyGained);

                db.update(existing, pcsUpdateProps);
            }

            db.commitTransaction();
        } finally {
            db.endTransaction();
        }
    }
}

