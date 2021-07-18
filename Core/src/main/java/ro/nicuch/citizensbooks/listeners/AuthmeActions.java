package ro.nicuch.citizensbooks.listeners;

import com.google.gson.JsonParseException;
import fr.xephi.authme.events.LoginEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import ro.nicuch.citizensbooks.CitizensBooksAPI;
import ro.nicuch.citizensbooks.CitizensBooksPlugin;

import java.io.File;

public class AuthmeActions implements Listener {
    private final CitizensBooksPlugin plugin;
    private final CitizensBooksAPI api;
    private ItemStack joinBook;

    public AuthmeActions(CitizensBooksPlugin plugin) {
        this.plugin = plugin;
        this.api = this.plugin.getAPI();
        this.onReload();
    }

    public void onReload() {
        File joinBookFile = new File(this.plugin.getDataFolder() + File.separator + "join_book.json");
        if (joinBookFile.exists()) {
            try {
                this.joinBook = this.api.getBookFromJsonFile(joinBookFile);
            } catch (JsonParseException ex) {
                this.joinBook = null;
                this.plugin.getLogger().warning("Failed to load join book!");
            }
        }
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        if (!this.plugin.getSettings().isItemStack("join_book"))
            return;
        if (this.api.hasPermission(event.getPlayer(), "npcbook.nojoinbook"))
            return;
        Player player = event.getPlayer();
        if (this.plugin.getSettings().isLong("join_book_last_seen_by_players." + player.getUniqueId().toString()))
            if (this.plugin.getSettings().getLong("join_book_last_seen_by_players." + player.getUniqueId().toString(), 0) >= this.plugin.getSettings().getLong("join_book_last_change", 0))
                return;
        this.plugin.getSettings().set("join_book_last_seen_by_players." + player.getUniqueId().toString(), System.currentTimeMillis());
        this.plugin.saveSettings();
        if (this.joinBook == null)
            return;
        this.api.openBook(event.getPlayer(), this.api.placeholderHook(player, this.joinBook, null));
    }
}
