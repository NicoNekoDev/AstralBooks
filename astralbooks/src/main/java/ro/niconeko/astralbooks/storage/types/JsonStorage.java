package ro.niconeko.astralbooks.storage.types;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import ro.niconeko.astralbooks.AstralBooksAPI;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.AbstractStorage;
import ro.niconeko.astralbooks.storage.settings.StorageSettings;
import ro.niconeko.astralbooks.utils.Side;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class JsonStorage extends AbstractStorage {
    private JsonObject jsonStorage = new JsonObject();
    private final File jsonStorageFile;
    //
    private BukkitTask autoSaveJsonStorage = null;

    public JsonStorage(AstralBooksPlugin plugin) {
        super(plugin);
        this.jsonStorageFile = new File(plugin.getDataFolder() + File.separator + "database.json");
        if (!this.jsonStorageFile.exists()) {
            this.jsonStorage = new JsonObject();
            this.writeJsonStorage();
        }
        this.readJsonStorage();
    }

    @Override
    protected boolean load(StorageSettings settings) {
        this.readJsonStorage();
        JsonObject filtersJson = this.jsonStorage.getAsJsonObject("filters");
        if (!(filtersJson == null || filtersJson.isJsonNull()))
            super.cache.filters.addAll(filtersJson.keySet());
        JsonObject commandsJson = this.jsonStorage.getAsJsonObject("commands");
        if (!(commandsJson == null || commandsJson.isJsonNull()))
            super.cache.commands.addAll(commandsJson.keySet());
        JsonObject npcBooksJson = this.jsonStorage.getAsJsonObject("npcbooks");
        if (!(npcBooksJson == null || !npcBooksJson.isJsonObject()))
            for (String npcKey : npcBooksJson.keySet()) {
                JsonObject npcBookJson = npcBooksJson.getAsJsonObject(npcKey);
                if (!(npcBookJson == null || !npcBookJson.isJsonObject()))
                    for (String sideKey : npcBookJson.keySet())
                        try {
                            super.cache.npcs.add(Pair.of(Integer.parseInt(npcKey), Side.valueOf(sideKey)));
                        } catch (NumberFormatException ignore) {
                        }
            }
        int autoSaveInterval = settings.getJsonSettings().getSaveInterval();
        this.autoSaveJsonStorage = Bukkit.getScheduler().runTaskTimer(this.plugin, this::writeJsonStorage, 20L * autoSaveInterval, 20L * autoSaveInterval);
        if (super.cache.filters.isEmpty())
            plugin.getLogger().info("No filter was loaded!");
        else
            plugin.getLogger().info("Loaded " + super.cache.filters.size() + " filters!");
        return true;
    }

    @Override
    protected void unload() {
        this.writeJsonStorage();
        if (this.autoSaveJsonStorage != null && !this.autoSaveJsonStorage.isCancelled())
            this.autoSaveJsonStorage.cancel();
    }

    private void readJsonStorage() {
        try (FileReader reader = new FileReader(this.jsonStorageFile)) {
            this.jsonStorage = AstralBooksAPI.GSON.fromJson(reader, JsonObject.class);
            if (this.jsonStorage == null || this.jsonStorage.isJsonNull())
                this.jsonStorage = new JsonObject();
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to read database.json!", ex);
        }
    }

    private void writeJsonStorage() {
        try (FileWriter writer = new FileWriter(this.jsonStorageFile)) {
            AstralBooksAPI.GSON.toJson(this.jsonStorage, writer);
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to save database.json!", ex);
        }
    }

    @Override
    protected Future<ItemStack> getFilterBookStack(String filterName) {
        return super.cache.poolExecutor.submit(() -> {
            JsonObject filtersJson = this.jsonStorage.getAsJsonObject("filters");
            if (filtersJson == null || filtersJson.isJsonNull())
                return null;
            JsonObject filterJson = filtersJson.getAsJsonObject(filterName);
            if (filterJson == null || filterJson.isJsonNull())
                return null;
            return this.plugin.getAPI().getDistribution().convertJsonToBook(filterJson);
        });
    }

    @Override
    protected Future<ItemStack> getNPCBookStack(int npcId, Side side) {
        return super.cache.poolExecutor.submit(() -> {
            JsonObject npcBooksJson = this.jsonStorage.getAsJsonObject("npcbooks");
            if (npcBooksJson == null || !npcBooksJson.isJsonObject())
                return null;
            JsonObject npcBookJson = npcBooksJson.getAsJsonObject(String.valueOf(npcId));
            if (npcBookJson == null || !npcBookJson.isJsonObject())
                return null;
            JsonObject npcBookJsonSide = npcBookJson.getAsJsonObject(side.toString());
            if (npcBookJsonSide == null || !npcBookJsonSide.isJsonObject())
                return null;
            return this.plugin.getAPI().getDistribution().convertJsonToBook(npcBookJsonSide);
        });
    }

    @Override
    protected Future<Pair<String, String>> getCommandFilterStack(String cmd) {
        return super.cache.poolExecutor.submit(() -> {
            JsonObject commandsJson = this.jsonStorage.getAsJsonObject("commands");
            if (commandsJson == null || commandsJson.isJsonNull())
                return null;
            JsonPrimitive commandJson = commandsJson.getAsJsonPrimitive(cmd);
            if (commandJson == null || !commandJson.isJsonPrimitive())
                return null;
            String[] data = commandJson.getAsString().split(";");
            return Pair.of(data[0], data[1]);
        });
    }

    @Override
    protected void putNPCBookStack(int npcId, Side side, ItemStack book) {
        if (!this.plugin.isCitizensEnabled())
            throw new IllegalStateException("Citizens is not enabled!");
        Pair<Integer, Side> pairKey = Pair.of(npcId, side);
        super.cache.npcs.add(pairKey);
        super.cache.npcBooks.put(pairKey, book);
        super.cache.poolExecutor.submit(() -> {
            try {
                JsonObject npcBooksJson = this.jsonStorage.getAsJsonObject("npcbooks");
                if (npcBooksJson == null || !npcBooksJson.isJsonObject()) {
                    npcBooksJson = new JsonObject();
                    this.jsonStorage.add("npcbooks", npcBooksJson);
                }
                JsonObject npcBookJson = npcBooksJson.getAsJsonObject(String.valueOf(npcId));
                if (npcBookJson == null || !npcBookJson.isJsonObject()) {
                    npcBookJson = new JsonObject();
                    npcBooksJson.add(String.valueOf(npcId), npcBookJson);
                }
                npcBookJson.add(side.toString(), this.plugin.getAPI().getDistribution().convertBookToJson(book));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    protected void putFilterBookStack(String filterName, ItemStack book) {
        super.cache.filters.add(filterName);
        super.cache.filterBooks.put(filterName, book);
        super.cache.poolExecutor.submit(() -> {
            try {
                JsonObject filtersJson = this.jsonStorage.getAsJsonObject("filters");
                if (filtersJson == null || !filtersJson.isJsonObject()) {
                    filtersJson = new JsonObject();
                    this.jsonStorage.add("filters", filtersJson);
                }
                filtersJson.add(filterName, this.plugin.getAPI().getDistribution().convertBookToJson(book));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    protected void putCommandFilterStack(String cmd, String filterName, String permission) {
        super.cache.commands.add(filterName);
        super.cache.commandFilters.put(cmd, Pair.of(filterName, permission));
        super.cache.poolExecutor.submit(() -> {
            JsonObject commandsJson = this.jsonStorage.getAsJsonObject("commands");
            if (commandsJson == null || !commandsJson.isJsonObject()) {
                commandsJson = new JsonObject();
                this.jsonStorage.add("commands", commandsJson);
            }
            commandsJson.add(cmd, new JsonPrimitive(filterName + ";" + permission));
        });
    }

    @Override
    protected void removeNPCBookStack(int npcId, Side side) {
        if (!this.plugin.isCitizensEnabled())
            throw new IllegalStateException("Citizens is not enabled!");
        super.cache.npcs.remove(Pair.of(npcId, side));
        super.cache.poolExecutor.submit(() -> {
            JsonObject npcBooksJson = this.jsonStorage.getAsJsonObject("npcbooks");
            if (npcBooksJson == null || !npcBooksJson.isJsonObject()) {
                npcBooksJson = new JsonObject();
                this.jsonStorage.add("npcbooks", npcBooksJson);
            }
            JsonObject npcBookJson = npcBooksJson.getAsJsonObject(String.valueOf(npcId));
            if (npcBookJson == null || !npcBookJson.isJsonObject()) {
                npcBookJson = new JsonObject();
                npcBooksJson.add(String.valueOf(npcId), npcBookJson);
            }
            npcBookJson.remove(side.toString());
        });
    }

    @Override
    protected void removeCommandFilterStack(String cmd) {
        super.cache.commands.remove(cmd);
        super.cache.commandFilters.invalidate(cmd);
        super.cache.poolExecutor.submit(() -> {
            JsonObject commandsJson = this.jsonStorage.getAsJsonObject("commands");
            if (commandsJson == null || !commandsJson.isJsonObject()) {
                commandsJson = new JsonObject();
                this.jsonStorage.add("commands", commandsJson);
            }
            commandsJson.remove(cmd);
        });
    }

    @Override
    protected void removeFilterBookStack(String filterName) {
        super.cache.filters.remove(filterName);
        super.cache.filterBooks.invalidate(filterName);
        super.cache.poolExecutor.submit(() -> {
            JsonObject filtersJson = this.jsonStorage.getAsJsonObject("filters");
            if (filtersJson == null || !filtersJson.isJsonObject()) {
                filtersJson = new JsonObject();
                this.jsonStorage.add("filters", filtersJson);
            }
            filtersJson.remove(filterName);
        });
    }
}
