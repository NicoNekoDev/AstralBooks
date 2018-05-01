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

import java.lang.reflect.InvocationTargetException;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.clip.placeholderapi.PlaceholderAPI;

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

    public CitizensBooksAPI(CitizensBooksPlugin plugin) {
        this.plugin = plugin;
        this.plugin.getLogger().info(ChatColor.GREEN + "Your server is running version " + version + "!");
    }

    /**
     * Get the book from filter
     *
     * @param filterName filter name/id
     * @return the book
     */
    public ItemStack getFilter(String filterName) {
        return this.stringToBook(this.plugin.getConfig().getString("filters." + filterName, ""));
    }

    /**
     * Check if filter has the book
     *
     * @param filterName filter name/id
     * @return if the filter has the book
     */
    public boolean hasFilter(String filterName) {
        return this.plugin.getConfig().isString("filters." + filterName);
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
        if (book == null)
            throw new NullPointerException("ItemStack can\'t be null!");
        if (book.getType() != Material.WRITTEN_BOOK)
            throw new IllegalArgumentException("The filter can only be a written book!");
        this.plugin.getConfig().set("filters." + filterName, this.bookToString(book));
        this.plugin.saveSettings();
    }

    /**
     * Remove the filter
     *
     * @param filterName filter name/id
     */
    public void removeFilter(String filterName) {
        this.plugin.getConfig().set("filters." + filterName, null);
        this.plugin.saveSettings();
    }

    private static Class<?> getNMSClass(String nmsClassString) {
        try {
            return Class.forName("net.minecraft.server." + version + "." + nmsClassString);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Class<?> getOBCClass(String obcClassString) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + obcClassString);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
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
            if (pc == null || ppocp == null || pds == null)
                throw new NullPointerException("Craftbukkit classes not found!");
            pc.getMethod("sendPacket", p).invoke(this.getConnection(player),
                    ppocp.getConstructor(String.class, pds).newInstance("MC|BOpen", pds.getConstructor(ByteBuf.class)
                            .newInstance(Unpooled.buffer(256).setByte(0, (byte) 0).writerIndex(1))));
        } catch (Exception ex) {
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
        if (book == null)
            throw new NullPointerException("ItemStack can\'t be null!");
        if (book.getType() != Material.WRITTEN_BOOK)
            throw new IllegalArgumentException("The filter can only be a written book!");
        int slot = player.getInventory().getHeldItemSlot();
        ItemStack old = player.getInventory().getItem(slot);
        PlayerInventory pi = player.getInventory();
        pi.setItem(slot, book);
        this.rightClick(player);
        pi.setItem(slot, old);
    }

    protected ItemStack placeholderHook(Player player, ItemStack book, NPC npc) {
        if (!this.plugin.isPlaceHolderEnabled())
            return book;
        if (npc == null)
            return this.stringToBook(PlaceholderAPI.setPlaceholders(player, this.bookToString(book)));
        Location loc = npc.getStoredLocation();
        return this.stringToBook(PlaceholderAPI.setPlaceholders(player, this.bookToString(book))
                .replace("%npc_name%", npc.getName())
                .replace("%npc_id%", npc.getId() + "")
                .replace("%npc_loc_x%", loc.getX() + "")
                .replace("%npc_loc_y%", loc.getY() + "")
                .replace("%npc_loc_z%", loc.getZ() + "")
                .replace("%npc_loc_world%", loc.getWorld().getName()));
    }

    protected String bookToString(ItemStack book) {
        try {
            if (cisobc == null)
                throw new NullPointerException("Craftbukkit classes not found!");
            Object nms = cisobc.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(cisobc, book);
            Object nbtTag = nms.getClass().getMethod("getTag").invoke(nms);
            return (String) nbtTag.getClass().getMethod("toString").invoke(nbtTag);
        } catch (Exception ex) {
            this.plugin.printError(ex);
        }
        return "";
    }

    protected ItemStack stringToBook(String nbt) {
        ItemStack def = new ItemStack(Material.WRITTEN_BOOK);
        try {
            if (msonp == null || cisobc == null)
                throw new NullPointerException("Craftbukkit classes not found!");
            Object tag = msonp.getDeclaredMethod("parse", String.class).invoke(msonp, nbt);
            Object nms = cisobc.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(cisobc, def);
            nms.getClass().getMethod("setTag", nbtTag).invoke(nms, tag);
            return (ItemStack) cisobc.getDeclaredMethod("asBukkitCopy", nmsIs).invoke(cisobc, nms);
        } catch (Exception ex) {
            this.plugin.printError(ex);
        }
        return def;
    }
}
