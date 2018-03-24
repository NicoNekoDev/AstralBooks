package ro.nicuch.citizensbooks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.event.NPCRightClickEvent;

public class CitizensActions implements Listener {
	private final CitizensBooks plugin;
	private final CitizensBooksAPI api;

	public CitizensActions(CitizensBooks plugin) {
		api = (this.plugin = plugin).getAPI();
	}

	@EventHandler
	public void event(NPCRightClickEvent event) {
		int npcId = event.getNPC().getId();
		if (!this.plugin.getConfig().isString("save." + npcId))
			return;
		ItemStack book = this.api.stringToBook(this.plugin.getConfig().getString("save." + npcId));
		if (book == null)
			return;
		BookNPCRightClickEvent e = new BookNPCRightClickEvent(event.getClicker(), event.getNPC(), book);
		this.plugin.getServer().getPluginManager().callEvent(e);
		if (e.isCancelled())
			return;
		book = e.getBook();
		if (book == null)
			return;
		if (e.usePlaceHolders())
			this.api.openBook(event.getClicker(), this.api.placeholderHook(event.getClicker(), book));
		else
			this.api.openBook(event.getClicker(), book);
	}

}
