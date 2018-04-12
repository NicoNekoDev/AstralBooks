package ro.nicuch.citizensbooks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerActions implements Listener {
    private final CitizensBooks plugin;
    private final CitizensBooksAPI api;

    public PlayerActions(CitizensBooks plugin) {
        api = (this.plugin = plugin).getAPI();
    }

    @EventHandler
    public void event(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().substring(1).split(" ")[0];
        if (!this.plugin.getConfig().isString("commands." + command))
            return;
        event.setCancelled(true);
        String filterName = this.plugin.getConfig().getString("commands." + command);
        if (!this.api.hasFilter(filterName)) {
            player.sendMessage(this.plugin.getMessage("lang.no_book_for_filter", LangDefaults.no_book_for_filter));
            return;
        }
        ItemStack book = this.api.getFilter(filterName);
        api.openBook(event.getPlayer(), this.api.placeholderHook(event.getPlayer(), book));
    }

}