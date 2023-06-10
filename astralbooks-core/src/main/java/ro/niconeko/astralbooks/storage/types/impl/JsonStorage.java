/*
 *     CitizensBooks
 *     Copyright (c) 2023 @ Drăghiciu 'NicoNekoDev' Nicolae
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package ro.niconeko.astralbooks.storage.types.impl;

import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import ro.niconeko.astralbooks.AstralBooksCore;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.StorageType;
import ro.niconeko.astralbooks.storage.types.EmbedStorage;
import ro.niconeko.astralbooks.utils.Side;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class JsonStorage extends EmbedStorage {
    private JsonObject jsonStorage = new JsonObject();
    private final File jsonStorageFile;
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
    protected boolean load() {
        try {
            this.lock.lock();
            super.plugin.getLogger().info("Loading " + super.storageType.getFormattedName() + " database...");
            super.loadSettings(super.plugin.getSettings().getStorageSettings());
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
            this.autoSaveJsonStorage = Bukkit.getScheduler().runTaskTimer(super.plugin, () -> {
                if (this.needsAutoSave) this.writeJsonStorage();
            }, 20L * super.autoSaveInterval, 20L * super.autoSaveInterval);
            if (super.cache.filters.isEmpty()) plugin.getLogger().info("No filter was loaded!");
            else plugin.getLogger().info("Loaded " + super.cache.filters.size() + " filters!");
            super.loaded = true;
            return true;
        } finally {
            super.lock.unlock();
        }
    }

    @Override
    protected void unload() {
        try {
            super.lock.lock();
            super.plugin.getLogger().info("Unloading " + super.storageType.getFormattedName() + " database...");
            super.loaded = false;
            this.writeJsonStorage();
            if (this.autoSaveJsonStorage != null) {
                this.autoSaveJsonStorage.cancel();
                this.autoSaveJsonStorage = null;
            }
        } finally {
            super.lock.unlock();
        }
    }

    private void readJsonStorage() {
        try (FileReader reader = new FileReader(this.jsonStorageFile)) {
            this.jsonStorage = AstralBooksCore.GSON.fromJson(reader, JsonObject.class);
            if (this.jsonStorage == null || !this.jsonStorage.isJsonObject()) this.jsonStorage = new JsonObject();
        } catch (Exception ex) {
            super.plugin.getLogger().log(Level.WARNING, "Failed to read database.json!", ex);
        }
    }

    private void writeJsonStorage() {
        try (FileWriter writer = new FileWriter(this.jsonStorageFile)) {
            AstralBooksCore.GSON.toJson(this.jsonStorage, writer);
        } catch (Exception ex) {
            super.plugin.getLogger().log(Level.WARNING, "Failed to save database.json!", ex);
        }
    }

    @Override
    protected Future<ItemStack> getFilterBookStack(String filterName) {
        return super.cache.poolExecutor.submit(() -> {
            JsonElement filtersJson = this.jsonStorage.get("filters");
            if (filtersJson == null || !filtersJson.isJsonObject()) return null;
            JsonElement filterJson = ((JsonObject) filtersJson).get(filterName);
            if (filterJson == null || !filterJson.isJsonObject()) return null;
            return super.plugin.getAPI().getDistribution().convertJsonToBook((JsonObject) filterJson);
        });
    }

    @Override
    protected Future<ItemStack> getNPCBookStack(int npcId, Side side) {
        return super.cache.poolExecutor.submit(() -> {
            JsonElement npcBooksJson = this.jsonStorage.get("npcbooks");
            if (npcBooksJson == null || !npcBooksJson.isJsonObject()) return null;
            JsonElement npcBookJson = ((JsonObject) npcBooksJson).get(String.valueOf(npcId));
            if (npcBookJson == null || !npcBookJson.isJsonObject()) return null;
            JsonElement npcBookJsonSide = ((JsonObject) npcBookJson).get(side.toString());
            if (npcBookJsonSide == null || !npcBookJsonSide.isJsonObject()) return null;
            return super.plugin.getAPI().getDistribution().convertJsonToBook((JsonObject) npcBookJsonSide);
        });
    }

    @Override
    protected Future<Pair<String, String>> getCommandFilterStack(String cmd) {
        return super.cache.poolExecutor.submit(() -> {
            JsonElement commandsJson = this.jsonStorage.get("commands");
            if (commandsJson == null || !commandsJson.isJsonObject()) return null;
            JsonElement commandJson = ((JsonObject) commandsJson).get(cmd);
            if (commandJson == null || !commandJson.isJsonPrimitive()) return null;
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
                ((JsonObject) npcBookJson).add(side.toString(), super.plugin.getAPI().getDistribution().convertBookToJson(book));
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
                ((JsonObject) filtersJson).add(filterName, super.plugin.getAPI().getDistribution().convertBookToJson(book));
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
    protected Future<LinkedList<Pair<Date, ItemStack>>> getAllBookSecurityStack(UUID uuid, int page, int amount) {
        return super.cache.poolExecutor.submit(() -> {
            LinkedList<Pair<Date, ItemStack>> temporaryList = new LinkedList<>();
            JsonElement bookSecurity = this.jsonStorage.get("book_security");
            if (bookSecurity == null || !bookSecurity.isJsonObject()) return temporaryList;
            JsonElement allBooksSecurity = ((JsonObject) bookSecurity).get("saved_books");
            if (allBooksSecurity == null || !allBooksSecurity.isJsonObject()) return temporaryList;
            JsonElement allPlayersSecurity = ((JsonObject) bookSecurity).get("saved_players");
            if (allPlayersSecurity == null || !allPlayersSecurity.isJsonObject()) return temporaryList;
            JsonElement playerSecurity = ((JsonObject) allPlayersSecurity).get(uuid.toString());
            if (playerSecurity == null || !playerSecurity.isJsonObject()) return temporaryList;
            for (String timeStamp : ((JsonObject) playerSecurity).keySet()) {
                JsonElement bookHashJson = ((JsonObject) playerSecurity).get(timeStamp);
                if (bookHashJson == null || !bookHashJson.isJsonPrimitive()) continue;
                String bookHash = bookHashJson.getAsString();
                JsonElement bookJson = ((JsonObject) allBooksSecurity).get(bookHash);
                if (bookJson == null || !bookJson.isJsonObject()) continue;
                try {
                    ItemStack book = super.plugin.getAPI().getDistribution().convertJsonToBook((JsonObject) bookJson);
                    Date date = new Date(Long.parseLong(timeStamp));
                    temporaryList.add(Pair.of(date, book));
                } catch (IllegalAccessException | NumberFormatException ignored) {
                }
            }
            temporaryList.sort(Comparator.comparing(pair -> pair.getFirstValue(), Comparator.reverseOrder()));
            if (page < 0) return temporaryList;
            LinkedList<Pair<Date, ItemStack>> list = new LinkedList<>();
            for (int i = page * amount; i < (page * amount) + amount; i++) {
                if (i >= temporaryList.size()) break;
                list.add(temporaryList.get(i));
            }
            return list;
        });
    }

    @Override
    protected Future<LinkedList<Triplet<UUID, Date, ItemStack>>> getAllBookSecurityStack(int page, int amount) {
        return super.cache.poolExecutor.submit(() -> {
            LinkedList<Triplet<UUID, Date, ItemStack>> temporaryList = new LinkedList<>();
            JsonElement bookSecurity = this.jsonStorage.get("book_security");
            if (bookSecurity == null || !bookSecurity.isJsonObject()) return temporaryList;
            JsonElement allBooksSecurity = ((JsonObject) bookSecurity).get("saved_books");
            if (allBooksSecurity == null || !allBooksSecurity.isJsonObject()) return temporaryList;
            JsonElement allPlayersSecurity = ((JsonObject) bookSecurity).get("saved_players");
            if (allPlayersSecurity == null || !allPlayersSecurity.isJsonObject()) return temporaryList;
            for (String uuidString : ((JsonObject) allPlayersSecurity).keySet()) {
                try {
                    JsonElement playerSecurity = ((JsonObject) allPlayersSecurity).get(uuidString);
                    if (playerSecurity == null || !playerSecurity.isJsonObject()) continue;
                    for (String timeStamp : ((JsonObject) playerSecurity).keySet()) {
                        try {
                            JsonElement bookHashJson = ((JsonObject) playerSecurity).get(timeStamp);
                            if (bookHashJson == null || !bookHashJson.isJsonPrimitive()) continue;
                            String bookHash = bookHashJson.getAsString();
                            JsonElement bookJson = ((JsonObject) allBooksSecurity).get(bookHash);
                            if (bookJson == null || !bookJson.isJsonObject()) continue;
                            ItemStack book = super.plugin.getAPI().getDistribution().convertJsonToBook((JsonObject) bookJson);
                            Date date = new Date(Long.parseLong(timeStamp));
                            temporaryList.add(Triplet.of(UUID.fromString(uuidString), date, book));
                        } catch (IllegalAccessException ignored) {
                        }
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
            temporaryList.sort(Comparator.comparing(pair -> pair.getSecondValue(), Comparator.reverseOrder()));
            if (page < 0) return temporaryList;
            LinkedList<Triplet<UUID, Date, ItemStack>> list = new LinkedList<>();
            for (int i = page * amount; i < (page * amount) + amount; i++) {
                if (i >= temporaryList.size()) break;
                list.add(temporaryList.get(i));
            }
            return list;
        });
    }

    @Override
    protected void putBookSecurityStack(UUID uuid, Date date, ItemStack book) {
        super.cache.poolExecutor.submit(() -> {
            JsonElement bookSecurity = this.jsonStorage.get("book_security");
            if (bookSecurity == null || !bookSecurity.isJsonObject()) {
                bookSecurity = new JsonObject();
                this.jsonStorage.add("book_security", bookSecurity);
            }
            JsonElement allBooksSecurity = ((JsonObject) bookSecurity).get("saved_books");
            if (allBooksSecurity == null || !allBooksSecurity.isJsonObject()) {
                allBooksSecurity = new JsonObject();
                ((JsonObject) bookSecurity).add("saved_books", allBooksSecurity);
            }
            JsonElement allPlayersSecurity = ((JsonObject) bookSecurity).get("saved_players");
            if (allPlayersSecurity == null || !allPlayersSecurity.isJsonObject()) {
                allPlayersSecurity = new JsonObject();
                ((JsonObject) bookSecurity).add("saved_players", allPlayersSecurity);
            }
            try {
                JsonObject bookJson = super.plugin.getAPI().getDistribution().convertBookToJson(book);
                String bookHash = Hashing.sha256().hashString(bookJson.toString(), StandardCharsets.UTF_8).toString();
                ((JsonObject) allBooksSecurity).add(bookHash, bookJson);
                JsonElement playerSecurity = ((JsonObject) allPlayersSecurity).get(uuid.toString());
                if (playerSecurity == null || !playerSecurity.isJsonObject()) {
                    playerSecurity = new JsonObject();
                    ((JsonObject) allPlayersSecurity).add(uuid.toString(), playerSecurity);
                }
                ((JsonObject) playerSecurity).add(String.valueOf(date.getTime()), new JsonPrimitive(bookHash));
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        });
    }

    @Override
    protected Future<ItemStack> getSecurityBookStack(UUID uuid, Date date) {
        return super.cache.poolExecutor.submit(() -> {
            JsonElement bookSecurity = this.jsonStorage.get("book_security");
            if (bookSecurity == null || !bookSecurity.isJsonObject()) return null;
            JsonElement allBooksSecurity = ((JsonObject) bookSecurity).get("saved_books");
            if (allBooksSecurity == null || !allBooksSecurity.isJsonObject()) return null;
            JsonElement allPlayersSecurity = ((JsonObject) bookSecurity).get("saved_players");
            if (allPlayersSecurity == null || !allPlayersSecurity.isJsonObject()) return null;
            JsonElement playerSecurity = ((JsonObject) allPlayersSecurity).get(uuid.toString());
            if (playerSecurity == null || !playerSecurity.isJsonObject()) return null;
            JsonElement bookHash = ((JsonObject) playerSecurity).get(String.valueOf(date.getTime()));
            if (bookHash == null || !bookHash.isJsonPrimitive()) return null;
            JsonElement book = ((JsonObject) allBooksSecurity).get(bookHash.getAsString());
            if (book == null || !book.isJsonObject()) return null;
            return super.plugin.getAPI().getDistribution().convertJsonToBook((JsonObject) book);
        });
    }

    @Override
    protected Queue<Triplet<Integer, Side, ItemStack>> getAllNPCBookStacks(AtomicBoolean failed) {
        Queue<Triplet<Integer, Side, ItemStack>> queue = new LinkedList<>();
        JsonElement npcBooksJson = this.jsonStorage.get("npcbooks");
        if (npcBooksJson == null || !npcBooksJson.isJsonObject()) return queue;
        for (String npcId : ((JsonObject) npcBooksJson).keySet()) {
            JsonElement npcBookJson = ((JsonObject) npcBooksJson).get(npcId);
            if (npcBookJson == null || !npcBookJson.isJsonObject()) continue;
            for (String side : ((JsonObject) npcBookJson).keySet())
                try {
                    JsonElement npcBookJsonSide = ((JsonObject) npcBookJson).get(side);
                    if (npcBookJsonSide == null || !npcBookJsonSide.isJsonObject()) continue;
                    queue.add(Triplet.of(Integer.parseInt(npcId), Side.fromString(side), super.plugin.getAPI().getDistribution().convertJsonToBook((JsonObject) npcBookJsonSide)));
                } catch (IllegalAccessException ignored) {
                }
        }
        return queue;
    }

    @Override
    protected Queue<Pair<String, ItemStack>> getAllFilterBookStacks(AtomicBoolean failed) {
        Queue<Pair<String, ItemStack>> queue = new LinkedList<>();
        JsonElement filtersJson = this.jsonStorage.get("filters");
        if (filtersJson == null || !filtersJson.isJsonObject()) return queue;
        for (String filterName : ((JsonObject) filtersJson).keySet())
            try {
                JsonElement filterJson = ((JsonObject) filtersJson).get(filterName);
                if (filterJson == null || !filterJson.isJsonObject()) continue;
                queue.add(Pair.of(filterName, super.plugin.getAPI().getDistribution().convertJsonToBook((JsonObject) filterJson)));
            } catch (IllegalAccessException ignored) {
            }
        return queue;
    }

    @Override
    protected Queue<Triplet<String, String, String>> getAllCommandFilterStacks(AtomicBoolean failed) {
        Queue<Triplet<String, String, String>> queue = new LinkedList<>();
        JsonElement commandsJson = this.jsonStorage.get("commands");
        if (commandsJson == null || !commandsJson.isJsonObject()) return queue;
        for (String cmd : ((JsonObject) commandsJson).keySet()) {
            JsonElement commandJson = ((JsonObject) commandsJson).get(cmd);
            if (commandJson == null || !commandJson.isJsonPrimitive()) return null;
            String[] data = commandJson.getAsString().split(";");
            queue.add(Triplet.of(cmd, data[0], data[1]));
        }
        return queue;
    }

    @Override
    protected Queue<Triplet<UUID, Date, ItemStack>> getAllBookSecurityStacks(AtomicBoolean failed) {
        Queue<Triplet<UUID, Date, ItemStack>> queue = new LinkedList<>();
        JsonElement bookSecurity = this.jsonStorage.get("book_security");
        if (bookSecurity == null || !bookSecurity.isJsonObject()) return queue;
        JsonElement allBooksSecurity = ((JsonObject) bookSecurity).get("saved_books");
        if (allBooksSecurity == null || !allBooksSecurity.isJsonObject()) return queue;
        JsonElement allPlayersSecurity = ((JsonObject) bookSecurity).get("saved_players");
        if (allPlayersSecurity == null || !allPlayersSecurity.isJsonObject()) return queue;
        for (String uuidString : ((JsonObject) allPlayersSecurity).keySet())
            try {
                JsonElement playerSecurity = ((JsonObject) allPlayersSecurity).get(uuidString);
                if (playerSecurity == null || !playerSecurity.isJsonObject()) continue;
                for (String timeStamp : ((JsonObject) playerSecurity).keySet())
                    try {
                        JsonElement bookHashJson = ((JsonObject) playerSecurity).get(timeStamp);
                        if (bookHashJson == null || !bookHashJson.isJsonPrimitive()) continue;
                        String bookHash = bookHashJson.getAsString();
                        JsonElement bookJson = ((JsonObject) allBooksSecurity).get(bookHash);
                        if (bookJson == null || !bookJson.isJsonObject()) continue;
                        ItemStack book = super.plugin.getAPI().getDistribution().convertJsonToBook((JsonObject) bookJson);
                        Date date = new Date(Long.parseLong(timeStamp));
                        queue.add(Triplet.of(UUID.fromString(uuidString), date, book));
                    } catch (IllegalAccessException ignored) {
                    }
            } catch (IllegalArgumentException ignored) {
            }
        return queue;
    }

    @Override
    protected void setAllNPCBookStacks(Queue<Triplet<Integer, Side, ItemStack>> queue, AtomicBoolean failed) {
        JsonElement npcBooksJson = this.jsonStorage.get("npcbooks");
        if (npcBooksJson == null || !npcBooksJson.isJsonObject()) {
            npcBooksJson = new JsonObject();
            this.jsonStorage.add("npcbooks", npcBooksJson);
        }
        Triplet<Integer, Side, ItemStack> triplet;
        while ((triplet = queue.poll()) != null)
            try {
                JsonElement npcBookJson = ((JsonObject) npcBooksJson).get(String.valueOf(triplet.getFirstValue()));
                if (npcBookJson == null || !npcBookJson.isJsonObject()) {
                    npcBookJson = new JsonObject();
                    ((JsonObject) npcBooksJson).add(String.valueOf(triplet.getFirstValue()), npcBookJson);
                }
                ((JsonObject) npcBookJson).add(triplet.getSecondValue().toString(), super.plugin.getAPI().getDistribution().convertBookToJson(triplet.getThirdValue()));
            } catch (IllegalAccessException ignored) {
            }
        this.writeJsonStorage();
    }

    @Override
    protected void setAllFilterBookStacks(Queue<Pair<String, ItemStack>> queue, AtomicBoolean failed) {
        JsonElement filtersJson = this.jsonStorage.get("filters");
        if (filtersJson == null || !filtersJson.isJsonObject()) {
            filtersJson = new JsonObject();
            this.jsonStorage.add("filters", filtersJson);
        }
        Pair<String, ItemStack> pair;
        while ((pair = queue.poll()) != null) try {
            ((JsonObject) filtersJson).add(pair.getFirstValue(), super.plugin.getAPI().getDistribution().convertBookToJson(pair.getSecondValue()));
        } catch (IllegalAccessException ignored) {
        }
        this.writeJsonStorage();
    }

    @Override
    protected void setAllCommandFilterStacks(Queue<Triplet<String, String, String>> queue, AtomicBoolean
            failed) {
        JsonElement commandsJson = this.jsonStorage.get("commands");
        if (commandsJson == null || !commandsJson.isJsonObject()) {
            commandsJson = new JsonObject();
            this.jsonStorage.add("commands", commandsJson);
        }
        Triplet<String, String, String> triplet; // command name, filter name, permission!
        while ((triplet = queue.poll()) != null)
            ((JsonObject) commandsJson).add(triplet.getFirstValue(), new JsonPrimitive(triplet.getSecondValue() + ";" + triplet.getThirdValue()));
        this.writeJsonStorage();
    }

    @Override
    protected void setAllBookSecurityStacks(Queue<Triplet<UUID, Date, ItemStack>> queue, AtomicBoolean failed) {
        JsonElement bookSecurity = this.jsonStorage.get("book_security");
        if (bookSecurity == null || !bookSecurity.isJsonObject()) {
            bookSecurity = new JsonObject();
            this.jsonStorage.add("book_security", bookSecurity);
        }
        JsonElement allBooksSecurity = ((JsonObject) bookSecurity).get("saved_books");
        if (allBooksSecurity == null || !allBooksSecurity.isJsonObject()) {
            allBooksSecurity = new JsonObject();
            ((JsonObject) bookSecurity).add("saved_books", allBooksSecurity);
        }
        JsonElement allPlayersSecurity = ((JsonObject) bookSecurity).get("saved_players");
        if (allPlayersSecurity == null || !allPlayersSecurity.isJsonObject()) {
            allPlayersSecurity = new JsonObject();
            ((JsonObject) bookSecurity).add("saved_players", allPlayersSecurity);
        }
        Triplet<UUID, Date, ItemStack> triplet;
        while ((triplet = queue.poll()) != null)
            try {
                JsonObject bookJson = super.plugin.getAPI().getDistribution().convertBookToJson(triplet.getThirdValue());
                String bookHash = Hashing.sha256().hashString(bookJson.toString(), StandardCharsets.UTF_8).toString();
                ((JsonObject) allBooksSecurity).add(bookHash, bookJson);
                JsonElement playerSecurity = ((JsonObject) allPlayersSecurity).get(triplet.getFirstValue().toString());
                if (playerSecurity == null || !playerSecurity.isJsonObject()) {
                    playerSecurity = new JsonObject();
                    ((JsonObject) allPlayersSecurity).add(triplet.getFirstValue().toString(), playerSecurity);
                }
                ((JsonObject) playerSecurity).add(String.valueOf(triplet.getSecondValue().getTime()), new JsonPrimitive(bookHash));
            } catch (IllegalAccessException ignored) {
            }
        this.writeJsonStorage();
    }

    @Override
    protected void removeNPCBookStack(int npcId, Side side) {
        super.cache.npcs.remove(Pair.of(npcId, side));
        super.cache.poolExecutor.submit(() -> {
            JsonElement npcBooksJson = this.jsonStorage.get("npcbooks");
            if (npcBooksJson == null || !npcBooksJson.isJsonObject()) return;
            JsonElement npcBookJson = ((JsonObject) npcBooksJson).get(String.valueOf(npcId));
            if (npcBookJson == null || !npcBookJson.isJsonObject()) return;
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
            if (commandsJson == null || !commandsJson.isJsonObject()) return;
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
            if (filtersJson == null || !filtersJson.isJsonObject()) return;
            ((JsonObject) filtersJson).remove(filterName);
            this.needsAutoSave = true;
        });
    }

    @Override
    protected Map<UUID, Set<Date>> cleanOldSecurityBookStacks() {
        try {
            super.lock.lock();
            Map<UUID, Set<Date>> map = new HashMap<>();
            JsonElement bookSecurity = this.jsonStorage.get("book_security");
            if (bookSecurity == null || !bookSecurity.isJsonObject()) return map;
            JsonElement allPlayersSecurity = ((JsonObject) bookSecurity).get("saved_players");
            if (allPlayersSecurity == null || !allPlayersSecurity.isJsonObject()) return map;
            Set<String> existingBooks = new HashSet<>();

            for (Iterator<String> iter = ((JsonObject) allPlayersSecurity).keySet().iterator(); iter.hasNext(); ) {
                String uuidString = iter.next();
                JsonElement playerSecurity = ((JsonObject) allPlayersSecurity).get(uuidString);
                if (playerSecurity == null || !playerSecurity.isJsonObject()) continue;
                for (Iterator<String> it = ((JsonObject) playerSecurity).keySet().iterator(); it.hasNext(); ) {
                    String timeStamp = it.next();
                    try {
                        Date date = new Date(Long.parseLong(timeStamp));
                        if (date.getTime() < System.currentTimeMillis() - TimeUnit.DAYS.toMillis(this.purgeSecurityBooksOlderThan)) {
                            it.remove();
                            UUID uuid = UUID.fromString(uuidString);
                            map.computeIfAbsent(uuid, key -> new HashSet<>()).add(date);
                        } else
                            existingBooks.add(((JsonObject) playerSecurity).get(timeStamp).getAsString());
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                if (((JsonObject) playerSecurity).keySet().isEmpty()) iter.remove();
            }

            JsonElement allBooksSecurity = ((JsonObject) bookSecurity).get("saved_books");
            if (allBooksSecurity == null || !allBooksSecurity.isJsonObject()) return map;

            ((JsonObject) allBooksSecurity).keySet().removeIf(bookHash -> !existingBooks.contains(bookHash));
            if (((JsonObject) allBooksSecurity).keySet().isEmpty())
                ((JsonObject) bookSecurity).remove("saved_books");
            if (((JsonObject) allPlayersSecurity).keySet().isEmpty())
                ((JsonObject) bookSecurity).remove("saved_players");
            if (((JsonObject) bookSecurity).keySet().isEmpty())
                this.jsonStorage.remove("book_security");
            if (!map.isEmpty())
                this.writeJsonStorage();
            return map;
        } finally {
            super.lock.unlock();
        }
    }
}
