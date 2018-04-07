package ro.nicuch.citizensbooks;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.permission.Permission;

public class CitizensBooks extends JavaPlugin {
	private Permission PERMISSION;
	private boolean placeholder;
	private CitizensBooksAPI api;

	@Override
	public void onEnable() {
		this.reloadSettings();
		PluginManager manager = this.getServer().getPluginManager();
		if (!manager.isPluginEnabled("Vault")) {
			this.getLogger().warning(ChatColor.RED + "Vault not enabled, plugin disabled!");
			this.setEnabled(false);
			return;
		}
		this.api = new CitizensBooksAPI(this);
		if (!manager.isPluginEnabled("PlaceholderAPI")) {
			this.getLogger().info(ChatColor.BLUE + "PlaceholderAPI not found!");
		} else {
			this.getLogger().info(ChatColor.GREEN + "PlaceholderAPI found, try hooking!");
			this.placeholder = true;
		}
		this.PERMISSION = this.getServer().getServicesManager().getRegistration(Permission.class).getProvider();
		TabExecutor te = null;
		manager.registerEvents(new PlayerActions(this), this);
		if (!manager.isPluginEnabled("Citizens")) {
			this.getLogger().info(ChatColor.BLUE + "Citizens not found!");
			te = new PlayerCommands(this);
		} else {
			this.getLogger().info(ChatColor.GREEN + "Citizens found, try hooking!");
			manager.registerEvents(new CitizensActions(this), this);
			te = new CitizensCommands(this);
		}
		this.getCommand("npcbook").setExecutor(te);
		this.getCommand("npcbook").setTabCompleter(te);
	}

	public CitizensBooksAPI getAPI() {
		return this.api;
	}

	@Override
	public void onDisable() {
		this.saveSettings();
	}

	public void reloadSettings() {
		File config = new File(this.getDataFolder() + File.separator + "config.yml");
		if (!config.exists()) {
			this.saveResource("config.yml", false);
		}
		if (this.getConfig().isInt("version") && this.getConfig().getInt("version") != 5) {
			File copy = new File(
					this.getDataFolder() + File.separator + "config_" + System.currentTimeMillis() + ".yml");
			config.renameTo(copy);
			this.saveResource("config.yml", true);
		}
		this.reloadConfig();
	}

	public void saveSettings() {
		this.saveConfig();
	}

	public Permission getPermission() {
		return this.PERMISSION;
	}

	public boolean isPlaceHolderEnabled() {
		return this.placeholder;
	}

	public String getMessage(String path, String def) {
		return ChatColor.translateAlternateColorCodes('&',
				this.getConfig().getString("lang.header", LangDefaults.header))
				+ ChatColor.translateAlternateColorCodes('&', this.getConfig().getString(path, def));
	}

	public String getMessageNoHeader(String path, String def) {
		return ChatColor.translateAlternateColorCodes('&', this.getConfig().getString(path, def));
	}
}
