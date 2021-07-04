package ro.nicuch.citizensbooks.dist.v1_16_R3;

import com.google.gson.*;
import net.minecraft.server.v1_16_R3.EnumHand;
import net.minecraft.server.v1_16_R3.PacketPlayOutOpenBook;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftMetaBook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import ro.nicuch.citizensbooks.dist.Distribution;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DistributionHandler implements Distribution {
    private final Field pagesField;
    private final JsonParser parser = new JsonParser();

    public DistributionHandler() throws NoSuchFieldException {
        this.pagesField = CraftMetaBook.class.getDeclaredField("pages");
        this.pagesField.setAccessible(true);
    }

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

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    @Override
    public JsonObject convertBookToJson(ItemStack book) {
        try {
            BookMeta bookMeta = (BookMeta) book.getItemMeta();
            List<String> pages = bookMeta.hasPages() ? (List<String>) this.pagesField.get(bookMeta) : new ArrayList<>();
            JsonArray jsonPages = new JsonArray();
            for (String page : pages) {
                jsonPages.add(this.parser.parse(page).getAsJsonObject());
            }
            JsonPrimitive jsonAuthor = new JsonPrimitive(bookMeta.hasAuthor() ? bookMeta.getAuthor() : "Server");
            JsonPrimitive jsonTitle = new JsonPrimitive(bookMeta.hasTitle() ? bookMeta.getTitle() : "Title");
            JsonObject jsonBook = new JsonObject();
            jsonBook.add("author", jsonAuthor);
            jsonBook.add("title", jsonTitle);
            jsonBook.add("pages", jsonPages);
            return jsonBook;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new JsonObject();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public ItemStack convertJsonToBook(JsonObject jsonBook) {
        ItemStack newBook = new ItemStack(Material.WRITTEN_BOOK);
        try {
            BookMeta bookMeta = (BookMeta) newBook.getItemMeta();
            JsonPrimitive jsonAuthor = jsonBook.getAsJsonPrimitive("author");
            JsonPrimitive jsonTitle = jsonBook.getAsJsonPrimitive("title");
            JsonArray jsonPages = jsonBook.getAsJsonArray("pages");
            bookMeta.setAuthor(jsonAuthor.isString() ? jsonAuthor.getAsString() : "Server");
            bookMeta.setTitle(jsonTitle.isString() ? jsonTitle.getAsString() : "Title");
            List<String> pages = new ArrayList<>();
            for (JsonElement jsonPage : jsonPages) {
                JsonObject page = jsonPage.getAsJsonObject();
                pages.add(page.toString());
            }
            this.pagesField.set(bookMeta, pages);
            newBook.setItemMeta(bookMeta);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return newBook;
    }
}
