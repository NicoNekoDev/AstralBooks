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

import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.context.ContextManager;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.Optional;

public class UpdateChecker implements Listener {
    private final CitizensBooksPlugin plugin;
    private final int resourceId = 37465;
    private String latestVersion;
    private boolean updateAvailable;

    public UpdateChecker(CitizensBooksPlugin plugin) {
        this.plugin = plugin;
        //Async check if the plugin has an update.
        //The timer will start after the server finish loading
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            //Sync log that the plugin is checking for an update
            Bukkit.getScheduler().runTask(this.plugin, () -> this.plugin.getLogger().info("Checking for updates..."));
            //Checking for updates
            if (this.checkForUpdate()) {
                if (this.plugin.getSettings().getBoolean("auto_update", false)) {
                    Bukkit.getScheduler().runTask(this.plugin, () ->
                            this.plugin.getLogger().info("An update for CitizensBooks (v" + this.latestVersion + ") is available!"));
                    this.downloadNewVersion();
                } else
                    Bukkit.getScheduler().runTask(this.plugin, () -> {
                        this.plugin.getLogger().info("An update for CitizensBooks (v" + this.latestVersion + ") is available at:");
                        this.plugin.getLogger().info("https://www.spigotmc.org/resources/citizensbooks." + resourceId + "/");
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
            this.plugin.getLogger().warning("Failed to check for a update on spigot.");
        }
        return null;
    }

    private boolean checkForUpdate() {
        String version = getSpigotVersion();
        if (version != null) {
            if (this.plugin.getDescription().getVersion().compareTo(version) < 0) {
                this.latestVersion = version;
                this.updateAvailable = true;
                Bukkit.getScheduler().runTask(this.plugin, this::announceOnlinePlayers);
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (!this.plugin.getSettings().getBoolean("update_check", true))
            return;
        Player player = event.getPlayer();
        boolean useVault = this.plugin.isVaultEnabled(); //So we check if Vault is hooked
        if (this.plugin.getAPI().hasPermission(player, "npcbook.notify"))
            return;
        if (!this.updateAvailable)
            return;
        player.sendMessage(this.plugin.getMessage("new_version_available", ConfigDefaults.new_version_available)
                .replace("%latest_version%", this.latestVersion == null ? "" : this.latestVersion).replace("%current_version%", this.plugin.getDescription().getVersion()));
    }

    private void announceOnlinePlayers() {
        Bukkit.getOnlinePlayers().forEach((Player player) -> {
            if (this.plugin.getAPI().hasPermission(player, "npcbook.notify"))
                player.sendMessage(this.plugin.getMessage("new_version_available", ConfigDefaults.new_version_available)
                        .replace("%latest_version%", this.latestVersion == null ? "" : this.latestVersion).replace("%current_version%", this.plugin.getDescription().getVersion()));
        });
    }

    private void downloadNewVersion() {
        try (BufferedInputStream in =
                     new BufferedInputStream(new URL("http://repo.pikacraft.ro/ro/nicuch/CitizensBooks/" + this.latestVersion + "/CitizensBooks-" + this.latestVersion + ".jar").openStream());
             FileOutputStream fout = new FileOutputStream("plugins" + File.separator + "CitizensBooks-" + this.latestVersion + ".jar")) {
            final byte[] data = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1)
                fout.write(data, 0, count);
        } catch (Exception e) {
            Bukkit.getScheduler().runTask(this.plugin, () -> this.plugin.getLogger().info("Failed to download new version!"));
            return;
        }
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            this.plugin.getLogger().info("Succesfully downloaded update!");
            this.plugin.getLogger().info("Please restart the server!!");
            //Bukkit.shutdown(); Fuck
        });
    }
}
