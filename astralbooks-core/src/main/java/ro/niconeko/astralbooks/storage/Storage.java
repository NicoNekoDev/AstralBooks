package ro.niconeko.astralbooks.storage;


import com.google.common.base.Preconditions;
import com.google.gson.*;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import ro.niconeko.astralbooks.AstralBooksCore;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.settings.StorageSettings;
import ro.niconeko.astralbooks.storage.types.JsonStorage;
import ro.niconeko.astralbooks.storage.types.MySQLStorage;
import ro.niconeko.astralbooks.storage.types.SQLiteStorage;
import ro.niconeko.astralbooks.utils.Side;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;

public class Storage {
    private final AstralBooksPlugin plugin;
    private StorageCache cache;
    private AbstractStorage storage;
    private final File joinBookFile;
    private JsonObject joinBookDatabase;
    private boolean needsJoinBookAutoSave = false;
    private BukkitTask autoSaveJoinBook;

    public Storage(AstralBooksPlugin plugin) {
        this.plugin = plugin;
        this.joinBookFile = new File(plugin.getDataFolder() + File.separator + "join_book.json");
    }

    public boolean convertFrom(StorageType type) {
        if (storage == null) throw new IllegalStateException("Trying to convert while database is not enabled");
        try {
            AbstractStorage convertFrom = switch (type) {
                case JSON -> new JsonStorage(plugin);
                case MYSQL -> new MySQLStorage(plugin);
                case SQLITE -> new SQLiteStorage(plugin);
            };
            convertFrom.cache.load();
            convertFrom.load(this.plugin.getSettings().getStorageSettings());
            this.storage.convertFrom(convertFrom);
            convertFrom.cache.unload();
            convertFrom.unload();
            return true;
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed conversion!", ex);
            return false;
        }
    }

