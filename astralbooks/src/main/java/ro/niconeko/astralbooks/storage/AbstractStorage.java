package ro.niconeko.astralbooks.storage;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import org.bukkit.inventory.ItemStack;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.settings.StorageSettings;
import ro.niconeko.astralbooks.utils.Side;

import java.sql.SQLException;
import java.util.concurrent.Future;

public abstract class AbstractStorage {
    public final AstralBooksPlugin plugin;
    public final StorageCache cache;

    protected AbstractStorage(AstralBooksPlugin plugin) {
        this.plugin = plugin;
        this.cache = new StorageCache(plugin, this);
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
}
