package ro.nicuch.citizensbooks.storage;


import io.github.NicoNekoDev.SimpleTuples.Pair;
import org.bukkit.inventory.ItemStack;
import ro.nicuch.citizensbooks.CitizensBooksPlugin;
import ro.nicuch.citizensbooks.storage.settings.StorageSettings;
import ro.nicuch.citizensbooks.storage.types.JsonStorage;
import ro.nicuch.citizensbooks.storage.types.MySQLStorage;
import ro.nicuch.citizensbooks.storage.types.SQLiteStorage;
import ro.nicuch.citizensbooks.utils.Side;

import java.sql.SQLException;
import java.util.concurrent.Future;

public class StorageConfiguration extends Storage {
    private Storage storage;

    public StorageConfiguration(CitizensBooksPlugin plugin) {
        super(plugin);
    }

    public boolean load(StorageSettings settings) throws SQLException {
        if (this.storage != null)
            this.storage.unload();
        switch (settings.getDatabaseType()) {
            case JSON -> this.storage = new JsonStorage(plugin); // todo
            case MYSQL -> this.storage = new MySQLStorage(plugin);
            case SQLITE -> this.storage = new SQLiteStorage(plugin);
        }
        return this.storage.load(settings);
    }

    @Override
    public void unload() {
        if (this.storage != null)
            this.storage.unload();
    }

    @Override
    protected Future<ItemStack> getFilterBookStack(String filterName) {
        return this.storage.getFilterBookStack(filterName);
    }

    @Override
    protected Future<ItemStack> getNPCBookStack(int npcId, Side side) {
        return this.storage.getNPCBookStack(npcId, side);
    }

    @Override
    protected Future<Pair<String, String>> getCommandFilterStack(String cmd) {
        return this.storage.getCommandFilterStack(cmd);
    }

    @Override
    protected void removeNPCBookStack(int npcId, Side side) {
        this.storage.removeNPCBookStack(npcId, side);
    }

    @Override
    protected void removeCommandFilterStack(String cmd) {
        this.storage.removeFilterBookStack(cmd);
    }

    @Override
    protected void putNPCBookStack(int npcId, Side side, ItemStack book) {
        this.storage.putNPCBookStack(npcId, side, book);
    }

    @Override
    protected void putFilterBookStack(String filterName, ItemStack book) {
        this.storage.putFilterBookStack(filterName, book);
    }

    @Override
    protected void putCommandFilterStack(String cmd, String filterName, String permission) {
        this.storage.putCommandFilterStack(cmd, filterName, permission);
    }

    @Override
    protected void removeFilterBookStack(String filterName) {
        this.storage.removeFilterBookStack(filterName);
    }
}
