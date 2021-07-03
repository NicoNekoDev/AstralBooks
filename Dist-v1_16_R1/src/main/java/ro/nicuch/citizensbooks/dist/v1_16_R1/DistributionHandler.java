package ro.nicuch.citizensbooks.dist.v1_16_R1;

import com.google.gson.JsonObject;
import net.minecraft.server.v1_16_R1.EnumHand;
import net.minecraft.server.v1_16_R1.PacketPlayOutOpenBook;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ro.nicuch.citizensbooks.dist.Distribution;

public class DistributionHandler implements Distribution {

    public void sendRightClick(Player player) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutOpenBook(EnumHand.MAIN_HAND));
    }

    @Override
    public void setItemInHand(Player player, ItemStack item) {
        player.getInventory().setItemInMainHand(item);
    }

    @Override
    public ItemStack getItemInHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override
    public JsonObject convertBookToJson(ItemStack book) {
        return null;
    }

    @Override
    public ItemStack convertJsonToBook(JsonObject jsonBook) {
        return null;
    }
}
