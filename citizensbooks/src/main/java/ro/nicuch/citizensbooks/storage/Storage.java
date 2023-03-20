package ro.nicuch.citizensbooks.storage;

import com.google.common.base.Preconditions;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ro.nicuch.citizensbooks.CitizensBooksPlugin;
import ro.nicuch.citizensbooks.storage.settings.StorageSettings;
import ro.nicuch.citizensbooks.utils.Side;

import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.Future;

public abstract class Storage {
    public final CitizensBooksPlugin plugin;
    public final StorageCache cache;

    protected Storage(CitizensBooksPlugin plugin) {
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

    // NPCs books
    public boolean putNPCBook(int npcId, Side side, ItemStack book) {
        Preconditions.checkArgument(npcId >= 0, "NPC id is less than 0!");
        Preconditions.checkNotNull(book, "The ItemStack is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        return this.cache.putNPCBook(npcId, side, book);
    }

    public boolean removeNPCBook(int npcId, Side side) {
        Preconditions.checkArgument(npcId >= 0, "NPC id is less than 0!");
        return this.cache.removeNPCBook(npcId, side);
    }

    public ItemStack getNPCBook(int npcId, Side side, ItemStack def) {
        Preconditions.checkArgument(npcId >= 0, "NPC id is less than 0!");
        if (def != null)
            Preconditions.checkArgument(def.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
                    " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        return this.cache.getNPCBook(npcId, side, def);
    }

    public ItemStack getNPCBook(int npcId, Side side) {
        return this.getNPCBook(npcId, side, new ItemStack(Material.WRITTEN_BOOK));
    }

    public boolean hasNPCBook(int npcId, Side side) {
        Preconditions.checkArgument(npcId >= 0, "NPC id is less than 0!");
        return this.cache.hasNPCBook(npcId, side);
    }

    // Filters books
    public boolean putFilterBook(String filterName, ItemStack book) {
        Preconditions.checkNotNull(filterName, "The filter name is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(!filterName.isEmpty(), "The filter name is empty! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkNotNull(book, "The ItemStack is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(this.plugin.getAPI().isValidName(filterName), "Invalid characters found in filterName!");
        return this.cache.putFilterBook(filterName, book);
    }

    public boolean removeFilterBook(String filterName) {
        Preconditions.checkNotNull(filterName, "The filter name is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(!filterName.isEmpty(), "The filter name is empty! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(this.plugin.getAPI().isValidName(filterName), "Invalid characters found in filterName!");
        return this.cache.removeFilterBook(filterName);
    }

    public ItemStack getFilterBook(String filterName, ItemStack def) {
        Preconditions.checkNotNull(filterName, "The filter name is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(!filterName.isEmpty(), "The filter name is empty! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(this.plugin.getAPI().isValidName(filterName), "Invalid characters found in filterName!");
        if (def != null)
            Preconditions.checkArgument(def.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
                    " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        return this.cache.getFilterBook(filterName, def);
    }

    public ItemStack getFilterBook(String filterName) {
        return this.getFilterBook(filterName, new ItemStack(Material.WRITTEN_BOOK));
    }

    public boolean hasFilterBook(String filterName) {
        Preconditions.checkNotNull(filterName, "The filter name is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(!filterName.isEmpty(), "The filter name is empty! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(this.plugin.getAPI().isValidName(filterName), "Invalid characters found in filterName!");
        return this.cache.hasFilterBook(filterName);
    }

    public Set<String> getFilterNames() {
        return this.cache.getFilterNames();
    }

    // Commands filters
    public boolean putCommandFilter(String cmd, String filterName, String permission) {
        Preconditions.checkNotNull(cmd, "The command is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkNotNull(permission, "The permission is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(!cmd.isEmpty(), "The command is empty! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(this.plugin.getAPI().isValidName(cmd), "Invalid characters found in command!");
        Preconditions.checkArgument(this.plugin.getAPI().isValidName(permission), "Invalid characters found in permission!");
        return this.cache.putCommandFilter(cmd, filterName, permission);
    }

    public boolean removeCommandFilter(String cmd) {
        Preconditions.checkNotNull(cmd, "The command is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(!cmd.isEmpty(), "The filter name is empty! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(this.plugin.getAPI().isValidName(cmd), "Invalid characters found in command!");
        return this.cache.removeCommandFilter(cmd);
    }

    public Pair<String, String> getCommandFilter(String cmd) {
        Preconditions.checkNotNull(cmd, "The command is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(!cmd.isEmpty(), "The command is empty! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(this.plugin.getAPI().isValidName(cmd), "Invalid characters found in command!");
        return this.cache.getCommandFilter(cmd);
    }

    public boolean hasCommandFilter(String cmd) {
        Preconditions.checkNotNull(cmd, "The command is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(!cmd.isEmpty(), "The command is empty! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(this.plugin.getAPI().isValidName(cmd), "Invalid characters found in command!");
        return this.cache.hasCommandFilter(cmd);
    }

    public Set<String> getCommandFilterNames() {
        return this.cache.getCommandFilterNames();
    }
}
