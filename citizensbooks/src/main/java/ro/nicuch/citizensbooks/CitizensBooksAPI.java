/*

    CitizensBooks
    Copyright (c) 2022 @ Drăghiciu 'NicoNekoDev' Nicolae

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */

package ro.nicuch.citizensbooks;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.func.TripletFunction;
import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.npc.NPC;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ContextManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import ro.nicuch.citizensbooks.dist.Distribution;
import ro.nicuch.citizensbooks.persistent.entity.EmptyEntityData;
import ro.nicuch.citizensbooks.persistent.entity.EntityData;
import ro.nicuch.citizensbooks.persistent.entity.NBTAPIEntityData;
import ro.nicuch.citizensbooks.persistent.entity.PersistentEntityData;
import ro.nicuch.citizensbooks.persistent.item.EmptyItemData;
import ro.nicuch.citizensbooks.persistent.item.ItemData;
import ro.nicuch.citizensbooks.persistent.item.NBTAPIItemData;
import ro.nicuch.citizensbooks.persistent.item.PersistentItemData;
import ro.nicuch.citizensbooks.utils.PersistentKey;
import ro.nicuch.citizensbooks.utils.Side;
import ro.nicuch.citizensbooks.utils.UpdateChecker;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CitizensBooksAPI {
    private final CitizensBooksPlugin plugin;
    private final CitizensBooksDatabase database;
    private Distribution distribution = null;
    private final Map<String, Pair<ItemStack, Path>> filters = new HashMap<>();
    private final Map<Chunk, Set<Block>> blocksPairedToChunk = new HashMap<>();
    private final Map<Block, Pair<ItemStack, ItemStack>> clickableBlocks = new HashMap<>();
    private final File filtersDirectory;
    private final Pattern filterNamePattern = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private final File joinBookFile;
    private final File savedBooksFile;
    private JsonObject jsonSavedBooks = new JsonObject();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public CitizensBooksAPI(final CitizensBooksPlugin plugin) {
        this.plugin = plugin;
        this.database = this.plugin.getDatabase();
        this.filtersDirectory = new File(plugin.getDataFolder() + File.separator + "filters");
        this.joinBookFile = new File(plugin.getDataFolder() + File.separator + "join_book.json");
        this.savedBooksFile = new File(plugin.getDataFolder() + File.separator + "saved_books.json");
    }

    public boolean noNBTAPIRequired() {
        return this.distribution.noNBTAPIRequired();
    }

    public void deployBooksForChunk(Chunk chunk, Map<Block, Pair<ItemStack, ItemStack>> clickableBlocks) {
        this.clickableBlocks.putAll(clickableBlocks);
        this.blocksPairedToChunk.put(chunk, clickableBlocks.keySet());
    }

    public void concentrateBooksForChunk(Chunk chunk) {
        Set<Block> blocksToRemove = this.blocksPairedToChunk.remove(chunk);
        if (blocksToRemove != null)
            for (Block block : blocksToRemove)
                this.clickableBlocks.remove(block);
    }

    public Set<Block> getBlocksPairedToChunk(Chunk chunk) {
        return this.blocksPairedToChunk.get(chunk);
    }

    public Map<Block, Pair<ItemStack, ItemStack>> getBlocksEntriesPairedToChunk(Chunk chunk) {
        Map<Block, Pair<ItemStack, ItemStack>> reducedMap = new HashMap<>();
        for (Block block : this.blocksPairedToChunk.get(chunk)) {
            reducedMap.put(block, this.clickableBlocks.get(block));
        }
        return reducedMap;
    }

    public Map<Block, Pair<ItemStack, ItemStack>> getClickableBlocks() {
        return this.clickableBlocks;
    }

    public ItemStack getBookOfBlock(Block block, Side side) {
        return switch (side) {
            case LEFT -> clickableBlocks.get(block).getFirstValue();
            case RIGHT -> clickableBlocks.get(block).getSecondValue();
        };
    }

    public ItemStack getBookOfEntity(Entity entity, Side side) {
        try {
            String stringJsonBook = switch (side) {
                case RIGHT -> this.entityDataFactory(entity).getString(PersistentKey.ENTITY_RIGHT_BOOK);
                case LEFT -> this.entityDataFactory(entity).getString(PersistentKey.ENTITY_LEFT_BOOK);
            };
            return this.distribution.convertJsonToBook(this.distribution.getGson().fromJson(stringJsonBook, JsonObject.class));
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    public void removeBookOfEntity(Entity entity) {
        this.removeBookOfEntity(entity, null);
    }

    public void removeBookOfEntity(Entity entity, Side side) {
        if (side == null) {
            this.entityDataFactory(entity).removeKey(PersistentKey.ENTITY_LEFT_BOOK);
            this.entityDataFactory(entity).removeKey(PersistentKey.ENTITY_RIGHT_BOOK);
            return;
        }
        switch (side) {
            case LEFT -> this.entityDataFactory(entity).removeKey(PersistentKey.ENTITY_LEFT_BOOK);
            case RIGHT -> this.entityDataFactory(entity).removeKey(PersistentKey.ENTITY_RIGHT_BOOK);
        }
    }

    public void removeBookOfBlock(Block block) {
        this.removeBookOfBlock(block, null);
    }

    public void removeBookOfBlock(Block block, Side side) {
        if (side == null) {
            clickableBlocks.remove(block);
            return;
        }
        switch (side) {
            case LEFT -> {
                Pair<ItemStack, ItemStack> pair = clickableBlocks.remove(block);
                if (pair == null || pair.getSecondValue() == null) break;
                clickableBlocks.put(block, Pair.of(null, pair.getSecondValue()));
            }
            case RIGHT -> {
                Pair<ItemStack, ItemStack> pair = clickableBlocks.remove(block);
                if (pair == null || pair.getFirstValue() == null) break;
                clickableBlocks.put(block, Pair.of(pair.getFirstValue(), null));
            }
        }
    }

    public void putBookOnEntity(Entity entity, ItemStack book, Side side) {
        try {
            String stringJsonBook = this.distribution.convertBookToJson(book).toString();
            switch (side) {
                case LEFT -> this.entityDataFactory(entity).putString(PersistentKey.ENTITY_LEFT_BOOK, stringJsonBook);
                case RIGHT -> this.entityDataFactory(entity).putString(PersistentKey.ENTITY_RIGHT_BOOK, stringJsonBook);
            }
        } catch (IllegalAccessException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to put book on entity!", ex);
        }
    }

    public void putBookOnBlock(Block block, ItemStack book, Side side) {
        switch (side) {
            case LEFT -> {
                Pair<ItemStack, ItemStack> pair = clickableBlocks.remove(block);
                clickableBlocks.put(block, Pair.of(book, pair == null ? null : pair.getSecondValue()));
            }
            case RIGHT -> {
                Pair<ItemStack, ItemStack> pair = clickableBlocks.remove(block);
                clickableBlocks.put(block, Pair.of(pair == null ? null : pair.getFirstValue(), book));
            }
        }
        this.blocksPairedToChunk.computeIfAbsent(block.getChunk(), k -> new HashSet<>()).add(block);
    }

    public LinkedList<String[]> getListOfFilters() {
        LinkedList<String[]> resultedFilters = new LinkedList<>();
        LinkedList<String> sortedFilters = new LinkedList<>(filters.keySet());
        String pooledResult;
        while ((pooledResult = sortedFilters.poll()) != null) {
            String[] page = new String[10];
            page[0] = pooledResult;
            for (int n = 1; n < 10; n++) {
                page[n] = sortedFilters.poll();
            }
            resultedFilters.add(page);
        }
        return resultedFilters;
    }

    public ItemData itemDataFactory(ItemStack stack) {
        if (this.noNBTAPIRequired()) {
            return new PersistentItemData(stack);
        } else if (this.plugin.isNBTAPIEnabled()) {
            return new NBTAPIItemData(stack);
        }
        return new EmptyItemData(stack);
    }

    public EntityData entityDataFactory(Entity entity) {
        if (this.noNBTAPIRequired()) {
            return new PersistentEntityData(entity);
        } else if (this.plugin.isNBTAPIEnabled()) {
            return new NBTAPIEntityData(entity);
        }
        return new EmptyEntityData();
    }

    /**
     * @return if successful
     */
    public boolean reloadNPCBooks() {
        this.jsonSavedBooks = null;
        if (this.plugin.isDatabaseEnabled())
            return true;
        if (!this.savedBooksFile.exists()) {
            try {
                InputStream input = this.plugin.getResource("saved_books.json");
                if (input == null) {
                    this.plugin.getLogger().warning("Failed to save default saved_books.json file!");
                    return false;
                }
                Files.copy(input, this.savedBooksFile.toPath());
            } catch (IOException e) {
                this.plugin.getLogger().warning("Failed to save default saved_books.json file!");
                return false;
            }
        }
        try (FileReader reader = new FileReader(this.savedBooksFile)) {
            this.jsonSavedBooks = this.gson.fromJson(reader, JsonObject.class);
            if (this.jsonSavedBooks == null || this.jsonSavedBooks.isJsonNull()) {
                this.jsonSavedBooks = new JsonObject();
            }
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to reload saved_books.json!", ex);
            return false;
        }
        return true;
    }

    /**
     * @return if successful
     */
    public boolean saveNPCBooks() {
        if (this.plugin.isDatabaseEnabled())
            return true;
        try (FileWriter writer = new FileWriter(this.savedBooksFile)) {
            this.gson.toJson(this.jsonSavedBooks, writer);
            return true;
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to save saved_books.json!", ex);
            return false;
        }
    }

    /**
     * \
     *
     * @param npcId the id of the NPC
     * @param side  the side
     * @return if successful
     */
    public boolean removeNPCBook(int npcId, Side side) {
        try {
            Preconditions.checkArgument(npcId >= 0, "NPC id is less than 0!");
            if (this.plugin.isDatabaseEnabled()) {
                this.plugin.getDatabase().removeNPCBook(npcId, side);
                return true;
            }
            JsonObject jsonNPCId = this.jsonSavedBooks.getAsJsonObject(String.valueOf(npcId));
            if (jsonNPCId == null || jsonNPCId.isJsonNull())
                return true;
            jsonNPCId.remove(side.toString());
            if (jsonNPCId.has("right_side") || jsonNPCId.has("left_side"))
                return true;
            this.jsonSavedBooks.remove(String.valueOf(npcId));
            return true;
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to remove NPC.", ex);
            return false;
        }
    }

    /**
     * @param npcId the id of the NPC
     * @param side  if left or right click
     * @param book  the book to put
     * @return if successful
     */
    public boolean putNPCBook(int npcId, String side, ItemStack book) {
        try {
            Preconditions.checkArgument(npcId >= 0, "NPC id is less than 0!");
            Preconditions.checkArgument(side.equalsIgnoreCase("left_side") || side.equalsIgnoreCase("right_side"), "Wrong String[side], couldn't match for [ " + side + " ]!");
            Preconditions.checkNotNull(book, "The ItemStack is null! This is not an error with CitizensBooks," +
                    " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
            Preconditions.checkArgument(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
                    " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
            if (this.plugin.isDatabaseEnabled()) {
                this.plugin.getDatabase().putNPCBook(npcId, side, book);
                return true;
            }
            JsonObject bookSideObject = new JsonObject();
            bookSideObject.add("book_content", this.distribution.convertBookToJson(book));
            bookSideObject.add("mc_version", new JsonPrimitive(this.distribution.getVersion()));

            String id = String.valueOf(npcId);

            JsonObject jsonNPCId = this.jsonSavedBooks.getAsJsonObject(id);
            if (jsonNPCId == null)
                jsonNPCId = new JsonObject();
            jsonNPCId.add(side, bookSideObject);

            this.jsonSavedBooks.add(id, jsonNPCId);
            return true;
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed JSON book!", ex);
            return false;
        }
    }

    public boolean hasNPCBook(int npcId, String side) {
        Preconditions.checkArgument(npcId >= 0, "NPC id is less than 0!");
        Preconditions.checkArgument(side.equalsIgnoreCase("left_side") || side.equalsIgnoreCase("right_side"), "Wrong String[$side], couldn't match for String[" + side + "]!");
        if (this.plugin.isDatabaseEnabled())
            return this.plugin.getDatabase().hasNPCBook(npcId, side);
        JsonObject jsonNPCId = this.jsonSavedBooks.getAsJsonObject(String.valueOf(npcId));
        if (jsonNPCId == null || jsonNPCId.isJsonNull())
            return false;
        JsonObject bookSideObject = jsonNPCId.getAsJsonObject(side);
        return !(bookSideObject == null || bookSideObject.isJsonNull()); // we care only about side object
    }

    public ItemStack getNPCBook(int npcId, String side, ItemStack defaultStack) {
        try {
            Preconditions.checkArgument(npcId >= 0, "NPC id is less than 0!");
            Preconditions.checkArgument(side.equalsIgnoreCase("left_side") || side.equalsIgnoreCase("right_side"), "Wrong String[$side], couldn't match for String[" + side + "]!");
            if (defaultStack != null)
                Preconditions.checkArgument(defaultStack.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
                        " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
            if (this.plugin.isDatabaseEnabled())
                return this.plugin.getDatabase().getNPCBook(npcId, side, defaultStack);
            JsonObject jsonNPCId = this.jsonSavedBooks.getAsJsonObject(String.valueOf(npcId));
            if (jsonNPCId == null || jsonNPCId.isJsonNull())
                return defaultStack;
            JsonObject bookSideObject = jsonNPCId.getAsJsonObject(side);
            if (bookSideObject == null || bookSideObject.isJsonNull())
                return defaultStack;
            JsonObject bookObject = bookSideObject.getAsJsonObject("book_content");
            if (bookObject == null || bookObject.isJsonNull())
                return defaultStack;
            ItemStack stack = this.distribution.convertJsonToBook(bookObject);
            if (stack == null)
                return defaultStack;
            return stack;
        } catch (IllegalAccessException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed JSON book!", ex);
            return defaultStack;
        }
    }

    public ItemStack getNPCBook(int npcId, String side) {
        return this.getNPCBook(npcId, side, null);
    }

    public ItemStack getJoinBook() {
        if (this.joinBookFile.exists()) {
            try {
                return this.getBookFromJsonFile(this.joinBookFile);
            } catch (JsonParseException ex) {
                return null;
            }
        }
        return null;
    }

    /**
     * @return if successful
     */
    public boolean removeJoinBook() {
        return this.joinBookFile.delete();
    }

    public void setJoinBook(ItemStack book) {
        Preconditions.checkNotNull(book, "The ItemStack is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        this.putBookInJsonFile(this.joinBookFile, book);
    }

    public ItemStack getBookFromJsonFile(File jsonFile) throws JsonParseException {
        try (FileReader fileReader = new FileReader(jsonFile)) {
            JsonObject jsonBookContent = CitizensBooksAPI.this.gson.fromJson(fileReader, JsonObject.class);
            return this.distribution.convertJsonToBook(jsonBookContent);
        } catch (Exception ex) {
            throw new JsonParseException("Failed to parse the json file " + jsonFile.getName());
        }
    }

    public void putBookInJsonFile(File jsonFile, ItemStack book) throws JsonParseException {
        Preconditions.checkNotNull(book, "The ItemStack is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        try (FileWriter fileWriter = new FileWriter(jsonFile)) {
            JsonObject jsonBookContent = this.distribution.convertBookToJson(book);
            this.gson.toJson(jsonBookContent, fileWriter);
        } catch (Exception ex) {
            throw new JsonParseException("Failed to put the book the json file " + jsonFile.getName());
        }
    }

    public boolean loadDistribution() {
        // Copyright (c) mbax - Thank you for the great 'modular project' tutorial!
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);

        this.plugin.getLogger().info("Your server is running version " + version + "!");
        try {
            final Class<?> clazz = Class.forName("ro.nicuch.citizensbooks.dist." + version + ".DistributionHandler");
            if (Distribution.class.isAssignableFrom(clazz)) {
                this.plugin.getLogger().info("Loading support for version " + version + "...");
                TripletFunction<Player, String, Optional<NPC>, String> papiString = (player, arg, optionalNPC) -> {
                    if (!CitizensBooksAPI.this.plugin.isPlaceHolderEnabled())
                        return arg;
                    if (optionalNPC.isEmpty())
                        return PlaceholderAPI.setPlaceholders(player, arg);
                    else {
                        NPC npc = optionalNPC.get();
                        return PlaceholderAPI.setPlaceholders(player, arg).replace("%npc_name%", npc.getName())
                                .replace("%npc_id%", npc.getId() + "")
                                .replace("%npc_loc_x%", npc.getEntity().getLocation().getX() + "")
                                .replace("%npc_loc_y%", npc.getEntity().getLocation().getY() + "")
                                .replace("%npc_loc_z%", npc.getEntity().getLocation().getZ() + "")
                                .replace("%npc_loc_world%", npc.getEntity().getWorld().getName());
                    }
                };
                TripletFunction<Player, List<String>, Optional<NPC>, List<String>> papiStringList = (player, argList, optionalNPC) -> {
                    if (!CitizensBooksAPI.this.plugin.isPlaceHolderEnabled())
                        return argList;
                    if (optionalNPC.isEmpty())
                        return PlaceholderAPI.setPlaceholders(player, argList);
                    else {
                        NPC npc = optionalNPC.get();
                        return PlaceholderAPI.setPlaceholders(player, argList).stream().map(str -> str.replace("%npc_name%", npc.getName())
                                .replace("%npc_id%", npc.getId() + "")
                                .replace("%npc_loc_x%", npc.getEntity().getLocation().getX() + "")
                                .replace("%npc_loc_y%", npc.getEntity().getLocation().getY() + "")
                                .replace("%npc_loc_z%", npc.getEntity().getLocation().getZ() + "")
                                .replace("%npc_loc_world%", npc.getEntity().getWorld().getName())).toList();
                    }
                };
                this.distribution = (Distribution) clazz.getConstructor(TripletFunction.class, TripletFunction.class).newInstance(papiString, papiStringList);
                return true;
            }
        } catch (final Exception ex) {
            this.plugin.getLogger().warning("CitizensBooks is incompatible with your server version " + version + "... ");
            if (UpdateChecker.updateAvailable())
                this.plugin.getLogger().info("Oh look! An update is available! Go to the Spigot page and download it! It might fix the error!");
            else
                this.plugin.getLogger().warning("Please don't report this error! We try hard to update it as fast as possible!");
            return false;
        }
        return false;
    }

    public ItemStack placeholderHook(Player player, ItemStack book) {
        try {
            if (!this.plugin.isPlaceHolderEnabled())
                return book;
            return this.distribution.applyPlaceholders(player, book);
        } catch (IllegalAccessException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed JSON book!", ex);
            return book;
        }
    }

    public ItemStack placeholderHook(Player player, ItemStack book, NPC npc) {
        try {
            if (!this.plugin.isPlaceHolderEnabled())
                return book;
            return this.distribution.applyPlaceholders(player, book, npc);
        } catch (IllegalAccessException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed JSON book!", ex);
            return book;
        }
    }

    /**
     * @return if successful
     */
    public Optional<Pair<Integer, Integer>> reloadFilters() {
        this.plugin.getLogger().info("Loading filters...");
        this.filters.clear();
        if (!this.filtersDirectory.exists()) {
            if (!this.filtersDirectory.mkdirs())
                return Optional.empty();
            File helloWorldFile = new File(this.filtersDirectory + File.separator + "hello_world.json");
            try {
                InputStream input = this.plugin.getResource("hello_world.json");
                if (input == null) {
                    this.plugin.getLogger().warning("Failed to save default hello_world filter! This error could be ignored...");
                    return Optional.empty();
                }
                Files.copy(input, helloWorldFile.toPath());
            } catch (IOException e) {
                this.plugin.getLogger().warning("Failed to save default hello_world filter! This error could be ignored...");
                return Optional.empty();
            }
        }
        AtomicInteger successfulFile = new AtomicInteger();
        AtomicInteger failedFile = new AtomicInteger();
        FileVisitor<Path> fileVisitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                File jsonFile = path.toFile();
                if (!jsonFile.getName().toLowerCase().endsWith(".json")) {
                    failedFile.incrementAndGet();
                    return FileVisitResult.CONTINUE; // don't log non json files
                }
                try (FileReader fileReader = new FileReader(jsonFile)) {
                    JsonObject jsonObject = CitizensBooksAPI.this.gson.fromJson(fileReader, JsonObject.class);
                    JsonPrimitive jsonFilterName = jsonObject.getAsJsonPrimitive("filter_name");
                    if (!jsonFilterName.isString()) {
                        CitizensBooksAPI.this.plugin.getLogger().warning("Failed to load " + jsonFile.getName() + " because it doesn't have a filter name!");
                        failedFile.incrementAndGet();
                        return FileVisitResult.CONTINUE;
                    }
                    String filterName = jsonFilterName.getAsString();
                    if (!isValidName(filterName)) {
                        CitizensBooksAPI.this.plugin.getLogger().warning("Failed to load " + jsonFile.getName() + " because it doesn't have a valid filter name!");
                        failedFile.incrementAndGet();
                        return FileVisitResult.CONTINUE;
                    }
                    JsonObject jsonBookContent = jsonObject.getAsJsonObject("book_content");
                    ItemStack book = CitizensBooksAPI.this.distribution.convertJsonToBook(jsonBookContent);
                    CitizensBooksAPI.this.filters.put(filterName, Pair.of(book, jsonFile.toPath()));
                    successfulFile.incrementAndGet();
                } catch (Exception ex) {
                    CitizensBooksAPI.this.plugin.getLogger().warning("Failed to load " + jsonFile.getName());
                    failedFile.incrementAndGet();
                    return FileVisitResult.CONTINUE;
                }
                return FileVisitResult.CONTINUE;
            }
        };
        try {
            Files.walkFileTree(this.filtersDirectory.toPath(), fileVisitor);
        } catch (IOException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to walk file tree", ex);
            return Optional.empty();
        }
        if (successfulFile.get() == 0)
            this.plugin.getLogger().info("No filter was found to load!");
        else
            this.plugin.getLogger().info("Loaded " + successfulFile.get() + " filter(s) while " + failedFile.get() + " failed to load!");
        return Optional.of(Pair.of(successfulFile.get(), failedFile.get()));
    }

    public boolean isValidName(String filterName) {
        if (filterName == null)
            return false;
        if (filterName.isEmpty())
            return false;
        return this.filterNamePattern.matcher(filterName).matches();
    }

    public Distribution getDistribution() {
        return this.distribution;
    }

    /**
     * Get the book from filter
     *
     * @param filterName filter name/id
     * @return the book
     */
    public ItemStack getFilter(String filterName, ItemStack defaultItemStack) {
        Preconditions.checkNotNull(filterName, "The filter name is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(!filterName.isEmpty(), "The filter name is empty! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(this.isValidName(filterName), "Invalid characters found in filterName!");
        if (this.plugin.isDatabaseEnabled())
            return this.database.getFilterBook(filterName, new ItemStack(Material.WRITTEN_BOOK));
        return this.filters.getOrDefault(filterName, Pair.of(defaultItemStack, null)).getFirstValue();
    }

    public ItemStack getFilter(String filterName) {
        return this.getFilter(filterName, new ItemStack(Material.WRITTEN_BOOK));
    }

    /**
     * Check if filter has the book
     *
     * @param filterName filter name/id
     * @return if the filter has the book
     */
    public boolean hasFilter(String filterName) {
        Preconditions.checkNotNull(filterName, "The filter name is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(!filterName.isEmpty(), "The filter name is empty! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(this.isValidName(filterName), "Invalid characters found in filterName!");
        if (this.plugin.isDatabaseEnabled())
            return this.database.hasFilterBook(filterName);
        return this.filters.containsKey(filterName);
    }

    /**
     * Create a filter from a book
     *
     * @param filterName filter name/id
     * @param book       the book
     * @return if successfully
     * @throws NullPointerException     if the book is null
     * @throws IllegalArgumentException if the book is not really a book
     */
    public boolean createFilter(String filterName, ItemStack book) {
        try {
            Preconditions.checkNotNull(filterName, "The filter name is null! This is not an error with CitizensBooks," +
                    " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
            Preconditions.checkArgument(!filterName.isEmpty(), "The filter name is empty! This is not an error with CitizensBooks," +
                    " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
            Preconditions.checkNotNull(book, "The ItemStack is null! This is not an error with CitizensBooks," +
                    " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
            Preconditions.checkArgument(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
                    " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
            Preconditions.checkArgument(this.isValidName(filterName), "Invalid characters found in filterName!");
            if (this.plugin.isDatabaseEnabled()) {
                this.database.putFilterBook(filterName, book);
                return true;
            }
            File jsonFile = new File(this.filtersDirectory + File.separator + filterName + ".json");
            FileWriter fileWriter = new FileWriter(jsonFile);
            JsonObject jsonBookContent = this.distribution.convertBookToJson(book);
            JsonObject jsonFileObject = new JsonObject();
            jsonFileObject.add("filter_name", new JsonPrimitive(filterName));
            jsonFileObject.add("mc_version", new JsonPrimitive(this.distribution.getVersion()));
            jsonFileObject.add("book_content", jsonBookContent);
            this.gson.toJson(jsonFileObject, fileWriter);
            this.filters.put(filterName, Pair.of(book, jsonFile.toPath()));
            return true;
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to create json file", ex);
            return false;
        }
    }

    public Set<String> getFilters() {
        if (this.plugin.isDatabaseEnabled())
            return this.database.getFilterNames();
        return this.filters.keySet();
    }

    /**
     * @param filterName the filter name
     * @return if successful
     */
    public boolean removeFilter(String filterName) {
        if (this.plugin.isDatabaseEnabled()) {
            this.database.removeFilterBook(filterName);
            return true;
        }
        if (this.filters.containsKey(filterName)) {
            Pair<ItemStack, Path> link = this.filters.remove(filterName);
            File jsonFile = link.getSecondValue().toFile();
            if (jsonFile.exists())
                return jsonFile.delete();
        }
        return true;
    }

    protected void rightClick(Player player) {
        this.distribution.sendRightClick(player);
    }

    /**
     * Open a book for player
     *
     * @param player the player
     * @param book   the book
     * @return if successful
     * @throws NullPointerException     if the book is null
     * @throws IllegalArgumentException if the book is not really a book
     */
    public boolean openBook(Player player, ItemStack book) {
        player.closeInventory();
        Preconditions.checkNotNull(book, "The ItemStack is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        int slot = player.getInventory().getHeldItemSlot();
        ItemStack old = player.getInventory().getItem(slot);
        PlayerInventory pi = player.getInventory();
        pi.setItem(slot, book);
        try {
            this.rightClick(player);
        } catch (Exception ex) {
            return false;
        }
        if (old != null)
            pi.setItem(slot, old.clone());
        else
            pi.setItem(slot, null);
        return true;
    }

    public boolean hasPermission(CommandSender sender, String permission) {
        try {
            if (sender.isOp()) return true;
            Optional<LuckPerms> luckPerms = this.plugin.isLuckPermsEnabled() ? Optional.of(this.plugin.getLuckPermissions()) : Optional.empty(); //If LuckPerms not enabled, this will return empty
            Optional<Permission> vaultPerms = this.plugin.isVaultEnabled() ? Optional.of(this.plugin.getVaultPermissions()) : Optional.empty(); //If vault not enabled or luckperms is used, this will return empty

            return (luckPerms.isPresent() && this.hasLuckPermission(luckPerms.get().getUserManager().getUser(sender.getName()), permission)) ||
                    (vaultPerms.isPresent() && vaultPerms.get().has(sender, permission)) || sender.hasPermission(permission);
        } catch (NullPointerException ex) {
            return false;
        }
    }

    protected boolean hasLuckPermission(User user, String permission) {
        if (user == null)
            throw new NullPointerException();
        ContextManager contextManager = this.plugin.getLuckPermissions().getContextManager();
        return user.getCachedData().getPermissionData(
                QueryOptions.contextual(contextManager.getContext(user).orElseGet(contextManager::getStaticContext))
        ).checkPermission(permission).asBoolean();
    }

    protected List<String> getPlayers() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    public String encodeItemStack(ItemStack item) {
        if (item != null && item.getType() != Material.AIR)
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
                dataOutput.writeObject(item.serialize());
                return Base64Coder.encodeLines(outputStream.toByteArray());
            } catch (IOException ex) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to encode item!", ex);
            }
        return "";
    }

    @SuppressWarnings("unchecked")
    public ItemStack decodeItemStack(String data) {
        if (data != null && !data.isEmpty())
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
                return ItemStack.deserialize((Map<String, Object>) dataInput.readObject());
            } catch (IOException | ClassNotFoundException ex) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to decode item!", ex);
            }
        return null;
    }
}
