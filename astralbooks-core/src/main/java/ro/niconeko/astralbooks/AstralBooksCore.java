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

package ro.niconeko.astralbooks;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import ro.niconeko.astralbooks.api.AstralBooksAPI;
import ro.niconeko.astralbooks.dist.Distribution;
import ro.niconeko.astralbooks.storage.PluginStorage;
import ro.niconeko.astralbooks.utils.PersistentKey;
import ro.niconeko.astralbooks.utils.Side;
import ro.niconeko.astralbooks.utils.UpdateChecker;
import ro.niconeko.astralbooks.utils.tuples.PairTuple;
import ro.niconeko.astralbooks.utils.tuples.TripletTuple;
import ro.nicuch.citizensbooks.CitizensBooksAPI;
import ro.nicuch.citizensbooks.CitizensBooksPlugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

@SuppressWarnings("RegExpRedundantEscape")
public class AstralBooksCore implements AstralBooksAPI {
    private final AstralBooksPlugin plugin;
    @Getter private Distribution distribution = null;
    private final Map<Chunk, Set<Block>> blocksPairedToChunk = new HashMap<>();
    @Getter private final Map<Block, PairTuple<ItemStack, ItemStack>> clickableBlocks = new HashMap<>();
    private final Pattern namePattern = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private final Pattern permissionPattern = Pattern.compile("^[a-zA-Z0-9\\._-]+$");

    public static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Gson DATABASE_GSON = new GsonBuilder().create();

    public AstralBooksCore(final AstralBooksPlugin plugin) {
        this.plugin = plugin;
    }

