package ro.nicuch.citizensbooks;

import fr.xephi.authme.events.LoginEvent;
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
        ItemStack book = this.api.stringToBook(this.plugin.getSettings().getString("join_book"));
        this.api.openBook(event.getPlayer(), book);
    }
}
