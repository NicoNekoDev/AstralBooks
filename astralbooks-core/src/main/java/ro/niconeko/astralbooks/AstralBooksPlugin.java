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

import lombok.Getter;
import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import me.lucko.commodore.file.CommodoreFileReader;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ro.niconeko.astralbooks.api.AstralBooks;
import ro.niconeko.astralbooks.listeners.AuthmeActions;
import ro.niconeko.astralbooks.listeners.CitizensActions;
import ro.niconeko.astralbooks.listeners.PlayerActions;
import ro.niconeko.astralbooks.listeners.ServerActions;
import ro.niconeko.astralbooks.settings.PluginSettings;
import ro.niconeko.astralbooks.storage.PluginStorage;
import ro.niconeko.astralbooks.utils.PersistentKey;
import ro.niconeko.astralbooks.utils.UpdateChecker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;

public class AstralBooksPlugin extends JavaPlugin implements AstralBooks {
    private final File settingsFile = new File(getDataFolder(), "settings.yml");
    @Getter private Permission vaultPerms;
    @Getter private LuckPerms luckPerms;
    @Getter private final AstralBooksCore API = new AstralBooksCore(this);
    @Getter private final PluginSettings settings = new PluginSettings(this);
    @Getter private PluginStorage pluginStorage;
    @Getter private boolean PlaceholderAPIEnabled, AuthMeEnabled, CitizensEnabled, LuckPermsEnabled, VaultEnabled;
    @Getter private PlayerActions playerActionsListener;
    @Getter private ServerActions serverActionsListener;

