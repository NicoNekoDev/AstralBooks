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
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import ro.nicuch.citizensbooks.dist.Distribution;
import ro.nicuch.citizensbooks.item.EmptyItemData;
import ro.nicuch.citizensbooks.item.ItemData;
import ro.nicuch.citizensbooks.item.NBTAPIItemData;
import ro.nicuch.citizensbooks.item.PersistentItemData;
import ro.nicuch.citizensbooks.utils.CipherUtil;
import ro.nicuch.citizensbooks.utils.UpdateChecker;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CitizensBooksAPI {
    private final CitizensBooksPlugin plugin;
    private final CitizensBooksDatabase database;
    private Distribution distribution = null;
    private final Map<String, Pair<ItemStack, Path>> filters = new HashMap<>();
    private final File filtersDirectory;
    private final Pattern filterNamePattern = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private final File joinBookFile;
    private final File savedBooksFile;
    private JsonObject jsonSavedBooks = new JsonObject();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public boolean noNBTAPIRequired() {
        return this.distribution.noNBTAPIRequired();
    }

    public CitizensBooksAPI(CitizensBooksPlugin plugin) {
        this.plugin = plugin;
        this.database = this.plugin.getDatabase();
        this.filtersDirectory = new File(plugin.getDataFolder() + File.separator + "filters");
        this.joinBookFile = new File(plugin.getDataFolder() + File.separator + "join_book.json");
        this.savedBooksFile = new File(plugin.getDataFolder() + File.separator + "saved_books.json");
    }

    public ItemData itemDataFactory(ItemStack stack) {
        if (this.noNBTAPIRequired()) {
            return new PersistentItemData(stack);
        } else if (this.plugin.isNBTAPIEnabled()) {
            return new NBTAPIItemData(stack);
        }
        return new EmptyItemData(stack);
    }

    public ItemStack encryptBook(ItemStack book, String password, boolean lock) {
        ItemData data = this.itemDataFactory(book);
        JsonObject bookContent = this.distribution.convertBookToJson(book);
        try {
            JsonArray bookPages = bookContent.getAsJsonArray("pages");
            JsonArray encryptedPages = new JsonArray();
            for (JsonElement bookPage : bookPages) {
                if (lock)
                    encryptedPages.add(this.encrypt(this.distribution.getGson().toJsonTree(bookPage), password));
                else
                    encryptedPages.add(this.decrypt(this.distribution.getGson().toJsonTree(bookPage), password));
            }
            bookContent.add("pages", encryptedPages);
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to cipher!", ex);
        }
        return data.copyDataToStack(this.distribution.convertJsonToBook(bookContent));
    }

    private JsonElement encrypt(JsonElement jsonElement, String password) throws Exception {
        if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
            if (primitive.isString()) {
                return new JsonPrimitive(CipherUtil.encrypt(password, primitive.getAsString()));
            }
        } else if (jsonElement.isJsonObject()) {
            JsonObject object = jsonElement.getAsJsonObject();
            JsonObject copy = new JsonObject();
            for (Map.Entry<String, JsonElement> elementEntry : object.entrySet()) {
                copy.add(elementEntry.getKey(), this.encrypt(elementEntry.getValue(), password));
            }
            return copy;
        } else if (jsonElement.isJsonArray()) {
            JsonArray array = jsonElement.getAsJsonArray();
            JsonArray copy = new JsonArray();
            for (JsonElement element : array) {
                copy.add(this.encrypt(element, password));
            }
            return copy;
        }
        return jsonElement;
    }

    private JsonElement decrypt(JsonElement jsonElement, String password) throws Exception {
        if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
            if (primitive.isString()) {
                return new JsonPrimitive(CipherUtil.decrypt(password, primitive.getAsString()));
            }
        } else if (jsonElement.isJsonObject()) {
            JsonObject object = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> elementEntry : object.entrySet()) {
                object.add(elementEntry.getKey(), this.decrypt(elementEntry.getValue(), password));
            }
            return object;
        } else if (jsonElement.isJsonArray()) {
            JsonArray array = jsonElement.getAsJsonArray();
            JsonArray copy = new JsonArray();
            for (JsonElement element : array) {
                copy.add(this.decrypt(element, password));
            }
            return copy;
        }
        return jsonElement;
    }

    public void reloadNPCBooks(Logger logger) {
        this.jsonSavedBooks = null;
        if (this.plugin.isDatabaseEnabled())
            return;
        if (!this.savedBooksFile.exists()) {
            try {
                InputStream input = this.plugin.getResource("saved_books.json");
                if (input == null) {
                    logger.warning("Failed to save default saved_books.json file!");
                    return;
                }
                Files.copy(input, this.savedBooksFile.toPath());
            } catch (IOException e) {
                logger.warning("Failed to save default saved_books.json file!");
                return;
            }
        }
        try (FileReader reader = new FileReader(this.savedBooksFile)) {
            this.jsonSavedBooks = this.gson.fromJson(reader, JsonObject.class);
            if (this.jsonSavedBooks == null || this.jsonSavedBooks.isJsonNull()) {
                this.jsonSavedBooks = new JsonObject();
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to reload saved_books.json!", ex);
        }
    }

    public void saveNPCBooks(Logger logger) {
        if (this.plugin.isDatabaseEnabled())
            return;
        try (FileWriter writer = new FileWriter(this.savedBooksFile)) {
            this.gson.toJson(this.jsonSavedBooks, writer);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to save saved_books.json!", ex);
        }
    }

    public void removeNPCBook(int npcId, String side) {
        Validate.isTrue(npcId >= 0, "NPC id is less than 0!");
        Validate.isTrue(side.equalsIgnoreCase("left_side") || side.equalsIgnoreCase("right_side"), "Wrong String[side], couldn't match for [ " + side + " ]!");
        if (this.plugin.isDatabaseEnabled()) {
            this.plugin.getDatabase().removeNPCBook(npcId, side);
            return;
        }
        JsonObject jsonNPCId = this.jsonSavedBooks.getAsJsonObject(String.valueOf(npcId));
        if (jsonNPCId == null || jsonNPCId.isJsonNull())
            return;
        jsonNPCId.remove(side);
        if (jsonNPCId.has("right_side") || jsonNPCId.has("left_side"))
            return;
        this.jsonSavedBooks.remove(String.valueOf(npcId));
    }

    public void putNPCBook(int npcId, String side, ItemStack book) {
        Validate.isTrue(npcId >= 0, "NPC id is less than 0!");
        Validate.isTrue(side.equalsIgnoreCase("left_side") || side.equalsIgnoreCase("right_side"), "Wrong String[side], couldn't match for [ " + side + " ]!");
        Validate.notNull(book, "The ItemStack is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Validate.isTrue(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        if (this.plugin.isDatabaseEnabled()) {
            this.plugin.getDatabase().putNPCBook(npcId, side, book);
            return;
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
    }

    public boolean hasNPCBook(int npcId, String side) {
        Validate.isTrue(npcId >= 0, "NPC id is less than 0!");
        Validate.isTrue(side.equalsIgnoreCase("left_side") || side.equalsIgnoreCase("right_side"), "Wrong String[$side], couldn't match for String[" + side + "]!");
        if (this.plugin.isDatabaseEnabled())
            return this.plugin.getDatabase().hasNPCBook(npcId, side);
        JsonObject jsonNPCId = this.jsonSavedBooks.getAsJsonObject(String.valueOf(npcId));
        if (jsonNPCId == null || jsonNPCId.isJsonNull())
            return false;
        JsonObject bookSideObject = jsonNPCId.getAsJsonObject(side);
        return bookSideObject != null && bookSideObject.isJsonNull(); // we care only about side object
    }

    public ItemStack getNPCBook(int npcId, String side, ItemStack defaultStack) {
        Validate.isTrue(npcId >= 0, "NPC id is less than 0!");
        Validate.isTrue(side.equalsIgnoreCase("left_side") || side.equalsIgnoreCase("right_side"), "Wrong String[$side], couldn't match for String[" + side + "]!");
        if (defaultStack != null)
            Validate.isTrue(defaultStack.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
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

    public void removeJoinBook() {
        this.joinBookFile.delete();
    }

    public void setJoinBook(ItemStack book) {
        Validate.notNull(book, "The ItemStack is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Validate.isTrue(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
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
        Validate.notNull(book, "The ItemStack is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Validate.isTrue(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
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
            //ex.printStackTrace();
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
        if (!this.plugin.isPlaceHolderEnabled())
            return book;
        return this.distribution.applyPlaceholders(player, book);
    }

    public ItemStack placeholderHook(Player player, ItemStack book, NPC npc) {
        if (!this.plugin.isPlaceHolderEnabled())
            return book;
        return this.distribution.applyPlaceholders(player, book, npc);
    }

    public void reloadFilters(Logger logger) {
        logger.info("Loading filters...");
        this.filters.clear();
        if (!this.filtersDirectory.exists()) {
            this.filtersDirectory.mkdirs();
            File helloWorldFile = new File(this.filtersDirectory + File.separator + "hello_world.json");
            try {
                InputStream input = this.plugin.getResource("hello_world.json");
                if (input == null) {
                    logger.warning("Failed to save default hello_world filter! This error could be ignored...");
                    return;
                }
                Files.copy(input, helloWorldFile.toPath());
            } catch (IOException e) {
                logger.warning("Failed to save default hello_world filter! This error could be ignored...");
            }
        }
        AtomicInteger successfulFile = new AtomicInteger();
        FileVisitor<Path> fileVisitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                File jsonFile = path.toFile();
                if (!jsonFile.getName().toLowerCase().endsWith(".json"))
                    return FileVisitResult.CONTINUE; // don't log non json files
                try (FileReader fileReader = new FileReader(jsonFile)) {
                    JsonObject jsonObject = CitizensBooksAPI.this.gson.fromJson(fileReader, JsonObject.class);
                    JsonPrimitive jsonFilterName = jsonObject.getAsJsonPrimitive("filter_name");
                    if (!jsonFilterName.isString()) {
                        logger.warning("Failed to load " + jsonFile.getName() + " because it doesn't have a filter name!");
                        return FileVisitResult.CONTINUE;
                    }
                    String filterName = jsonFilterName.getAsString();
                    if (!isValidName(filterName)) {
                        logger.warning("Failed to load " + jsonFile.getName() + " because it doesn't have a valid filter name!");
                        return FileVisitResult.CONTINUE;
                    }
                    JsonObject jsonBookContent = jsonObject.getAsJsonObject("book_content");
                    ItemStack book = CitizensBooksAPI.this.distribution.convertJsonToBook(jsonBookContent);
                    CitizensBooksAPI.this.filters.put(filterName, Pair.of(book, jsonFile.toPath()));
                    successfulFile.incrementAndGet();
                } catch (Exception ex) {
                    logger.warning("Failed to load " + jsonFile.getName());
                    return FileVisitResult.CONTINUE;
                }
                return FileVisitResult.CONTINUE;
            }
        };
        try {
            Files.walkFileTree(this.filtersDirectory.toPath(), fileVisitor);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int successful = successfulFile.get();
        if (successful == 0)
            logger.info("No filter was loaded!");
        else
            logger.info("Loaded " + successfulFile.get() + " filters!");
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
        Validate.notNull(filterName, "The filter name is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Validate.notEmpty(filterName, "The filter name is empty! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Validate.isTrue(this.isValidName(filterName), "Invalid characters found in filterName!");
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
        Validate.notNull(filterName, "The filter name is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Validate.isTrue(!filterName.isEmpty(), "The filter name is empty! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Validate.isTrue(this.isValidName(filterName), "Invalid characters found in filterName!");
        if (this.plugin.isDatabaseEnabled())
            return this.database.hasFilterBook(filterName);
        return this.filters.containsKey(filterName);
    }

    /**
     * Create a filter from a book
     *
     * @param filterName filter name/id
     * @param book       the book
     * @throws NullPointerException     if the book is null
     * @throws IllegalArgumentException if the book is not really a book
     */
    public void createFilter(String filterName, ItemStack book) {
        Validate.notNull(filterName, "The filter name is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Validate.notEmpty(filterName, "The filter name is empty! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Validate.notNull(book, "The ItemStack is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Validate.isTrue(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Validate.isTrue(this.isValidName(filterName), "Invalid characters found in filterName!");
        if (this.plugin.isDatabaseEnabled()) {
            this.database.putFilterBook(filterName, book);
            return;
        }
        File jsonFile = new File(this.filtersDirectory + File.separator + filterName + ".json");
        try (FileWriter fileWriter = new FileWriter(jsonFile)) {
            JsonObject jsonBookContent = this.distribution.convertBookToJson(book);
            JsonObject jsonFileObject = new JsonObject();
            jsonFileObject.add("filter_name", new JsonPrimitive(filterName));
            jsonFileObject.add("mc_version", new JsonPrimitive(this.distribution.getVersion()));
            jsonFileObject.add("book_content", jsonBookContent);
            this.gson.toJson(jsonFileObject, fileWriter);
            this.filters.put(filterName, Pair.of(book, jsonFile.toPath()));
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to create json file", ex);
        }
    }

    public Set<String> getFilters() {
        if (this.plugin.isDatabaseEnabled())
            return this.database.getFilterNames();
        return this.filters.keySet();
    }

    /**
     * Remove the filter
     *
     * @param filterName filter name/id
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void removeFilter(String filterName) {
        if (this.plugin.isDatabaseEnabled()) {
            this.database.removeFilterBook(filterName);
            return;
        }
        if (this.filters.containsKey(filterName)) {
            Pair<ItemStack, Path> link = this.filters.remove(filterName);
            File jsonFile = link.getSecondValue().toFile();
            if (jsonFile.exists())
                jsonFile.delete();
        }
    }

    protected void rightClick(Player player) {
        this.distribution.sendRightClick(player);
    }

    /**
     * Open a book for player
     *
     * @param player the player
     * @param book   the book
     * @throws NullPointerException     if the book is null
     * @throws IllegalArgumentException if the book is not really a book
     */
    public void openBook(Player player, ItemStack book) {
        player.closeInventory();
        Validate.notNull(book, "The ItemStack is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Validate.isTrue(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        int slot = player.getInventory().getHeldItemSlot();
        ItemStack old = player.getInventory().getItem(slot);
        PlayerInventory pi = player.getInventory();
        pi.setItem(slot, book);
        try {
            this.rightClick(player);
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.WARNING, "Something went wrong!", ex);
        }
        if (old != null)
            pi.setItem(slot, old.clone());
        else
            pi.setItem(slot, null);
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

    protected Optional<Player> getPlayer(String name) {
        Player player = Bukkit.getPlayer(name);
        if (player == null)
            return Optional.empty();
        return Optional.of(player);
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