    public boolean load(StorageSettings settings) throws SQLException {
        if (this.plugin.getSettings().isJoinBookEnabled()) {
            if (joinBookFile.exists())
                this.joinBookDatabase = this.readJsonFile(this.joinBookFile);
            this.needsJoinBookAutoSave = false;
            this.autoSaveJoinBook = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
                if (this.needsJoinBookAutoSave) this.writeJsonFile(this.joinBookFile, this.joinBookDatabase);
            }, 20L * 60, 20L * 60);
        }
        if (this.cache != null)
            this.cache.unload();
        if (this.storage != null)
            this.storage.unload();
        this.storage = switch (settings.getDatabaseType()) {
            case JSON -> new JsonStorage(plugin);
            case MYSQL -> new MySQLStorage(plugin);
            case SQLITE -> new SQLiteStorage(plugin);
        };
        this.cache = this.storage.cache;
        this.cache.load();
        return this.storage.load(settings);
    }

    public void unload() {
        if (this.autoSaveJoinBook != null && !this.autoSaveJoinBook.isCancelled())
            this.autoSaveJoinBook.cancel();
        if (this.joinBookDatabase != null) {
            this.writeJsonFile(this.joinBookFile, this.joinBookDatabase);
            this.joinBookDatabase = null;
        }
        if (this.cache != null)
            this.cache.unload();
        if (this.storage != null)
            this.storage.unload();
    }

    private JsonObject readJsonFile(File jsonFile) throws JsonParseException {
        try (FileReader fileReader = new FileReader(jsonFile)) {
            return AstralBooksCore.GSON.fromJson(fileReader, JsonObject.class);
        } catch (Exception ex) {
            throw new JsonParseException("Failed to parse the json file " + jsonFile.getName());
        }
    }

    private void writeJsonFile(File jsonFile, JsonObject jsonObject) {
        try (FileWriter fileWriter = new FileWriter(jsonFile)) {
            AstralBooksCore.GSON.toJson(jsonObject, fileWriter);
        } catch (Exception ex) {
            throw new JsonParseException("Failed to put the data the json file " + jsonFile.getName());
        }
    }

    public boolean setJoinBook(ItemStack book) {
        Preconditions.checkNotNull(book, "The ItemStack is null! This is not an error with AstralBooks," +
                " so please don't report it. Make sure the plugins that uses AstralBooks as dependency are correctly configured.");
        Preconditions.checkArgument(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with AstralBooks," +
                " so please don't report it. Make sure the plugins that uses AstralBooks as dependency are correctly configured.");
        if (!this.removeJoinBook())
            return false;
        try {
            this.joinBookDatabase = new JsonObject();
            this.autoSaveJoinBook = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
                if (this.needsJoinBookAutoSave) this.writeJsonFile(this.joinBookFile, this.joinBookDatabase);
            }, 20L * 60, 20L * 60);
            this.joinBookDatabase.add("last_change", new JsonPrimitive(System.currentTimeMillis()));
            this.joinBookDatabase.add("join_book", this.plugin.getAPI().getDistribution().convertBookToJson(book));
            this.joinBookDatabase.add("players", new JsonObject());
            this.writeJsonFile(this.joinBookFile, this.joinBookDatabase);
            return true;
        } catch (IllegalAccessException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Unable to convert book to json", ex);
            return false;
        }
    }

    public boolean removeJoinBook() {
        if (this.joinBookFile.exists() && this.joinBookDatabase != null) {
            this.needsJoinBookAutoSave = false;
            this.joinBookDatabase = null;
            if (this.autoSaveJoinBook != null && !this.autoSaveJoinBook.isCancelled())
                this.autoSaveJoinBook.cancel();
            return this.joinBookFile.delete();
        }
        return true;
    }

    public ItemStack getJoinBook() {
        if (!this.joinBookFile.exists() && this.joinBookDatabase == null)
            return null;
        try {
            JsonElement joinBook = this.joinBookDatabase.get("join_book");
            if (joinBook == null || !joinBook.isJsonObject())
                return null;
            return this.plugin.getAPI().getDistribution().convertJsonToBook((JsonObject) joinBook);
        } catch (IllegalAccessException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Unable to convert json to book", ex);
            return null;
        }
    }

    public boolean hasJoinBook() {
        if (!this.joinBookFile.exists() && this.joinBookDatabase == null)
            return false;
        return this.joinBookDatabase.has("join_book");
    }

    public long getJoinBookLastChange() {
        if (!this.joinBookFile.exists() && this.joinBookDatabase == null)
            return 0;
        JsonElement lastChange = this.joinBookDatabase.get("last_change");
        if (lastChange == null || !lastChange.isJsonPrimitive())
            return 0;
        return lastChange.getAsLong();
    }

    public long getJoinBookLastSeen(Player player) {
        if (!this.joinBookFile.exists() && this.joinBookDatabase == null)
            return 0;
        JsonElement players = this.joinBookDatabase.get("players");
        if (players == null || !players.isJsonObject())
            return 0;
        JsonElement lastSeen = ((JsonObject) players).get(player.getUniqueId().toString());
        if (lastSeen == null || !lastSeen.isJsonPrimitive())
            return 0;
        return lastSeen.getAsLong();
    }

    public boolean setJoinBookLastSeen(Player player, long lastSeen) {
        if (!this.joinBookFile.exists() && this.joinBookDatabase == null)
            return false;
        JsonElement players = this.joinBookDatabase.get("players");
        if (players == null || !players.isJsonObject()) {
            players = new JsonObject();
            this.joinBookDatabase.add("players", players);
        }
        ((JsonObject) players).add(player.getUniqueId().toString(), new JsonPrimitive(lastSeen));
        this.needsJoinBookAutoSave = true;
        return true;
    }

    public boolean hasJoinBookLastSeen(Player player) {
        if (!this.joinBookFile.exists() && this.joinBookDatabase == null)
            return false;
        JsonElement players = this.joinBookDatabase.get("players");
        if (players == null || !players.isJsonObject())
            return false;
        return ((JsonObject) players).has(player.getUniqueId().toString());
    }

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

    public Set<Pair<Integer, Side>> getNPCBooks() {
        return this.cache.getNPCBooks();
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
        Preconditions.checkArgument(this.plugin.getAPI().isValidPermission(permission), "Invalid characters found in permission!");
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
