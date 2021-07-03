package ro.nicuch.citizensbooks.dist;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Distribution {

    void sendRightClick(Player player);

    void setItemInHand(Player player, ItemStack item);

    ItemStack getItemInHand(Player player);

    JsonObject convertBookToJson(ItemStack book);

    ItemStack convertJsonToBook(JsonObject jsonBook);
}
