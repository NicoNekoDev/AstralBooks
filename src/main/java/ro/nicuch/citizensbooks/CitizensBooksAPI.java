/*

   CitizensBooks
   Copyright (c) 2018 @ Drăghiciu 'nicuch' Nicolae

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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.npc.NPC;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ContextManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CitizensBooksAPI {
    private final CitizensBooksPlugin plugin;
    public static final String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",")
            .split(",")[3];
    private static final Class<?> cisobc = getOBCClass("inventory.CraftItemStack");
    private static final Class<?> pds = getNMSClass("PacketDataSerializer");
    private static final Class<?> pc = getNMSClass("PlayerConnection");
    private static final Class<?> p = getNMSClass("Packet");
    private static final Class<?> ppocp = getNMSClass("PacketPlayOutCustomPayload");
    private static final Class<?> msonp = getNMSClass("MojangsonParser");
    private static final Class<?> nbtTag = getNMSClass("NBTTagCompound");
    private static final Class<?> nmsIs = getNMSClass("ItemStack");
    private static final Class<?> ppoob = getNMSClass("PacketPlayOutOpenBook");
    private static final Class<?> eh = getNMSClass("EnumHand");

    public CitizensBooksAPI(CitizensBooksPlugin plugin) {
        (this.plugin = plugin).getLogger().info("Your server is running version " + version + "!");
        this.checkVersionCompatibility(false);
    }

    private boolean checkVersionCompatibility(boolean throwable) {
        return switch (version) {
            case "v1_8_R1", "v1_8_R2", "v1_8_R3",
                    "v1_9_R1", "v1_9_R2", "v1_10_R1",
                    "v1_11_R1", "v1_12_R1", "v1_13_R1",
                    "v1_13_R2", "v1_14_R1", "v1_14_R2",
                    "v1_14_R3", "v1_15_R1", "v1_16_R1",
                    "v1_16_R2", "v1_16_R3", "v1_16_R4",
                    "v1_17_R1" -> true;
            default -> {
                if (throwable) {
                    this.plugin.getLogger().warning("Well, this version of CitizensBooks is incompatible with your server version " + version + "... ");
                    if (UpdateChecker.updateAvailable())
                        this.plugin.getLogger().info("Oh look! An update is available! Go to Spigot page and download it! It might fix the error!");
                    else
                        this.plugin.getLogger().warning("Please don't report this error! We try hard to update it as fast as possible!");
                } else {
                    this.plugin.getLogger().warning("Your server version is incompatbile, but this plugin still tries to work.");
                    if (UpdateChecker.updateAvailable())
                        this.plugin.getLogger().info("Oh look! An update is available! Go to Spigot page and download it!!");
                }
                yield false;
            }
        };
    }

    /**
     * Get the book from filter
     *
     * @param filterName filter name/id
     * @return the book
     */
    public ItemStack getFilter(String filterName) {
        Validate.notNull(filterName, "The filter name is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Validate.notEmpty(filterName, "The filter name is empty! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        return this.plugin.getSettings().getItemStack("filters." + filterName, new ItemStack(Material.WRITTEN_BOOK));
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
        return this.plugin.getSettings().isItemStack("filters." + filterName);
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
        this.plugin.getSettings().set("filters." + filterName, book);
        this.plugin.saveSettings();
    }

    public Set<String> getFilters() {
        return this.plugin.getSettings().getConfigurationSection("filters").getKeys(false);
    }

    /**
     * Remove the filter
     *
     * @param filterName filter name/id
     */
    public void removeFilter(String filterName) {
        this.plugin.getSettings().set("filters." + filterName, null);
        this.plugin.saveSettings();
    }

    private static Class<?> getNMSClass(String nmsClassString) {
        try {
            return Class.forName("net.minecraft.server." + version + "." + nmsClassString);
        } catch (ClassNotFoundException ignore) {
        }
        return null;
    }

    @SuppressWarnings("SameParameterValue")
    private static Class<?> getOBCClass(String obcClassString) {
        /*IntelliJ thinks that this method is useless as if is only used once but
         *if I try to remove the try-catch block, is gonna give me errors, not warnings.*/
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + obcClassString);
        } catch (ClassNotFoundException ignore) {
        }
        return null;
    }

    private Object getConnection(Player player) throws SecurityException, NoSuchMethodException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
        return nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
    }

    protected void rightClick(Player player) {
        try {
            switch (version) {
                case "v1_8_R1", "v1_8_R2", "v1_8_R3",
                        "v1_9_R1", "v1_9_R2", "v1_10_R1",
                        "v1_11_R1", "v1_12_R1" -> {
                    if (ppocp == null)
                        throw new NullPointerException("PPOCP not found");
                    if (pds == null)
                        throw new NullPointerException("PDS not found!");
                    if (pc == null)
                        throw new NullPointerException("PC not found!");
                    pc.getMethod("sendPacket", p).invoke(this.getConnection(player),
                            ppocp.getConstructor(String.class, pds).newInstance("MC|BOpen", pds.getConstructor(ByteBuf.class)
                                    .newInstance(Unpooled.buffer(256).setByte(0, (byte) 0).writerIndex(1))));
                }
                case "v1_13_R1", "v1_13_R2" -> {
                    Class<?> mk = getNMSClass("MinecraftKey");
                    //Used for 1.13 and above
                    if (mk == null)
                        throw new Exception("Plugin outdated!");
                    if (ppocp == null)
                        throw new NullPointerException("PPOCP not found");
                    if (pds == null)
                        throw new NullPointerException("PDS not found!");
                    if (pc == null)
                        throw new NullPointerException("PC not found!");
                    pc.getMethod("sendPacket", p).invoke(this.getConnection(player),
                            ppocp.getConstructor(mk, pds)
                                    .newInstance(mk.getMethod("a", String.class).invoke(mk, "minecraft:book_open"), pds.getConstructor(ByteBuf.class)
                                            .newInstance(Unpooled.buffer(256).setByte(0, (byte) 0).writerIndex(1))));
                }
                case "v1_17_R1" -> {
                    // none
                }
                default -> {
                    if (ppoob == null)
                        throw new Exception("Plugin outdated!");
                    if (eh == null)
                        throw new Exception("Plugin outdated!");
                    if (pc == null)
                        throw new NullPointerException("PC not found!");
                    pc.getMethod("sendPacket", p).invoke(this.getConnection(player),
                            ppoob.getConstructor(eh).newInstance(
                                    eh.getDeclaredMethod("valueOf", String.class)
                                            .invoke(eh, "MAIN_HAND")));
                }
            }
        } catch (Exception ex) {
            if (this.checkVersionCompatibility(true))
                this.plugin.printError(ex);
        }
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
        //testing
        Validate.notNull(book, "The ItemStack is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Validate.isTrue(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        if ("v1_17_R1".equals(version)) {
            try {
                player.getClass().getMethod("openBook", ItemStack.class).invoke(player, book);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else {
            int slot = player.getInventory().getHeldItemSlot();
            ItemStack old = player.getInventory().getItem(slot);
            PlayerInventory pi = player.getInventory();
            pi.setItem(slot, book);
            this.rightClick(player);
            pi.setItem(slot, old);
        }

    }

    protected ItemStack placeholderHook(Player player, ItemStack book, NPC npc) {
        Validate.notNull(book, "The ItemStack is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Validate.isTrue(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        if (!this.plugin.isPlaceHolderEnabled())
            return book;
        if (book.hasItemMeta()) {
            BookMeta bookMeta = (BookMeta) book.getItemMeta();
            if (bookMeta.hasTitle()) {
                if (npc == null) {
                    bookMeta.setTitle(PlaceholderAPI.setPlaceholders(player, bookMeta.getTitle()));
                } else {
                    Location loc = npc.getStoredLocation();
                    bookMeta.setTitle(PlaceholderAPI.setPlaceholders(player, bookMeta.getTitle())
                            .replace("%npc_name%", npc.getName())
                            .replace("%npc_id%", npc.getId() + "")
                            .replace("%npc_loc_x%", loc.getX() + "")
                            .replace("%npc_loc_y%", loc.getY() + "")
                            .replace("%npc_loc_z%", loc.getZ() + "")
                            .replace("%npc_loc_world%", loc.getWorld().getName()));
                }
            }
            if (bookMeta.hasAuthor()) {
                if (npc == null) {
                    bookMeta.setAuthor(PlaceholderAPI.setPlaceholders(player, bookMeta.getAuthor()));
                } else {
                    Location loc = npc.getStoredLocation();
                    bookMeta.setAuthor(PlaceholderAPI.setPlaceholders(player, bookMeta.getAuthor())
                            .replace("%npc_name%", npc.getName())
                            .replace("%npc_id%", npc.getId() + "")
                            .replace("%npc_loc_x%", loc.getX() + "")
                            .replace("%npc_loc_y%", loc.getY() + "")
                            .replace("%npc_loc_z%", loc.getZ() + "")
                            .replace("%npc_loc_world%", loc.getWorld().getName()));
                }
            }
            if (bookMeta.hasPages()) {
                if (npc == null) {
                    bookMeta.setPages(PlaceholderAPI.setPlaceholders(player, bookMeta.getPages()));
                } else {
                    Location loc = npc.getStoredLocation();
                    bookMeta.setPages(PlaceholderAPI.setPlaceholders(player, bookMeta.getPages()).stream().map(str -> str
                            .replace("%npc_name%", npc.getName())
                            .replace("%npc_id%", npc.getId() + "")
                            .replace("%npc_loc_x%", loc.getX() + "")
                            .replace("%npc_loc_y%", loc.getY() + "")
                            .replace("%npc_loc_z%", loc.getZ() + "")
                            .replace("%npc_loc_world%", loc.getWorld().getName())).collect(Collectors.toList()));
                }
            }
            book.setItemMeta(bookMeta);
        }
        return book;
    }

    protected boolean hasPermission(CommandSender sender, String permission) {
        try {
            if (sender instanceof Player player) {
                if (player.isOp()) return true;
                Optional<LuckPerms> luckPerms = this.plugin.isLuckPermsEnabled() ? Optional.of(this.plugin.getLuckPermissions()) : Optional.empty(); //If LuckPerms not enabled, this will return empty
                Optional<Permission> vaultPerms = this.plugin.isVaultEnabled() ? Optional.of(this.plugin.getVaultPermissions()) : Optional.empty(); //If vault not enabled or luckperms is used, this will return empty

                return (luckPerms.isPresent() && this.hasLuckPermission(luckPerms.get().getUserManager().getUser(sender.getName()), permission)) ||
                        (vaultPerms.isPresent() && vaultPerms.get().has(sender, permission)) || sender.hasPermission(permission);
            }
            return true;
        } catch (NullPointerException ex) {
            return false;
        }
    }

    protected boolean hasLuckPermission(User user, String permission) {
        if (user == null)
            throw new NullPointerException();
        ContextManager contextManager = this.plugin.getLuckPermissions().getContextManager();
        return user.getCachedData().getPermissionData(QueryOptions.contextual(contextManager.getContext(user).orElseGet(contextManager::getStaticContext))).checkPermission(permission).asBoolean();
    }

    @SuppressWarnings("deprecation")
    protected Optional<Player> getPlayer(String name) {
        Player player = Bukkit.getPlayer(name);
        if (player == null)
            return Optional.empty();
        return Optional.of(player);
    }

    protected List<String> getPlayers() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }
}
