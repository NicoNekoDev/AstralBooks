package ro.niconeko.astralbooks.storage;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import org.bukkit.inventory.ItemStack;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.settings.StorageSettings;
import ro.niconeko.astralbooks.utils.Side;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Storage {
    public final AstralBooksPlugin plugin;
    protected final StorageCache cache;
    private final StorageType storageType;
    protected boolean loaded;
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

    protected abstract boolean load(StorageSettings settings) throws SQLException;

    protected abstract void unload();

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
