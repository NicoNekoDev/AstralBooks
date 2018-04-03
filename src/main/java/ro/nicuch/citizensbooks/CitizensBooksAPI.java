package ro.nicuch.citizensbooks;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.clip.placeholderapi.PlaceholderAPI;

public class CitizensBooksAPI {
	private final CitizensBooks plugin;
	private static final String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",")
			.split(",")[3];
	private static final Class<?> cisobc = getOBCClass("inventory.CraftItemStack");
	private static final Class<?> pds = getNMSClass("PacketDataSerializer");
	private static final Class<?> pc = getNMSClass("PlayerConnection");
	private static final Class<?> p = getNMSClass("Packet");
	private static final Class<?> ppocp = getNMSClass("PacketPlayOutCustomPayload");
	private static final Class<?> msonp = getNMSClass("MojangsonParser");
	private static final Class<?> nbtTag = getNMSClass("NBTTagCompound");
	private static final Class<?> nmsIs = getNMSClass("ItemStack");

	public CitizensBooksAPI(CitizensBooks plugin) {
		this.plugin = plugin;
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

	private static Class<?> getNMSClass(String nmsClassString) {
		try {
			return Class.forName("net.minecraft.server." + version + "." + nmsClassString);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Class<?> getOBCClass(String cbClassString) {
		try {
			return Class.forName("org.bukkit.craftbukkit." + version + "." + cbClassString);
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
			pc.getMethod("sendPacket", p).invoke(this.getConnection(player),
					ppocp.getConstructor(String.class, pds).newInstance("MC|BOpen", pds.getConstructor(ByteBuf.class)
							.newInstance(Unpooled.buffer(256).setByte(0, (byte) 0).writerIndex(1))));
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
		return this.stringToBook(PlaceholderAPI.setPlaceholders(player, this.bookToString(item)));
	}

	protected String bookToString(ItemStack book) {
		try {
			Object nms = cisobc.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(cisobc, book);
			Object nbtTag = nms.getClass().getMethod("getTag").invoke(nms);
			return (String) nbtTag.getClass().getMethod("toString").invoke(nbtTag);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	protected ItemStack stringToBook(String nbt) {
		ItemStack def = new ItemStack(Material.WRITTEN_BOOK);
		try {
			Object tag = msonp.getDeclaredMethod("parse", String.class).invoke(msonp, nbt);
			Object nms = cisobc.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(cisobc, def);
			nms.getClass().getMethod("setTag", nbtTag).invoke(nms, tag);
			return (ItemStack) cisobc.getDeclaredMethod("asBukkitCopy", nmsIs).invoke(cisobc, nms);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return def;
	}
}
