package ro.nicuch.citizensbooks.dist;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Distribution {

    void sendRightClick(Player player);

    void setItemInHand(Player player, ItemStack item);

    ItemStack getItemInHand(Player player);
}
