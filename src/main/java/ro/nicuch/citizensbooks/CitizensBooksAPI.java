package ro.nicuch.citizensbooks;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.clip.placeholderapi.PlaceholderAPI;

public class CitizensBooksAPI {
	private final CitizensBooks plugin;

	public CitizensBooksAPI(CitizensBooks plugin) {
		this.plugin = plugin;
		String version = null;
		try {
			version = plugin.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		} catch (ArrayIndexOutOfBoundsException e) {
			this.plugin.getLogger().warning(ChatColor.RED + "Your server is running an unknown version!");
			return;
		}
		this.plugin.getLogger().info(ChatColor.GREEN + "Your server is running version " + version + "!");
	}

	public ItemStack getFilter(String filterName) {
		return this.stringToBook(this.plugin.getConfig().getString("filters." + filterName, ""));
	}

	public boolean hasFilter(String filterName) {
		return this.plugin.getConfig().isString("filters." + filterName);
	}

	public void createFilter(String filterName, ItemStack book) {
		this.plugin.getConfig().set("filters." + filterName, this.bookToString(book));
		this.plugin.saveSettings();
	}

	public void removeFilter(String filterName) {
		this.plugin.getConfig().set("filters." + filterName, null);
		this.plugin.saveSettings();
	}

	// Author: Skionz
	private Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
		String version = plugin.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
		String name = "net.minecraft.server." + version + nmsClassString;
		Class<?> nmsClass = Class.forName(name);
		return nmsClass;
	}

	// Author: Skionz
	private Object getConnection(Player player) throws SecurityException, NoSuchMethodException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Method getHandle = player.getClass().getMethod("getHandle");
		Object nmsPlayer = getHandle.invoke(player);
		Field conField = nmsPlayer.getClass().getField("playerConnection");
		Object con = conField.get(nmsPlayer);
		return con;
	}

	protected void rightClick(Player player) {
		try {
			Class<?> pds = this.getNMSClass("PacketDataSerializer");
			Constructor<?> pdsc = pds.getConstructor(ByteBuf.class);
			Class<?> ppocp = this.getNMSClass("PacketPlayOutCustomPayload");
			Constructor<?> ppocpc = ppocp.getConstructor(String.class, pds);
			ByteBuf buf = Unpooled.buffer(256);
			buf.setByte(0, (byte) 0);
			buf.writerIndex(1);
			Object packet = ppocpc.newInstance("MC|BOpen", pdsc.newInstance(buf));
			Method sendPacket = getNMSClass("PlayerConnection").getMethod("sendPacket", this.getNMSClass("Packet"));
			sendPacket.invoke(this.getConnection(player), packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openBook(Player player, ItemStack book) {
		int slot = player.getInventory().getHeldItemSlot();
		ItemStack old = player.getInventory().getItem(slot);
		PlayerInventory pi = player.getInventory();
		pi.setItem(slot, book);
		this.rightClick(player);
		pi.setItem(slot, old);
	}

	protected ItemStack placeholderHook(Player player, ItemStack item) {
		if (!this.plugin.isPlaceHolderEnabled())
			return item;
		if (!(item.getItemMeta() instanceof BookMeta))
			return item;
		BookMeta meta = (BookMeta) item.getItemMeta();
		meta.setPages(PlaceholderAPI.setPlaceholders(player, meta.getPages()));
		item.setItemMeta(meta);
		return item;
	}

	protected String bookToString(ItemStack book) {
		net.minecraft.server.v1_12_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(book);
		return nmsStack.getTag().toString();
	}

	@SuppressWarnings("deprecation")
	protected ItemStack stringToBook(String arg) {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		return Bukkit.getUnsafe().modifyItemStack(book, arg);
	}
}