    @Override
    public void onEnable() {
        try {
            this.getLogger().info("============== BEGIN LOAD ==============");

            if (!this.API.loadDistribution()) {
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

            PluginManager manager = this.getServer().getPluginManager();
            if (!manager.isPluginEnabled("LuckPerms")) {
                this.getLogger().info("LuckPerms not found!");
                if (!manager.isPluginEnabled("Vault"))
                    this.getLogger().info("Vault not found!");
                else {
                    this.getLogger().info("Vault found, try hooking...");
                    RegisteredServiceProvider<Permission> provider = this.getServer().getServicesManager().getRegistration(Permission.class);
                    if (provider != null) {
                        this.VaultEnabled = true;
                        this.vaultPerms = provider.getProvider();
                        this.getLogger().info("Successfully hooked into Vault!");
                    } else
                        this.getLogger().info("Failed to hook into Vault!");
                }
            } else {
                this.getLogger().info("LuckPerms found, try hooking...");
                Plugin plugin = manager.getPlugin("LuckPerms");
                if (plugin != null) {
                    if (plugin.getDescription().getVersion().startsWith("5")) {
                        RegisteredServiceProvider<LuckPerms> provider = this.getServer().getServicesManager().getRegistration(LuckPerms.class);
                        if (provider != null) {
                            this.LuckPermsEnabled = true;
                            this.luckPerms = provider.getProvider();
                            if (manager.isPluginEnabled("Vault"))
                                this.getLogger().info("Vault plugin found, but we'll use LuckPerms!");
                            this.getLogger().info("Successfully hooked into LuckPerms!");
                        } else
                            this.getLogger().info("Failed to hook into LuckPerms!");
                    } else {
                        this.getLogger().info("Your LuckPerms version is outdated! :(");
                        if (manager.isPluginEnabled("Vault")) {
                            RegisteredServiceProvider<Permission> provider = this.getServer().getServicesManager().getRegistration(Permission.class);
                            if (provider != null) {
                                this.getLogger().info("Vault found instead! Try hooking...");
                                this.VaultEnabled = true;
                                this.vaultPerms = provider.getProvider();
                                this.getLogger().info("Successfully hooked into Vault!");
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
                this.getLogger().info("PlaceholderAPI found, try hooking...");
                this.PlaceholderAPIEnabled = true;
                this.getLogger().info("Successfully hooked into PlaceholderAPI!");
            }
            if (!manager.isPluginEnabled("Citizens"))
                this.getLogger().info("Citizens not found!");
            else {
                this.getLogger().info("Citizens found, try hooking...");
                manager.registerEvents(new CitizensActions(this), this);
                this.CitizensEnabled = true;
                this.getLogger().info("Successfully hooked into Citizens!");
            }
            if (!manager.isPluginEnabled("Authme"))
                this.getLogger().info("Authme not found!");
            else {
                this.getLogger().info("AuthMe found, try hooking...");
                manager.registerEvents(new AuthmeActions(this), this);
                this.AuthMeEnabled = true;
                this.getLogger().info("Successfully hooked into AuthMe!");
            }

            this.playerActionsListener = new PlayerActions(this);
            this.serverActionsListener = new ServerActions(this);

            if (!this.reloadPlugin())
                throw new IllegalStateException("Failed to load settings!");

            if (this.settings.isMetricsEnabled()) {
                Metrics metrics = new Metrics(this, 18026);
                metrics.addCustomChart(new SimplePie("database_type", () ->
                        switch (this.settings.getStorageSettings().getDatabaseType()) {
                            case JSON -> "JSON";
                            case MYSQL -> "MySQL";
                            case SQLITE -> "SQLite";
                            case H2 -> "H2";
                            case MARIADB -> "MariaDB";
                        }));
            }

            manager.registerEvents(this.playerActionsListener, this);
            manager.registerEvents(this.serverActionsListener, this);

            PluginCommand astralBooksCommand = this.getCommand("abook");
            if (astralBooksCommand != null)
                astralBooksCommand.setExecutor(new AstralBooksCommand(this));
            if (CommodoreProvider.isSupported()) {
                this.getLogger().info("Loading Brigardier support...");
                Commodore commodore = CommodoreProvider.getCommodore(this);
                this.getLogger().info("  Command /abook: " + (this.registerCompletions(commodore, astralBooksCommand) ? "supported" : "unsupported"));
            } else
                this.getLogger().info("Brigardier is not supported on this version!");
            //Update checker, by default enabled
            if (this.settings.isUpdateCheck())
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
        if (this.serverActionsListener != null)
            this.serverActionsListener.onDisable();
        if (this.pluginStorage != null && this.isEnabled())
            this.pluginStorage.unload();
    }

    private boolean registerCompletions(Commodore commodore, PluginCommand command) {
        if (command == null)
            return false;
        try (InputStream is = this.getResource("command.commodore")) {
            if (is == null)
                throw new FileNotFoundException();
            commodore.register(command, CommodoreFileReader.INSTANCE.parse(is), player -> this.API.hasPermission(player, "astralbooks.tab.completer"));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean loadSettings() {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(this.settingsFile);
            config.options().setHeader(
                    List.of("""
                                                 _             _ ____              _       \s
                                       /\\       | |           | |  _ \\            | |      \s
                                      /  \\   ___| |_ _ __ __ _| | |_) | ___   ___ | | _____\s
                                     / /\\ \\ / __| __| '__/ _` | |  _ < / _ \\ / _ \\| |/ / __|
                                    / ____ \\\\__ \\ |_| | | (_| | | |_) | (_) | (_) |   <\\__ \\
                                   /_/    \\_\\___/\\__|_|  \\__,_|_|____/ \\___/ \\___/|_|\\_\\___/
                                                                                           \s
                                                                                           \s
                                """.split("\n"))
            );
            this.settings.load(config);
            return true;
        } catch (Exception ex) {
            this.getLogger().log(Level.WARNING, "Failed to load settings", ex);
            return false;
        }
    }

    public boolean saveSettings() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            this.settings.load(config);
            config.options().setHeader(
                    List.of("""
                                                 _             _ ____              _       \s
                                       /\\       | |           | |  _ \\            | |      \s
                                      /  \\   ___| |_ _ __ __ _| | |_) | ___   ___ | | _____\s
                                     / /\\ \\ / __| __| '__/ _` | |  _ < / _ \\ / _ \\| |/ / __|
                                    / ____ \\\\__ \\ |_| | | (_| | | |_) | (_) | (_) |   <\\__ \\
                                   /_/    \\_\\___/\\__|_|  \\__,_|_|____/ \\___/ \\___/|_|\\_\\___/
                                                                                           \s
                                                                                           \s
                                """.split("\n"))
            );
            config.save(this.settingsFile);
            return true;
        } catch (Exception ex) {
            this.getLogger().log(Level.WARNING, "Failed to save settings", ex);
            return false;
        }
    }

    public boolean reloadPlugin() {
        if (this.loadSettings()) {
            this.saveSettings();
            try {
                if (this.pluginStorage != null)
                    this.pluginStorage.unload();
                this.pluginStorage = new PluginStorage(this);
                this.pluginStorage.load(this.settings.getStorageSettings());
            } catch (SQLException ex) {
                this.getLogger().log(Level.SEVERE, "Could not load storage!", ex);
                return false;
            }
            if (this.playerActionsListener != null)
                this.playerActionsListener.onReload();
        }
        return true;
    }
}
