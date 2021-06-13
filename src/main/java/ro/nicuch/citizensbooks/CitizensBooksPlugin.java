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

import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ro.nicuch.citizensbooks.bstats.Metrics;

import java.io.File;

public class CitizensBooksPlugin extends JavaPlugin {
    private Permission vaultPerms;
    private LuckPerms luckPerms;
    private final CitizensBooksAPI api = new CitizensBooksAPI(this);
    private YamlConfiguration settings;
    private boolean usePlaceholderAPI, useAuthMe, useCitizens, useLuckPerms, useVault;
    public final int configVersion = 9;

    @Override
    public void onEnable() {
        try {
            this.getLogger().info("============== BEGIN LOAD ==============");
            this.reloadSettings();
            //bStats Metrics, by default enabled
            new Metrics(this);
            PluginManager manager = this.getServer().getPluginManager();
            if (!manager.isPluginEnabled("LuckPerms")) {
                this.getLogger().info("LuckPerms not found!");
                if (!manager.isPluginEnabled("Vault"))
                    this.getLogger().info("Vault not found!");
                else {
                    this.getLogger().info("Vault found, try hooking!");
                    this.useVault = true;
                    this.vaultPerms = this.getServer().getServicesManager().getRegistration(Permission.class).getProvider();
                }
            } else {
                this.getLogger().info("LuckPerms found, try hooking!");
                if (manager.getPlugin("LuckPerms").getDescription().getVersion().startsWith("5")) {
                    this.useLuckPerms = true;
                    this.luckPerms = this.getServer().getServicesManager().getRegistration(LuckPerms.class).getProvider();
                    if (manager.isPluginEnabled("Vault"))
                        this.getLogger().info("Vault plugin found, but we'll use LuckPerms!");
                } else {
                    this.getLogger().info("Your LuckPerms version is oudated! :(");
                    if (manager.isPluginEnabled("Vault")) {
                        this.getLogger().info("Vault found instead! Try hooking!");
                        this.useVault = true;
                        this.vaultPerms = this.getServer().getServicesManager().getRegistration(Permission.class).getProvider();
                    }
                }
            }
            if (!manager.isPluginEnabled("PlaceholderAPI"))
                this.getLogger().info("PlaceholderAPI not found!");
            else {
                this.getLogger().info("PlaceholderAPI found, try hooking!");
                this.usePlaceholderAPI = true;
            }
            manager.registerEvents(new PlayerActions(this), this);
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
            TabExecutor tabExecutor = new CitizensBooksCommand(this);
            this.getCommand("npcbook").setExecutor(tabExecutor);
            this.getCommand("npcbook").setTabCompleter(tabExecutor);
            //Update checker, by default enabled
            if (this.settings.getBoolean("update_check", true))
                manager.registerEvents(new UpdateChecker(this), this);
            this.getLogger().info("============== END LOAD ==============");
        } catch (Exception ex) {
            this.printError(ex); //StackOverflows are not catched directly, maybe this will help?
            this.getLogger().info("============== END LOAD ==============");
            this.setEnabled(false);
        }
    }

    public CitizensBooksAPI getAPI() {
        return this.api;
    }

    @Override
    public void onDisable() {
        this.saveSettings();
    }

    public YamlConfiguration getSettings() {
        return this.settings;
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
                if (!ConfigUpdater.updateConfig(this, this.settings.getInt("version"))) {
                    boolean renamed = config.renameTo(new File(
                            this.getDataFolder() + File.separator + "config_" + System.currentTimeMillis() + ".yml"));
                    if (renamed) {
                        this.getLogger().info("A new config.yml was generated!");
                        this.saveResource("config.yml", true);
                        //Load again the config
                        this.settings = YamlConfiguration.loadConfiguration(config);
                    } else
                        this.getLogger().info("Failed to generate a new config!");
                } else
                    this.getLogger().info("The config has been updated!");
            }
        } catch (Exception ex) {
            this.printError(ex); //Saving files can cause IOException
        }
    }

    public void saveSettings() {
        try {
            this.settings.save(new File(this.getDataFolder() + File.separator + "config.yml"));
        } catch (Exception ex) {
            this.printError(ex); //Saving files can cause IOException
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

    public String getMessage(String path, String def) {
        return ChatColor.translateAlternateColorCodes('&',
                this.settings.getString("lang.header", ConfigDefaults.header))
                + ChatColor.translateAlternateColorCodes('&', this.settings.getString(path, def));
    }

    public String getMessageNoHeader(String path, String def) {
        return ChatColor.translateAlternateColorCodes('&', this.settings.getString(path, def));
    }

    /*
     * author GamerKing195 (from AutoupdaterAPI)
     */
    public void printError(Exception ex) {
        this.getLogger().severe("A severe error has occurred with CitizensBooks.");
        this.getLogger().severe("If you cannot figure out this error on your own (e.g. a config error) please copy and paste everything from here to END ERROR and post it at https://github.com/nicuch/CitizensBooks/issues.");
        this.getLogger().severe("");
        this.getLogger().severe("============== BEGIN ERROR ==============");
        this.getLogger().severe("PLUGIN VERSION: CitizensBooks " + getDescription().getVersion());
        this.getLogger().severe("");
        this.getLogger().severe("MESSAGE: " + ex.getMessage());
        this.getLogger().severe("");
        this.getLogger().severe("STACKTRACE: ");
        ex.printStackTrace();
        this.getLogger().severe("");
        this.getLogger().severe("============== END ERROR ==============");
    }
}
