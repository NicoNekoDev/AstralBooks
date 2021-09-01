package ro.nicuch.citizensbooks.dist;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class Distribution {
    final String version;

    public Distribution(final String version) {
        this.version = version;
    }

    public final String getVersion() {
        return this.version;
    }

    public abstract void sendRightClick(Player player);

    public abstract void setItemInHand(Player player, ItemStack item);

    public abstract ItemStack getItemInHand(Player player);

    public abstract JsonObject convertBookToJson(ItemStack book);

    public abstract ItemStack convertJsonToBook(JsonObject jsonBook);
}
