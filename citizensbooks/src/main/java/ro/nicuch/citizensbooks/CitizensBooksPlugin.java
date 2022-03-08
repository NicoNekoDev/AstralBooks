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

import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import me.lucko.commodore.file.CommodoreFileFormat;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ro.nicuch.citizensbooks.listeners.AuthmeActions;
import ro.nicuch.citizensbooks.listeners.CitizensActions;
import ro.nicuch.citizensbooks.listeners.PlayerActions;
import ro.nicuch.citizensbooks.utils.Message;
import ro.nicuch.citizensbooks.utils.PersistentKey;
import ro.nicuch.citizensbooks.utils.UpdateChecker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;

public class CitizensBooksPlugin extends JavaPlugin {
    private Permission vaultPerms;
    private LuckPerms luckPerms;
    private final CitizensBooksDatabase database = new CitizensBooksDatabase(this);
    private final CitizensBooksAPI api = new CitizensBooksAPI(this);
    private YamlConfiguration settings;
    private boolean usePlaceholderAPI, useAuthMe, useCitizens, useLuckPerms, useVault, useNBTAPI, useDatabase;
    public final int configVersion = 9;
    private PlayerActions playerActionsListener;

    @Override
    public void onEnable() {
        try {
            this.getLogger().info("============== BEGIN LOAD ==============");
            this.reloadSettings();
            if (!this.api.loadDistribution()) {
                this.getLogger().info("Failed to load distribution... disabling the plugin!");
                this.setEnabled(false);
                this.getLogger().info("============== END LOAD ==============");
                return;
            }
            if (!PersistentKey.init(this)) {
                this.getLogger().info("Failed to load PersistentKey!");
                this.setEnabled(false);
                this.getLogger().info("============== END LOAD ==============");
                return;
            }
            //bStats Metrics, by default enabled
            new Metrics(this, 2454);
            PluginManager manager = this.getServer().getPluginManager();
            if (!manager.isPluginEnabled("LuckPerms")) {
                this.getLogger().info("LuckPerms not found!");
                if (!manager.isPluginEnabled("Vault"))
                    this.getLogger().info("Vault not found!");
                else {
                    this.getLogger().info("Vault found, try hooking!");
                    RegisteredServiceProvider<Permission> provider = this.getServer().getServicesManager().getRegistration(Permission.class);
                    if (provider != null) {
                        this.useVault = true;
                        this.vaultPerms = provider.getProvider();
                    } else
                        this.getLogger().info("Failed to hook into Vault!");
                }
            } else {
                this.getLogger().info("LuckPerms found, try hooking!");
                Plugin plugin = manager.getPlugin("LuckPerms");
                if (plugin != null) {
                    if (plugin.getDescription().getVersion().startsWith("5")) {
                        RegisteredServiceProvider<LuckPerms> provider = this.getServer().getServicesManager().getRegistration(LuckPerms.class);
                        if (provider != null) {
                            this.useLuckPerms = true;
                            this.luckPerms = provider.getProvider();
                            if (manager.isPluginEnabled("Vault"))
                                this.getLogger().info("Vault plugin found, but we'll use LuckPerms!");
                        } else
                            this.getLogger().info("Failed to hook into LuckPerms!");
                    } else {
                        this.getLogger().info("Your LuckPerms version is oudated! :(");
                        if (manager.isPluginEnabled("Vault")) {
                            RegisteredServiceProvider<Permission> provider = this.getServer().getServicesManager().getRegistration(Permission.class);
                            if (provider != null) {
                                this.getLogger().info("Vault found instead! Try hooking!");
                                this.useVault = true;
                                this.vaultPerms = provider.getProvider();
                            } else // do we need it?
                                this.getLogger().info("Failed to hook into Vault!");
                        }
                    }
                } else
                    this.getLogger().info("Failed to hook into LuckPerms!");
            }
            if (!manager.isPluginEnabled("PlaceholderAPI"))
                this.getLogger().info("PlaceholderAPI not found!");
            else {
                this.getLogger().info("PlaceholderAPI found, try hooking!");
                this.usePlaceholderAPI = true;
            }
            manager.registerEvents((this.playerActionsListener = new PlayerActions(this)), this);
            if (!manager.isPluginEnabled("Citizens"))
                this.getLogger().info("Citizens not found!");
            else {
                this.getLogger().info("Citizens found, try hooking!");
                manager.registerEvents(new CitizensActions(this), this);
                this.useCitizens = true;
            }
            if (!manager.isPluginEnabled("Authme"))
                this.getLogger().info("Authme not found!");
            else {
                this.getLogger().info("Authme found, try hooking!");
                manager.registerEvents(new AuthmeActions(this), this);
                this.useAuthMe = true;
            }
            if (!manager.isPluginEnabled("NBTAPI"))
                if (this.api.noNBTAPIRequired())
                    this.getLogger().info("NBTAPI not found, but support for it it's not required!");
                else
                    this.getLogger().info("NBTAPI not found!");
            else {
                if (this.api.noNBTAPIRequired()) {
                    this.getLogger().info("NBTAPI found, but support for it it's not required!");
                } else {
                    this.getLogger().info("NBTAPI found, try hooking!");
                    this.useNBTAPI = true;
                }
            }

            // load database, filters and npc books after dependencies
            if (!this.setDatabaseEnabled(this.database.enableDatabase(this.getLogger())))
                this.api.reloadFilters(this.getLogger());
            this.api.reloadNPCBooks(this.getLogger());

            PluginCommand npcBookCommand = this.getCommand("npcbook");
            if (npcBookCommand != null)
                npcBookCommand.setExecutor(new CitizensBooksCommand(this));
            PluginCommand encryptBookCommand = this.getCommand("encryptbook");
            if (encryptBookCommand != null)
                encryptBookCommand.setExecutor(new CipherBookCommand(this, true));
            PluginCommand decryptBookCommand = this.getCommand("decryptbook");
            if (decryptBookCommand != null)
                decryptBookCommand.setExecutor(new CipherBookCommand(this, false));
            if (CommodoreProvider.isSupported()) {
                this.getLogger().info("Loading Brigardier support...");
                Commodore commodore = CommodoreProvider.getCommodore(this);
                this.getLogger().info("  Command /npcbook: " + (this.registerCompletions(commodore, npcBookCommand, "command.commodore") ? "supported" : "unsupported"));
                this.getLogger().info("  Command /encryptbook: " + (this.registerCompletions(commodore, encryptBookCommand, "encrypt.commodore") ? "supported" : "unsupported"));
                this.getLogger().info("  Command /decryptbook: " + (this.registerCompletions(commodore, decryptBookCommand, "decrypt.commodore") ? "supported" : "unsupported"));
            } else
                this.getLogger().info("Brigardier is not supported on this version!");
            //Update checker, by default enabled
            if (this.settings.getBoolean("update_check", true))
                manager.registerEvents(new UpdateChecker(this), this);
            this.getLogger().info("============== END LOAD ==============");
        } catch (Exception ex) {
            this.getLogger().log(Level.WARNING, "Error detected, disabling the plugin!", ex);
            this.getLogger().info("============== END LOAD ==============");
            this.setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        if (this.playerActionsListener != null)
            this.playerActionsListener.onDisable();
        if (this.isDatabaseEnabled())
            this.database.disableDatabase(this.getLogger());
    }

    public CitizensBooksDatabase getDatabase() {
        return this.database;
    }

    public CitizensBooksAPI getAPI() {
        return this.api;
    }

    public YamlConfiguration getSettings() {
        return this.settings;
    }

    private boolean registerCompletions(Commodore commodore, PluginCommand command, String resource) {
        if (command == null)
            return false;
        try (InputStream is = this.getResource(resource)) {
            if (is == null)
                throw new FileNotFoundException();
            commodore.register(command, CommodoreFileFormat.parse(is), player -> this.api.hasPermission(player, "npcbook.tab.completer"));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public void reloadSettings() {
        try {
            File config = new File(this.getDataFolder() + File.separator + "config.yml");
            if (!config.exists()) {
                this.saveResource("config.yml", false);
                this.getLogger().info("A new config.yml was created!");
            }
            this.settings = YamlConfiguration.loadConfiguration(config);
            //Load config.yml first
            if (this.settings.isInt("version") && this.settings.getInt("version") != this.configVersion) {
                boolean renamed = config.renameTo(new File(
                        this.getDataFolder() + File.separator + "config_" + System.currentTimeMillis() + ".yml"));
                if (renamed) {
                    this.getLogger().info("A new config.yml was generated!");
                    this.saveResource("config.yml", true);
                    //Load again the config
                    this.settings = YamlConfiguration.loadConfiguration(config);
                } else
                    this.getLogger().info("Failed to generate a new config!");
            }
            if (this.playerActionsListener != null)
                this.playerActionsListener.onReload();
        } catch (Exception ex) {
            this.getLogger().log(Level.WARNING, "Couldn't reload config", ex);
        }
    }

    public void saveSettings() {
        try {
            this.settings.save(new File(this.getDataFolder() + File.separator + "config.yml"));
        } catch (Exception ex) {
            this.getLogger().log(Level.WARNING, "Couldn't save config", ex);
        }
    }

    public LuckPerms getLuckPermissions() {
        return this.luckPerms;
    }

    public boolean isLuckPermsEnabled() {
        return this.useLuckPerms;
    }

    public Permission getVaultPermissions() {
        return this.vaultPerms;
    }

    public boolean isVaultEnabled() {
        return this.useVault;
    }

    public boolean isPlaceHolderEnabled() {
        return this.usePlaceholderAPI;
    }

    public boolean isAuthmeEnabled() {
        return this.useAuthMe;
    }

    public boolean isCitizensEnabled() {
        return this.useCitizens;
    }

    public boolean isNBTAPIEnabled() {
        return this.useNBTAPI;
    }

    public boolean isDatabaseEnabled() {
        return this.useDatabase;
    }

    public boolean setDatabaseEnabled(boolean enabled) {
        return (this.useDatabase = enabled);
    }

    public String getMessage(Message msg) {
        return ChatColor.translateAlternateColorCodes('&',
                this.settings.getString(Message.HEADER.getPath(), Message.HEADER.getDefault()))
                + ChatColor.translateAlternateColorCodes('&', this.settings.getString(msg.getPath(), msg.getDefault()));
    }

    public String getMessageNoHeader(Message msg) {
        return ChatColor.translateAlternateColorCodes('&', this.settings.getString(msg.getPath(), msg.getPath()));
    }
}
