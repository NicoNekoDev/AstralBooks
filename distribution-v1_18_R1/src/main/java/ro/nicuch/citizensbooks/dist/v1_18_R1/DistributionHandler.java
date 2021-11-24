/*

    CitizensBooks
    Copyright (c) 2021 @ Drăghiciu 'nicuch' Nicolae

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */

package ro.nicuch.citizensbooks.dist.v1_18_R1;

import com.google.gson.*;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.world.InteractionHand;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftMetaBook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import ro.nicuch.citizensbooks.dist.Distribution;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DistributionHandler extends Distribution {
    private final Field pagesField;

    public DistributionHandler() throws NoSuchFieldException {
        super("1_18_R1");
        this.pagesField = CraftMetaBook.class.getDeclaredField("pages");
        this.pagesField.setAccessible(true);
    }

    public void sendRightClick(Player player) {
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundOpenBookPacket(InteractionHand.MAIN_HAND));
    }

    @Override
    public void setItemInHand(Player player, ItemStack item) {
        player.getInventory().setItemInMainHand(item);
    }

    @Override
    public ItemStack getItemInHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Override
    public JsonObject convertBookToJson(ItemStack book) {
        try {
            BookMeta bookMeta = (BookMeta) book.getItemMeta();
            List<String> pages = bookMeta.hasPages() ? (List<String>) this.pagesField.get(bookMeta) : new ArrayList<>();
            JsonArray jsonPages = new JsonArray();
            for (String page : pages) {
                jsonPages.add(super.gson.fromJson(page, JsonElement.class));
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
                    pages.add(jsonPage.toString());
            }
            this.pagesField.set(bookMeta, pages);
            newBook.setItemMeta(bookMeta);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return newBook;
    }
}