    protected void importFromCitizensBooks() {
        PluginStorage pluginStorage = this.plugin.getPluginStorage();
        if (!Bukkit.getPluginManager().isPluginEnabled("CitizensBooks")) {
            this.plugin.getLogger().info("CitizensBooks not enabled");
            return;
        }
        CitizensBooksPlugin plugin = (CitizensBooksPlugin) Bukkit.getPluginManager().getPlugin("CitizensBooks");
        if (plugin == null) {
            this.plugin.getLogger().info("CitizensBooks not enabled");
            return;
        }
        CitizensBooksAPI api = plugin.getAPI();
        if (this.plugin.isCitizensEnabled()) {
            this.plugin.getLogger().warning("Importing NPCs books...");
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                int npcId = npc.getId();
                if (api.hasNPCBook(npcId, Side.RIGHT.toString())) {
                    if (pluginStorage.hasNPCBook(npcId, Side.RIGHT))
                        this.plugin.getLogger().info("NPC (id: " + npcId + ") right side is already set, skipping!");
                    else
                        pluginStorage.putNPCBook(npcId, Side.RIGHT, api.getNPCBook(npcId, Side.RIGHT.toString()));
                }
                if (api.hasNPCBook(npcId, Side.LEFT.toString())) {
                    if (pluginStorage.hasNPCBook(npcId, Side.LEFT))
                        this.plugin.getLogger().info("NPC (id: " + npcId + ") left side is already set, skipping!");
                    else
                        pluginStorage.putNPCBook(npcId, Side.LEFT, api.getNPCBook(npcId, Side.LEFT.toString()));
                }
            }
        } else
            this.plugin.getLogger().warning("Citizens is required for NPCs conversion!");
        this.plugin.getLogger().warning("Importing filters...");
        for (String filterName : api.getFilters()) {
            if (pluginStorage.hasFilterBook(filterName))
                this.plugin.getLogger().info("Filter (id: " + filterName + ") is already set, skipping!");
            else
                pluginStorage.putFilterBook(filterName, api.getFilter(filterName));
        }
        this.plugin.getLogger().warning("Importing commands...");
        if (plugin.getSettings().isConfigurationSection("commands"))
            //noinspection DataFlowIssue
            for (String commandName : plugin.getSettings().getConfigurationSection("commands").getKeys(false)) {
                String filterName = plugin.getSettings().getString("commands." + commandName + ".filter_name");
                String permission = plugin.getSettings().getString("commands." + commandName + ".permission");
                if (pluginStorage.hasCommandFilter(commandName))
                    this.plugin.getLogger().info("Command (id: " + commandName + ") is already set, skipping!");
                else
                    pluginStorage.putCommandFilter(commandName, filterName, permission == null || permission.isEmpty() ? "none" : permission);
            }
        if (!pluginStorage.hasJoinBook() && api.getJoinBook() != null) {
            this.plugin.getLogger().warning("Importing the join book... (please enabled it in the settings)");
            pluginStorage.setJoinBook(api.getJoinBook());
        } else this.plugin.getLogger().warning("Join book was not found");
        this.plugin.getLogger().warning("Done :)");
    }

    public void deployBooksForChunk(Chunk chunk, Map<Block, PairTuple<ItemStack, ItemStack>> clickableBlocks) {
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

    public Map<Block, PairTuple<ItemStack, ItemStack>> getBlocksEntriesPairedToChunk(Chunk chunk) {
        Map<Block, PairTuple<ItemStack, ItemStack>> reducedMap = new HashMap<>();
        Set<Block> blocks = this.blocksPairedToChunk.get(chunk);
        if (blocks != null)
            for (Block block : blocks) {
                reducedMap.put(block, this.clickableBlocks.get(block));
            }
        return reducedMap;
    }

    @Override
    public ItemStack getBookOfBlock(Block block, Side side) {
        PairTuple<ItemStack, ItemStack> pairBook = this.clickableBlocks.get(block);
        if (pairBook == null)
            return null;
        return switch (side) {
            case LEFT -> pairBook.firstValue();
            case RIGHT -> pairBook.secondValue();
        };
    }

    @Override
    public ItemStack getBookOfEntity(Entity entity, Side side) {
        try {
            String stringJsonBook = switch (side) {
                case RIGHT ->
                        entity.getPersistentDataContainer().get(PersistentKey.ENTITY_RIGHT_BOOK, PersistentDataType.STRING);
                case LEFT ->
                        entity.getPersistentDataContainer().get(PersistentKey.ENTITY_LEFT_BOOK, PersistentDataType.STRING);
            };
            if (stringJsonBook == null)
                return null;
            return this.distribution.convertJsonToBook(PRETTY_GSON.fromJson(stringJsonBook, JsonObject.class));
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    @Override
    public void removeBookOfEntity(Entity entity) {
        this.removeBookOfEntity(entity, null);
    }

    @Override
    public void removeBookOfEntity(Entity entity, Side side) {
        if (side == null) {
            entity.getPersistentDataContainer().remove(PersistentKey.ENTITY_LEFT_BOOK);
            entity.getPersistentDataContainer().remove(PersistentKey.ENTITY_RIGHT_BOOK);
            return;
        }
        switch (side) {
            case LEFT -> entity.getPersistentDataContainer().remove(PersistentKey.ENTITY_LEFT_BOOK);
            case RIGHT -> entity.getPersistentDataContainer().remove(PersistentKey.ENTITY_RIGHT_BOOK);
        }
    }

    @Override
    public void removeBookOfBlock(Block block) {
        this.removeBookOfBlock(block, null);
    }

    @Override
    public void removeBookOfBlock(Block block, Side side) {
        if (side == null) {
            clickableBlocks.remove(block);
            return;
        }
        switch (side) {
            case LEFT -> {
                PairTuple<ItemStack, ItemStack> pair = clickableBlocks.remove(block);
                if (pair == null || pair.secondValue() == null) break;
                clickableBlocks.put(block, new PairTuple<>(null, pair.secondValue()));
            }
            case RIGHT -> {
                PairTuple<ItemStack, ItemStack> pair = clickableBlocks.remove(block);
                if (pair == null || pair.firstValue() == null) break;
                clickableBlocks.put(block, new PairTuple<>(pair.firstValue(), null));
            }
        }
    }

    @Override
    public void putBookOnEntity(Entity entity, ItemStack book, Side side) {
        try {
            String stringJsonBook = this.distribution.convertBookToJson(book).toString();
            switch (side) {
                case LEFT ->
                        entity.getPersistentDataContainer().set(PersistentKey.ENTITY_LEFT_BOOK, PersistentDataType.STRING, stringJsonBook);
                case RIGHT ->
                        entity.getPersistentDataContainer().set(PersistentKey.ENTITY_RIGHT_BOOK, PersistentDataType.STRING, stringJsonBook);
            }
        } catch (IllegalAccessException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to put book on entity!", ex);
        }
    }

    @Override
    public void putBookOnBlock(Block block, ItemStack book, Side side) {
        switch (side) {
            case LEFT -> {
                PairTuple<ItemStack, ItemStack> pair = clickableBlocks.remove(block);
                clickableBlocks.put(block, new PairTuple<>(book, pair == null ? null : pair.secondValue()));
            }
            case RIGHT -> {
                PairTuple<ItemStack, ItemStack> pair = clickableBlocks.remove(block);
                clickableBlocks.put(block, new PairTuple<>(pair == null ? null : pair.firstValue(), book));
            }
        }
        this.blocksPairedToChunk.computeIfAbsent(block.getChunk(), k -> new HashSet<>()).add(block);
    }

    public boolean loadDistribution() {
        // Copyright (c) mbax - Thank you for the great 'modular project' tutorial!
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);

        this.plugin.getLogger().info("Your server is running version " + version + "!");
        try {
            final Class<?> clazz = Class.forName("ro.niconeko.astralbooks.dist." + version + ".DistributionHandler");
            if (Distribution.class.isAssignableFrom(clazz)) {
                this.plugin.getLogger().info("Loading support for version " + version + "...");
                this.distribution = (Distribution) clazz.getConstructor(AstralBooksPlugin.class).newInstance(this.plugin);
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

    public boolean isValidPermission(String permission) {
        if (permission == null)
            return false;
        if (permission.isEmpty())
            return false;
        return this.permissionPattern.matcher(permission).matches();
    }

    @Override
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
            this.distribution.sendRightClick(player);
        } catch (Exception ex) {
            return false;
        }
        if (old != null)
            pi.setItem(slot, old.clone());
        else
            pi.setItem(slot, null);
        return true;
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

    @Override
    public boolean setJoinBook(ItemStack book) {
        return this.plugin.getPluginStorage().setJoinBook(book);
    }

    @Override
    public boolean removeJoinBook() {
        return this.plugin.getPluginStorage().removeJoinBook();
    }

    @Override
    public ItemStack getJoinBook() {
        return this.plugin.getPluginStorage().getJoinBook();
    }

    @Override
    public boolean hasJoinBook() {
        return this.plugin.getPluginStorage().hasJoinBook();
    }

    @Override
    public long getJoinBookLastChange() {
        return this.plugin.getPluginStorage().getJoinBookLastChange();
    }

    @Override
    public long getJoinBookLastSeen(Player player) {
        return this.plugin.getPluginStorage().getJoinBookLastSeen(player);
    }

    @Override
    public boolean setJoinBookLastSeen(Player player, long lastSeen) {
        return this.plugin.getPluginStorage().setJoinBookLastSeen(player, lastSeen);
    }

    @Override
    public boolean hasJoinBookLastSeen(Player player) {
        return this.plugin.getPluginStorage().hasJoinBookLastSeen(player);
    }

    // NPCs books
    @Override
    public boolean putNPCBook(int npcId, Side side, ItemStack book) {
        return this.plugin.getPluginStorage().putNPCBook(npcId, side, book);
    }

    @Override
    public boolean removeNPCBook(int npcId, Side side) {
        return this.plugin.getPluginStorage().removeNPCBook(npcId, side);
    }

    @Override
    public ItemStack getNPCBook(int npcId, Side side, ItemStack def) {
        return this.plugin.getPluginStorage().getNPCBook(npcId, side, def);
    }

    @Override
    public ItemStack getNPCBook(int npcId, Side side) {
        return this.plugin.getPluginStorage().getNPCBook(npcId, side);
    }

    @Override
    public boolean hasNPCBook(int npcId, Side side) {
        return this.plugin.getPluginStorage().hasNPCBook(npcId, side);
    }


    public Set<PairTuple<Integer, Side>> getNPCBooks() {
        return this.plugin.getPluginStorage().getNPCBooks();
    }

    // Filters books
    @Override
    public boolean putFilterBook(String filterName, ItemStack book) {
        return this.plugin.getPluginStorage().putFilterBook(filterName, book);
    }

    @Override
    public boolean removeFilterBook(String filterName) {
        return this.plugin.getPluginStorage().removeFilterBook(filterName);
    }

    @Override
    public ItemStack getFilterBook(String filterName, ItemStack def) {
        return this.plugin.getPluginStorage().getFilterBook(filterName, def);
    }

    @Override
    public ItemStack getFilterBook(String filterName) {
        return this.plugin.getPluginStorage().getFilterBook(filterName);
    }

    @Override
    public boolean hasFilterBook(String filterName) {
        return this.plugin.getPluginStorage().hasFilterBook(filterName);
    }

    @Override
    public Set<String> getFilterNames() {
        return this.plugin.getPluginStorage().getFilterNames();
    }

    // Commands filters
    @Override
    public boolean putCommandFilter(String cmd, String filterName, @NotNull String permission) {
        return this.plugin.getPluginStorage().putCommandFilter(cmd, filterName, permission);
    }

    @Override
    public boolean removeCommandFilter(String cmd) {
        return this.plugin.getPluginStorage().removeCommandFilter(cmd);
    }

    @Override
    public PairTuple<String, String> getCommandFilter(String cmd) {
        return this.plugin.getPluginStorage().getCommandFilter(cmd);
    }

    @Override
    public boolean hasCommandFilter(String cmd) {
        return this.plugin.getPluginStorage().hasCommandFilter(cmd);
    }

    @Override
    public Set<String> getCommandFilterNames() {
        return this.plugin.getPluginStorage().getCommandFilterNames();
    }

    public LinkedList<PairTuple<Date, ItemStack>> getAllBookSecurity(UUID uuid, int page, int amount) {
        return this.plugin.getPluginStorage().getAllBookSecurity(uuid, page, amount);
    }

    public LinkedList<TripletTuple<UUID, Date, ItemStack>> getAllBookSecurity(int page, int amount) {
        return this.plugin.getPluginStorage().getAllBookSecurity(page, amount);
    }

    public void putBookSecurity(UUID uuid, Date date, ItemStack book) {
        this.plugin.getPluginStorage().putBookSecurity(uuid, date, book);
    }
}
