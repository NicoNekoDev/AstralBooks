package ro.nicuch.citizensbooks.dist.v1_17_R1;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ro.nicuch.citizensbooks.dist.Distribution;

public class DistributionHandler implements Distribution {

    public void sendRightClick(Player player) {
        // TODO
    }

    @Override
    public void setItemInHand(Player player, ItemStack item) {
        player.getInventory().setItemInMainHand(item);
    }

    @Override
    public ItemStack getItemInHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }
}
