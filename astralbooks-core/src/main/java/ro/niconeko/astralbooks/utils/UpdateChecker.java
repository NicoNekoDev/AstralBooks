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

package ro.niconeko.astralbooks.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ro.niconeko.astralbooks.AstralBooksPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.logging.Level;

public class UpdateChecker implements Listener {
    private final AstralBooksPlugin plugin;
    private final int resourceId = 37465;
    private static String latestVersion;
    private static boolean updateAvailable;

    public UpdateChecker(AstralBooksPlugin plugin) {
        this.plugin = plugin;
        //Async check if the plugin has an update.
        //The timer will start after the server finish loading
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            //Sync log that the plugin is checking for an update
            Bukkit.getScheduler().runTask(this.plugin, () -> this.plugin.getLogger().info("Checking for updates..."));
            //Checking for updates
            if (this.checkForUpdate()) {
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    this.plugin.getLogger().info("An update for AstralBooks (v" + latestVersion + ") is available at:");
                    this.plugin.getLogger().info("https://www.spigotmc.org/resources/astralbooks-a-book-for-everything." + resourceId + "/");
                    Bukkit.getScheduler().runTask(
                            this.plugin, () -> Bukkit.getOnlinePlayers().stream()
                                    .filter(player -> this.plugin.getAPI().hasPermission(player, "astralbooks.notify") || player.isOp()).forEach(player -> player.sendMessage(this.plugin.getSettings().getMessageSettings().getMessage(Message.NEW_VERSION_AVAILABLE)
                                            .replace("%latest_version%", latestVersion == null ? "" : latestVersion).replace("%current_version%", this.plugin.getDescription().getVersion()))));
                });
            } else
                Bukkit.getScheduler().runTask(this.plugin, () ->
                        this.plugin.getLogger().info("No new version available!"));
        }, 0, 30 * 60 * 20);
    }

    private String getSpigotVersion() {
        try {
            HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openConnection();
            con.setRequestMethod("GET");
            return new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
        } catch (Exception ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to check for update on spigot.");
        }
        return null;
    }

    private boolean checkForUpdate() {
        String version = getSpigotVersion();
        if (version != null) {
            if (this.plugin.getDescription().getVersion().compareTo(version) < 0) {
                latestVersion = version;
                updateAvailable = true;
                return true;
            }
        }
        return false;
    }

    public static boolean updateAvailable() {
        return updateAvailable;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (!this.plugin.getSettings().isUpdateCheck())
            return;
        Player player = event.getPlayer();
        if (!this.plugin.getAPI().hasPermission(player, "astralbooks.notify"))
            return;
        if (!updateAvailable)
            return;
        player.sendMessage(this.plugin.getSettings().getMessageSettings().getMessage(Message.NEW_VERSION_AVAILABLE)
                .replace("%latest_version%", latestVersion == null ? "" : latestVersion).replace("%current_version%", this.plugin.getDescription().getVersion()));
    }
}
