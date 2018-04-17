package ro.nicuch.citizensbooks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateChecker implements Listener {
    private final CitizensBooks plugin;
    private final int resourceId = 37465;
    private String latestVersion;
    private boolean updateAvailable;

    public UpdateChecker(CitizensBooks plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            Bukkit.getScheduler().runTask(this.plugin, () -> this.plugin.getLogger().info(ChatColor.GREEN + "Checking for updates..."));
            if (this.checkForUpdate())
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    this.plugin.getLogger().info(ChatColor.GOLD + "An update for CitizensBooks (" + this.latestVersion + ") is available at:");
                    this.plugin.getLogger().info(ChatColor.GOLD + "https://www.spigotmc.org/resources/citizensbooks." + resourceId + "/");
                    Bukkit.getPluginManager().registerEvents(this, this.plugin);
                });
            else
                Bukkit.getScheduler().runTask(this.plugin, () ->
                        this.plugin.getLogger().info(ChatColor.GREEN + "No new version available!"));
        }, 0, 30 * 60 * 20);
    }

    private String getSpigotVersion() {
        try {
            HttpsURLConnection con = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openConnection();
            con.setRequestMethod("GET");
            return new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
        } catch (Exception ex) {
            this.plugin.getLogger().warning(ChatColor.RED + "Failed to check for a update on spigot.");
        }
        return null;
    }

    private boolean checkForUpdate() {
        String version = getSpigotVersion();
        if (version != null) {
            if (this.plugin.getDescription().getVersion().compareTo(version) < 0) {
                this.latestVersion = version;
                this.updateAvailable = true;
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!this.plugin.getPermission().has(player, "npcbook.notify"))
            return;
        if (!this.updateAvailable)
            return;
        player.sendMessage(this.plugin.getMessage("new_version_available", LangDefaults.new_version_available)
                .replace("%latest_version%", this.latestVersion == null ? "" : this.latestVersion).replace("%current_version%", this.plugin.getDescription().getVersion()));
    }
}
