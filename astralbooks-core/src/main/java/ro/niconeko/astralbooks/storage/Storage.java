package ro.niconeko.astralbooks.storage;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import org.bukkit.inventory.ItemStack;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.settings.StorageSettings;
import ro.niconeko.astralbooks.utils.Side;

import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

public abstract class Storage {
    public final AstralBooksPlugin plugin;
    protected final StorageCache cache;
    private final StorageType storageType;

    protected Storage(AstralBooksPlugin plugin, StorageType storageType) {
        this.plugin = plugin;
        this.storageType = storageType;
        this.cache = new StorageCache(plugin, this);
    }

    public final StorageType getStorageType() {
        return this.storageType;
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

    protected final boolean convertFrom(Storage storage) {
        if (storage.getStorageType() == this.getStorageType())
            throw new IllegalStateException("The storage type is the same");
        try {
            for (Pair<Integer, Side> npcBook : storage.cache.getNPCBooks()) {
                int npcId = npcBook.getFirstValue();
                Side side = npcBook.getSecondValue();
                this.putNPCBookStack(npcId, side, storage.getNPCBookStack(npcId, side).get());
            }
            for (String filterName : storage.cache.getFilterNames()) {
                ItemStack book = storage.getFilterBookStack(filterName).get();
                this.putFilterBookStack(filterName, book);
            }
            for (String commandName : storage.cache.getCommandFilterNames()) {
                Pair<String, String> commandAndPermission = storage.getCommandFilterStack(commandName).get();
                String command = commandAndPermission.getFirstValue();
                String permission = commandAndPermission.getSecondValue();
                this.putCommandFilterStack(commandName, command, permission);
            }
            for (Triplet<UUID, Date, ItemStack> bookSecurity : storage.getAllBookSecurityStack(-1, -1).get()) {
                UUID playerUUID = bookSecurity.getFirstValue();
                Date securityDate = bookSecurity.getSecondValue();
                ItemStack book = bookSecurity.getThirdValue();
                this.putBookSecurityStack(playerUUID, securityDate, book);
            }
        } catch (InterruptedException | ExecutionException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed conversion!", ex);
        }
        return true;
    }
}
