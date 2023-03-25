package ro.niconeko.astralbooks.storage.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import ro.niconeko.astralbooks.AstralBooksCore;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.AbstractStorage;
import ro.niconeko.astralbooks.storage.StorageType;
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
    private boolean needsAutoSave = false;

    public JsonStorage(AstralBooksPlugin plugin) {
        super(plugin, StorageType.JSON);
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
        JsonElement filtersJson = this.jsonStorage.get("filters");
        if (filtersJson != null && filtersJson.isJsonObject())
            super.cache.filters.addAll(((JsonObject) filtersJson).keySet());
        JsonElement commandsJson = this.jsonStorage.get("commands");
        if (commandsJson != null && commandsJson.isJsonObject())
            super.cache.commands.addAll(((JsonObject) commandsJson).keySet());
        JsonElement npcBooksJson = this.jsonStorage.get("npcbooks");
        if (npcBooksJson != null && npcBooksJson.isJsonObject())
            for (String npcKey : ((JsonObject) npcBooksJson).keySet()) {
                JsonElement npcBookJson = ((JsonObject) npcBooksJson).get(npcKey);
                if (npcBookJson != null && npcBookJson.isJsonObject())
                    for (String sideKey : ((JsonObject) npcBookJson).keySet())
                        try {
                            super.cache.npcs.add(Pair.of(Integer.parseInt(npcKey), Side.fromString(sideKey)));
                        } catch (NumberFormatException ignore) {
                        }
            }
        int autoSaveInterval = settings.getJsonSettings().getSaveInterval();
        this.autoSaveJsonStorage = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            if (this.needsAutoSave) this.writeJsonStorage();
        }, 20L * autoSaveInterval, 20L * autoSaveInterval);
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
            this.jsonStorage = AstralBooksCore.GSON.fromJson(reader, JsonObject.class);
            if (this.jsonStorage == null || this.jsonStorage.isJsonNull())
                this.jsonStorage = new JsonObject();
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to read database.json!", ex);
        }
    }

    private void writeJsonStorage() {
        try (FileWriter writer = new FileWriter(this.jsonStorageFile)) {
            AstralBooksCore.GSON.toJson(this.jsonStorage, writer);
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to save database.json!", ex);
        }
    }

    @Override
    protected Future<ItemStack> getFilterBookStack(String filterName) {
        return super.cache.poolExecutor.submit(() -> {
            JsonElement filtersJson = this.jsonStorage.get("filters");
            if (filtersJson == null || !filtersJson.isJsonObject())
                return null;
            JsonElement filterJson = ((JsonObject) filtersJson).get(filterName);
            if (filterJson == null || !filterJson.isJsonObject())
                return null;
            return this.plugin.getAPI().getDistribution().convertJsonToBook((JsonObject) filterJson);
        });
    }

    @Override
    protected Future<ItemStack> getNPCBookStack(int npcId, Side side) {
        return super.cache.poolExecutor.submit(() -> {
            JsonElement npcBooksJson = this.jsonStorage.get("npcbooks");
            if (npcBooksJson == null || !npcBooksJson.isJsonObject())
                return null;
            JsonElement npcBookJson = ((JsonObject) npcBooksJson).get(String.valueOf(npcId));
            if (npcBookJson == null || !npcBookJson.isJsonObject())
                return null;
            JsonElement npcBookJsonSide = ((JsonObject) npcBookJson).get(side.toString());
            if (npcBookJsonSide == null || !npcBookJsonSide.isJsonObject())
                return null;
            return this.plugin.getAPI().getDistribution().convertJsonToBook((JsonObject) npcBookJsonSide);
        });
    }

    @Override
    protected Future<Pair<String, String>> getCommandFilterStack(String cmd) {
        return super.cache.poolExecutor.submit(() -> {
            JsonElement commandsJson = this.jsonStorage.get("commands");
            if (commandsJson == null || !commandsJson.isJsonObject())
                return null;
            JsonElement commandJson = ((JsonObject) commandsJson).get(cmd);
            if (commandJson == null || !commandJson.isJsonPrimitive())
                return null;
            String[] data = commandJson.getAsString().split(";");
            return Pair.of(data[0], data[1]);
        });
    }

    @Override
    protected void putNPCBookStack(int npcId, Side side, ItemStack book) {
        Pair<Integer, Side> pairKey = Pair.of(npcId, side);
        super.cache.npcs.add(pairKey);
        super.cache.npcBooks.put(pairKey, book);
        super.cache.poolExecutor.submit(() -> {
            try {
                JsonElement npcBooksJson = this.jsonStorage.get("npcbooks");
                if (npcBooksJson == null || !npcBooksJson.isJsonObject()) {
                    npcBooksJson = new JsonObject();
                    this.jsonStorage.add("npcbooks", npcBooksJson);
                }
                JsonElement npcBookJson = ((JsonObject) npcBooksJson).get(String.valueOf(npcId));
                if (npcBookJson == null || !npcBookJson.isJsonObject()) {
                    npcBookJson = new JsonObject();
                    ((JsonObject) npcBooksJson).add(String.valueOf(npcId), npcBookJson);
                }
                ((JsonObject) npcBookJson).add(side.toString(), this.plugin.getAPI().getDistribution().convertBookToJson(book));
                this.needsAutoSave = true;
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
                JsonElement filtersJson = this.jsonStorage.get("filters");
                if (filtersJson == null || !filtersJson.isJsonObject()) {
                    filtersJson = new JsonObject();
                    this.jsonStorage.add("filters", filtersJson);
                }
                ((JsonObject) filtersJson).add(filterName, this.plugin.getAPI().getDistribution().convertBookToJson(book));
                this.needsAutoSave = true;
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
            JsonElement commandsJson = this.jsonStorage.get("commands");
            if (commandsJson == null || !commandsJson.isJsonObject()) {
                commandsJson = new JsonObject();
                this.jsonStorage.add("commands", commandsJson);
            }
            ((JsonObject) commandsJson).add(cmd, new JsonPrimitive(filterName + ";" + permission));
            this.needsAutoSave = true;
        });
    }

    @Override
    protected void removeNPCBookStack(int npcId, Side side) {
        super.cache.npcs.remove(Pair.of(npcId, side));
        super.cache.poolExecutor.submit(() -> {
            JsonElement npcBooksJson = this.jsonStorage.get("npcbooks");
            if (npcBooksJson == null || !npcBooksJson.isJsonObject()) {
                npcBooksJson = new JsonObject();
                this.jsonStorage.add("npcbooks", npcBooksJson);
            }
            JsonElement npcBookJson = ((JsonObject) npcBooksJson).get(String.valueOf(npcId));
            if (npcBookJson == null || !npcBookJson.isJsonObject()) {
                npcBookJson = new JsonObject();
                ((JsonObject) npcBooksJson).add(String.valueOf(npcId), npcBookJson);
            }
            ((JsonObject) npcBookJson).remove(side.toString());
            this.needsAutoSave = true;
        });
    }

    @Override
    protected void removeCommandFilterStack(String cmd) {
        super.cache.commands.remove(cmd);
        super.cache.commandFilters.invalidate(cmd);
        super.cache.poolExecutor.submit(() -> {
            JsonElement commandsJson = this.jsonStorage.get("commands");
            if (commandsJson == null || !commandsJson.isJsonObject()) {
                commandsJson = new JsonObject();
                this.jsonStorage.add("commands", commandsJson);
            }
            ((JsonObject) commandsJson).remove(cmd);
            this.needsAutoSave = true;
        });
    }

    @Override
    protected void removeFilterBookStack(String filterName) {
        super.cache.filters.remove(filterName);
        super.cache.filterBooks.invalidate(filterName);
        super.cache.poolExecutor.submit(() -> {
            JsonElement filtersJson = this.jsonStorage.get("filters");
            if (filtersJson == null || !filtersJson.isJsonObject()) {
                filtersJson = new JsonObject();
                this.jsonStorage.add("filters", filtersJson);
            }
            ((JsonObject) filtersJson).remove(filterName);
            this.needsAutoSave = true;
        });
    }
}
