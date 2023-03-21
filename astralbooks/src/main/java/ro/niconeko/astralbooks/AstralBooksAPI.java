/*

    AstralBooks
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

package ro.niconeko.astralbooks;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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
import ro.niconeko.astralbooks.dist.Distribution;
import ro.niconeko.astralbooks.persistent.entity.EmptyEntityData;
import ro.niconeko.astralbooks.persistent.entity.EntityData;
import ro.niconeko.astralbooks.persistent.entity.NBTAPIEntityData;
import ro.niconeko.astralbooks.persistent.entity.PersistentEntityData;
import ro.niconeko.astralbooks.persistent.item.EmptyItemData;
import ro.niconeko.astralbooks.persistent.item.ItemData;
import ro.niconeko.astralbooks.persistent.item.NBTAPIItemData;
import ro.niconeko.astralbooks.persistent.item.PersistentItemData;
import ro.niconeko.astralbooks.utils.PersistentKey;
import ro.niconeko.astralbooks.utils.Side;
import ro.niconeko.astralbooks.utils.UpdateChecker;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AstralBooksAPI {
    private final AstralBooksPlugin plugin;
    private Distribution distribution = null;
    private final Map<Chunk, Set<Block>> blocksPairedToChunk = new HashMap<>();
    private final Map<Block, Pair<ItemStack, ItemStack>> clickableBlocks = new HashMap<>();
    private final Pattern namePattern = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private final File joinBookFile;

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public AstralBooksAPI(final AstralBooksPlugin plugin) {
        this.plugin = plugin;
        this.joinBookFile = new File(plugin.getDataFolder() + File.separator + "join_book.json");
    }

    public boolean noNBTAPIRequired() {
        return this.distribution.noNBTAPIRequired();
    }

    public void deployBooksForChunk(Chunk chunk, Map<Block, Pair<ItemStack, ItemStack>> clickableBlocks) {
        this.clickableBlocks.putAll(clickableBlocks);
        this.blocksPairedToChunk.put(chunk, new HashSet<>(clickableBlocks.keySet()));
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
        Set<Block> blocks = this.blocksPairedToChunk.get(chunk);
        if (blocks != null)
            for (Block block : blocks) {
                reducedMap.put(block, this.clickableBlocks.get(block));
            }
        return reducedMap;
    }

    public Map<Block, Pair<ItemStack, ItemStack>> getClickableBlocks() {
        return this.clickableBlocks;
    }

    public ItemStack getBookOfBlock(Block block, Side side) {
        Pair<ItemStack, ItemStack> pairBook = clickableBlocks.get(block);
        if (pairBook == null)
            return null;
        return switch (side) {
            case LEFT -> pairBook.getFirstValue();
            case RIGHT -> pairBook.getSecondValue();
        };
    }

    public ItemStack getBookOfEntity(Entity entity, Side side) {
        try {
            String stringJsonBook = switch (side) {
                case RIGHT -> this.entityDataFactory(entity).getString(PersistentKey.ENTITY_RIGHT_BOOK);
                case LEFT -> this.entityDataFactory(entity).getString(PersistentKey.ENTITY_LEFT_BOOK);
            };
            if (stringJsonBook == null)
                return null;
            return this.distribution.convertJsonToBook(GSON.fromJson(stringJsonBook, JsonObject.class));
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
        Preconditions.checkNotNull(book, "The ItemStack is null! This is not an error with AstralBooks," +
                " so please don't report it. Make sure the plugins that uses AstralBooks as dependency are correctly configured.");
        Preconditions.checkArgument(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with AstralBooks," +
                " so please don't report it. Make sure the plugins that uses AstralBooks as dependency are correctly configured.");
        this.putBookInJsonFile(this.joinBookFile, book);
    }

    public ItemStack getBookFromJsonFile(File jsonFile) throws JsonParseException {
        try (FileReader fileReader = new FileReader(jsonFile)) {
            JsonObject jsonBookContent = GSON.fromJson(fileReader, JsonObject.class);
            return this.distribution.convertJsonToBook(jsonBookContent);
        } catch (Exception ex) {
            throw new JsonParseException("Failed to parse the json file " + jsonFile.getName());
        }
    }

    public void putBookInJsonFile(File jsonFile, ItemStack book) throws JsonParseException {
        Preconditions.checkNotNull(book, "The ItemStack is null! This is not an error with AstralBooks," +
                " so please don't report it. Make sure the plugins that uses AstralBooks as dependency are correctly configured.");
        Preconditions.checkArgument(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with AstralBooks," +
                " so please don't report it. Make sure the plugins that uses AstralBooks as dependency are correctly configured.");
        try (FileWriter fileWriter = new FileWriter(jsonFile)) {
            JsonObject jsonBookContent = this.distribution.convertBookToJson(book);
            GSON.toJson(jsonBookContent, fileWriter);
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
            final Class<?> clazz = Class.forName("ro.niconeko.AstralBooks.dist." + version + ".DistributionHandler");
            if (Distribution.class.isAssignableFrom(clazz)) {
                this.plugin.getLogger().info("Loading support for version " + version + "...");
                TripletFunction<Player, String, Optional<NPC>, String> papiString = (player, arg, optionalNPC) -> {
                    if (!AstralBooksAPI.this.plugin.isPlaceholderAPIEnabled())
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
                    if (!AstralBooksAPI.this.plugin.isPlaceholderAPIEnabled())
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
            this.plugin.getLogger().warning("AstralBooks is incompatible with your server version " + version + "... ");
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
            if (!this.plugin.isPlaceholderAPIEnabled())
                return book;
            return this.distribution.applyPlaceholders(player, book);
        } catch (IllegalAccessException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed JSON book!", ex);
            return book;
        }
    }

    public ItemStack placeholderHook(Player player, ItemStack book, NPC npc) {
        try {
            if (!this.plugin.isPlaceholderAPIEnabled())
                return book;
            return this.distribution.applyPlaceholders(player, book, npc);
        } catch (IllegalAccessException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed JSON book!", ex);
            return book;
        }
    }

    public boolean isValidName(String filterName) {
        if (filterName == null)
            return false;
        if (filterName.isEmpty())
            return false;
        return this.namePattern.matcher(filterName).matches();
    }

    public Distribution getDistribution() {
        return this.distribution;
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
        Preconditions.checkNotNull(book, "The ItemStack is null! This is not an error with AstralBooks," +
                " so please don't report it. Make sure the plugins that uses AstralBooks as dependency are correctly configured.");
        Preconditions.checkArgument(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with AstralBooks," +
                " so please don't report it. Make sure the plugins that uses AstralBooks as dependency are correctly configured.");
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
            Optional<LuckPerms> luckPerms = this.plugin.isLuckPermsEnabled() ? Optional.of(this.plugin.getLuckPerms()) : Optional.empty(); //If LuckPerms not enabled, this will return empty
            Optional<Permission> vaultPerms = this.plugin.isVaultEnabled() ? Optional.of(this.plugin.getVaultPerms()) : Optional.empty(); //If vault not enabled or luckperms is used, this will return empty

            return (luckPerms.isPresent() && this.hasLuckPermission(luckPerms.get().getUserManager().getUser(sender.getName()), permission)) ||
                    (vaultPerms.isPresent() && vaultPerms.get().has(sender, permission)) || sender.hasPermission(permission);
        } catch (NullPointerException ex) {
            return false;
        }
    }

    protected boolean hasLuckPermission(User user, String permission) {
        if (user == null)
            throw new NullPointerException();
        ContextManager contextManager = this.plugin.getLuckPerms().getContextManager();
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
