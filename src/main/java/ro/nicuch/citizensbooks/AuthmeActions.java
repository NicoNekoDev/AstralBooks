package ro.nicuch.citizensbooks;

import fr.xephi.authme.events.LoginEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class AuthmeActions implements Listener {
    private final CitizensBooksPlugin plugin;
    private final CitizensBooksAPI api;

    public AuthmeActions(CitizensBooksPlugin plugin) {
        api = (this.plugin = plugin).getAPI();
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        if (!this.plugin.getSettings().isString("join_book"))
            return;
        Player player = event.getPlayer();
        if (this.plugin.getSettings().isLong("join_book_last_seen_by_players." + player.getUniqueId().toString()))
            if (this.plugin.getSettings().getLong("join_book_last_seen_by_players." + player.getUniqueId().toString(), 0) >= this.plugin.getSettings().getLong("join_book_last_change", 0))
                return;
        this.plugin.getSettings().set("join_book_last_seen_by_players." + player.getUniqueId().toString(), System.currentTimeMillis());
        this.plugin.saveSettings();
        ItemStack book = this.api.stringToBook(this.plugin.getSettings().getString("join_book"));
        this.api.openBook(player, book);
    }
}
