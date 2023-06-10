package ro.niconeko.astralbooks.storage;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import org.bukkit.inventory.ItemStack;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.settings.StorageSettings;
import ro.niconeko.astralbooks.utils.Side;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public abstract class Storage {
    public final AstralBooksPlugin plugin;
    protected Connection connection;
    protected final StorageCache cache;
    protected final StorageType storageType;
    protected boolean loaded;
    protected int purgeSecurityBooksOlderThan = 30;
    public final ReentrantLock lock = new ReentrantLock();

    protected Storage(AstralBooksPlugin plugin, StorageType storageType) {
        this.plugin = plugin;
        this.storageType = storageType;
        this.cache = new StorageCache(plugin, this);
    }

    public final StorageType getStorageType() {
        return this.storageType;
    }

    public final boolean isLoaded() {
        return this.loaded;
    }

    protected boolean load() throws SQLException {
        try {
            this.lock.lock();
            this.plugin.getLogger().info("Loading " + this.storageType.getFormattedName() + " database...");
            this.loadSettings(this.plugin.getSettings().getStorageSettings());
            Class.forName(this.getDriver());
            this.connection = DriverManager.getConnection(this.getURL());
            this.connection.setAutoCommit(true);
            if (!this.createTables())
                return false;
            if (!this.preloadCache())
                return false;
            this.loaded = true;
            return true;
        } catch (ClassNotFoundException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "(" + this.storageType.getFormattedName() + ") Failed to find " + this.storageType.getFormattedName() + " driver!", ex);
            return false;
        } finally {
            this.lock.unlock();
        }
    }

    protected void unload() {
        try {
            this.lock.lock();
            this.plugin.getLogger().info("Unloading " + this.storageType.getFormattedName() + " database...");
            this.loaded = false;
            try {
                this.connection.close();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.SEVERE, "(" + this.storageType.getFormattedName() + ") Failed to unload database!", ex);
            }
        } finally {
            this.lock.unlock();
        }
    }


    protected String getDriver() {
        throw new IllegalStateException("Tried to get " + this.storageType.getFormattedName() + " driver... please report this issue!");
    }

    protected String getURL() {
        throw new IllegalStateException("Tried to get " + this.storageType.getFormattedName() + " url... please report this issue!");
    }

    protected abstract void loadSettings(StorageSettings storageSettings);

    protected abstract boolean createTables();

    protected abstract boolean preloadCache();

    protected abstract Future<ItemStack> getFilterBookStack(String filterName);

    protected abstract Future<ItemStack> getNPCBookStack(int npcId, Side side);

    protected abstract Future<Pair<String, String>> getCommandFilterStack(String cmd);

    protected abstract void removeFilterBookStack(String filterName);

    protected abstract void removeNPCBookStack(int npcId, Side side);

    protected abstract void removeCommandFilterStack(String cmd);

    protected abstract void putNPCBookStack(int npcId, Side side, ItemStack book);

    protected abstract void putFilterBookStack(String filterName, ItemStack book);

    protected abstract void putCommandFilterStack(String cmd, String filterName, String permission);

    protected abstract Future<LinkedList<Pair<Date, ItemStack>>> getAllBookSecurityStack(UUID uuid, int page, int amount);

    protected abstract Future<LinkedList<Triplet<UUID, Date, ItemStack>>> getAllBookSecurityStack(int page, int amount);

    protected abstract void putBookSecurityStack(UUID uuid, Date date, ItemStack book);

    protected abstract Future<ItemStack> getSecurityBookStack(UUID uuid, Date date);

    protected abstract Queue<Triplet<Integer, Side, ItemStack>> getAllNPCBookStacks(AtomicBoolean failed);

    protected abstract Queue<Pair<String, ItemStack>> getAllFilterBookStacks(AtomicBoolean failed);

    protected abstract Queue<Triplet<String, String, String>> getAllCommandFilterStacks(AtomicBoolean failed);

    protected abstract Queue<Triplet<UUID, Date, ItemStack>> getAllBookSecurityStacks(AtomicBoolean failed);

    protected abstract void setAllNPCBookStacks(Queue<Triplet<Integer, Side, ItemStack>> queue, AtomicBoolean failed);

    protected abstract void setAllFilterBookStacks(Queue<Pair<String, ItemStack>> queue, AtomicBoolean failed);

    protected abstract void setAllCommandFilterStacks(Queue<Triplet<String, String, String>> queue, AtomicBoolean failed);

    protected abstract void setAllBookSecurityStacks(Queue<Triplet<UUID, Date, ItemStack>> queue, AtomicBoolean failed);

    protected abstract Map<UUID, Set<Date>> cleanOldSecurityBookStacks();

}
