/*

    CitizensBooks
    Copyright (c) 2022 @ Drăghiciu 'NicoNekoDev' Nicolae

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

package ro.nicuch.astralbooks.dist.v1_8_R3;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import io.github.NicoNekoDev.SimpleTuples.func.TripletFunction;
import io.netty.buffer.Unpooled;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomPayload;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftMetaBook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import ro.nicuch.astralbooks.dist.Distribution;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DistributionHandler extends Distribution {
    private final Field pagesField;
    private final JsonParser parser = new JsonParser();

    public DistributionHandler(TripletFunction<Player, String, Optional<NPC>, String> papiStr, TripletFunction<Player, List<String>, Optional<NPC>, List<String>> papiStrList) throws NoSuchFieldException {
        super("1_8_R3", papiStr, papiStrList);
        this.pagesField = CraftMetaBook.class.getDeclaredField("pages");
        this.pagesField.setAccessible(true);
    }

    public void sendRightClick(Player player) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
                new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(Unpooled.buffer(256).setByte(0, (byte) 0).writerIndex(1)))
        );
    }

    @Override
    public void setItemInHand(Player player, ItemStack item) {
        player.setItemInHand(item);
    }

    @Override
    public ItemStack getItemInHand(Player player) {
        return player.getItemInHand();
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public JsonObject convertBookToJson(ItemStack book) throws IllegalAccessException {
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        List<IChatBaseComponent> pages = bookMeta.hasPages() ? (List<IChatBaseComponent>) this.pagesField.get(bookMeta) : new ArrayList<>();
        JsonArray jsonPages = new JsonArray();
        for (IChatBaseComponent page : pages) {
            jsonPages.add(this.parser.parse(IChatBaseComponent.ChatSerializer.a(page)));
        }
        JsonPrimitive jsonAuthor = new JsonPrimitive(bookMeta.hasAuthor() ? bookMeta.getAuthor() : "Server");
        JsonPrimitive jsonTitle = new JsonPrimitive(bookMeta.hasTitle() ? bookMeta.getTitle() : "Title");
        JsonObject jsonBook = new JsonObject();
        jsonBook.add("author", jsonAuthor);
        jsonBook.add("title", jsonTitle);
        jsonBook.add("pages", jsonPages);
        return jsonBook;
    }

    @Override
    public ItemStack convertJsonToBook(JsonObject jsonBook) throws IllegalAccessException {
        ItemStack newBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) newBook.getItemMeta();
        JsonPrimitive jsonAuthor = jsonBook.getAsJsonPrimitive("author");
        JsonPrimitive jsonTitle = jsonBook.getAsJsonPrimitive("title");
        JsonArray jsonPages = jsonBook.getAsJsonArray("pages");
        bookMeta.setAuthor(jsonAuthor.isString() ? jsonAuthor.getAsString() : "Server");
        bookMeta.setTitle(jsonTitle.isString() ? jsonTitle.getAsString() : "Title");
        List<IChatBaseComponent> pages = new ArrayList<>();
        for (JsonElement jsonPage : jsonPages) {
            pages.add(IChatBaseComponent.ChatSerializer.a(jsonPage.toString()));
        }
        this.pagesField.set(bookMeta, pages);
        newBook.setItemMeta(bookMeta);
        return newBook;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public ItemStack applyPlaceholders(Player player, ItemStack book, NPC npc) throws IllegalAccessException {
        Preconditions.checkNotNull(book, "The ItemStack is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        List<String> pages = bookMeta.hasPages() ? super.papiReplaceStrList.apply(player, ((List<IChatBaseComponent>) this.pagesField.get(bookMeta)).stream().map(IChatBaseComponent.ChatSerializer::a).toList(), Optional.ofNullable(npc)) : new ArrayList<>();
        String author = bookMeta.hasAuthor() ? super.papiReplaceStr.apply(player, bookMeta.getAuthor(), Optional.ofNullable(npc)) : "Server";
        String title = bookMeta.hasTitle() ? super.papiReplaceStr.apply(player, bookMeta.getTitle(), Optional.ofNullable(npc)) : "Title";
        ItemStack newBook = new ItemStack(Material.WRITTEN_BOOK);
        bookMeta.setAuthor(author);
        bookMeta.setTitle(title);
        this.pagesField.set(bookMeta, pages.stream().map(IChatBaseComponent.ChatSerializer::a).toList());
        newBook.setItemMeta(bookMeta);
        return newBook;
    }

    @Override
    public ItemStack applyPlaceholders(Player player, ItemStack book) throws IllegalAccessException {
        return this.applyPlaceholders(player, book, null);
    }
}
