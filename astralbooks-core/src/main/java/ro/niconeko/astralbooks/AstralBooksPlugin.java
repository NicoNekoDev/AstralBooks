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
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ro.niconeko.astralbooks.api.AstralBooks;
import ro.niconeko.astralbooks.commands.AstralBooksCommand;
import ro.niconeko.astralbooks.listeners.PlayerActions;
import ro.niconeko.astralbooks.listeners.ServerActions;
import ro.niconeko.astralbooks.managers.BossBarManager;
import ro.niconeko.astralbooks.managers.HooksManager;
import ro.niconeko.astralbooks.managers.SettingsManager;
import ro.niconeko.astralbooks.storage.PluginStorage;
import ro.niconeko.astralbooks.utils.MessageUtils;
import ro.niconeko.astralbooks.utils.PersistentKey;
import ro.niconeko.astralbooks.utils.UpdateChecker;
import ro.niconeko.astralbooks.values.Settings;

import java.io.File;
import java.sql.SQLException;

public class AstralBooksPlugin extends JavaPlugin implements AstralBooks {
    private final File settingsFile = new File(getDataFolder(), "settings.yml");
    @Getter private Permission vaultPerms;
    @Getter private final AstralBooksCore API = new AstralBooksCore(this);
    @Getter private PluginStorage pluginStorage;
    @Getter private boolean PlaceholderAPIEnabled, AuthMeEnabled, CitizensEnabled, LuckPermsEnabled, VaultEnabled;
    @Getter private PlayerActions playerActionsListener;
    @Getter private ServerActions serverActionsListener;

    @Override
    public void onEnable() {
        MessageUtils.sendMessage(Bukkit.getConsoleSender(), "&a============== &fAstralBooks &a=============");

        if (!this.API.loadDistribution()) {
            MessageUtils.sendMessage(Bukkit.getConsoleSender(), "Failed to load distribution... disabling the plugin!");
            this.setEnabled(false);
            MessageUtils.sendMessage(Bukkit.getConsoleSender(), "&a========================================");
            return;
        }
        if (!PersistentKey.init(this)) {
            MessageUtils.sendMessage(Bukkit.getConsoleSender(), "&cFailed to load PersistentKey!");
            this.setEnabled(false);
            MessageUtils.sendMessage(Bukkit.getConsoleSender(), "&a========================================");
            return;
        }

        HooksManager.load(this);
        BossBarManager.load(this);

        this.playerActionsListener = new PlayerActions(this);
        this.serverActionsListener = new ServerActions(this);

        if (!this.reloadPlugin()) {
            this.setEnabled(false);
            MessageUtils.sendMessage(Bukkit.getConsoleSender(), "&a========================================");
            return;
        }

        if (Settings.METRICS_ENABLED.get()) {
            Metrics metrics = new Metrics(this, 18026);
            metrics.addCustomChart(new SimplePie("database_type", () ->
                    switch (Settings.STORAGE.get().TYPE.get()) {
                        case JSON -> "JSON";
                        case MYSQL -> "MySQL";
                        case SQLITE -> "SQLite";
                        case H2 -> "H2";
                        case MARIADB -> "MariaDB";
                    }));
        }

        Bukkit.getPluginManager().registerEvents(this.playerActionsListener, this);
        Bukkit.getPluginManager().registerEvents(this.serverActionsListener, this);

        // I hate reflections
        API.getDistribution().register(new AstralBooksCommand(this));

        //Update checker, by default enabled
        if (Settings.UPDATE_CHECK.get())
            Bukkit.getPluginManager().registerEvents(new UpdateChecker(this), this);
        MessageUtils.sendMessage(Bukkit.getConsoleSender(), "&a========================================");
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

    public boolean reloadPlugin() {
        if (SettingsManager.loadAndSave(this)) {
            try {
                if (this.pluginStorage != null)
                    this.pluginStorage.unload();
                this.pluginStorage = new PluginStorage(this);
                this.pluginStorage.load(Settings.STORAGE.get());
            } catch (SQLException ex) {
                MessageUtils.sendMessage(Bukkit.getConsoleSender(), "&cFailed to load storage!");
                ex.printStackTrace();
                return false;
            }
            if (this.playerActionsListener != null)
                this.playerActionsListener.onReload();
        }
        return true;
    }
}
