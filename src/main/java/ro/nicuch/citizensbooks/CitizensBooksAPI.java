package ro.nicuch.citizensbooks;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.clip.placeholderapi.PlaceholderAPI;

public class CitizensBooksAPI {
	private final CitizensBooks plugin;
	private final String version;

	public CitizensBooksAPI(CitizensBooks plugin) {
		this.plugin = plugin;
		this.version = plugin.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		this.plugin.getLogger().info(ChatColor.GREEN + "Your server is running version " + this.version + "!");
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

	private Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server." + this.version + "." + nmsClassString);
	}

	private Class<?> getOBCClass(String cbClassString) throws ClassNotFoundException {
		return Class.forName("org.bukkit.craftbukkit." + this.version + "." + cbClassString);
	}

	private Object getConnection(Player player) throws SecurityException, NoSuchMethodException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
		return nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);
	}

	protected void rightClick(Player player) {
		try {
			Class<?> pds = this.getNMSClass("PacketDataSerializer");
			this.getNMSClass("PlayerConnection").getMethod("sendPacket", this.getNMSClass("Packet")).invoke(
					this.getConnection(player),
					this.getNMSClass("PacketPlayOutCustomPayload").getConstructor(String.class, pds)
							.newInstance("MC|BOpen", pds.getConstructor(ByteBuf.class)
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
		if (!(item.getItemMeta() instanceof BookMeta))
			return item;
		BookMeta meta = (BookMeta) item.getItemMeta();
		meta.setPages(PlaceholderAPI.setPlaceholders(player, meta.getPages()));
		item.setItemMeta(meta);
		return item;
	}

	protected String bookToString(ItemStack book) {
		try {
			Class<?> cisobc = this.getOBCClass("inventory.CraftItemStack");
			Object nms = cisobc.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(cisobc, book);
			Object tag = nms.getClass().getMethod("getTag", this.getNMSClass("NBTTagCompound")).invoke(nms);
			return (String) tag.getClass().getMethod("toString", String.class).invoke(tag);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	@SuppressWarnings("deprecation")
	protected ItemStack stringToBook(String arg) {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		return Bukkit.getUnsafe().modifyItemStack(book, arg);
	}
}
