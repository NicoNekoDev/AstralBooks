/*
 *     CitizensBooks
 *     Copyright (c) 2023 @ Drăghiciu 'NicoNekoDev' Nicolae
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package ro.niconeko.astralbooks.dist.v1_13_R2;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.Unpooled;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.v1_13_R2.IChatBaseComponent;
import net.minecraft.server.v1_13_R2.MinecraftKey;
import net.minecraft.server.v1_13_R2.PacketDataSerializer;
import net.minecraft.server.v1_13_R2.PacketPlayOutCustomPayload;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftMetaBook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.dist.Distribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DistributionHandler extends Distribution {

    public DistributionHandler(AstralBooksPlugin plugin) throws NoSuchFieldException {
        super(plugin, CraftMetaBook.class.getDeclaredField("pages"));
    }

    @Override
    public boolean isNBTAPIRequired() {
        return true;
    }

    @Override
    public boolean isPersistentDataContainerEnabled() {
        return false;
    }

    @Override
    public String getVersion() {
        return "1_13_R2";
    }

    @Override
    public void sendRightClick(Player player) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
                new PacketPlayOutCustomPayload(MinecraftKey.a("minecraft:book_open"), new PacketDataSerializer(Unpooled.buffer(256).setByte(0, (byte) 0).writerIndex(1)))
        );
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
    public JsonObject convertBookToJson(ItemStack book) throws IllegalAccessException {
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        List<IChatBaseComponent> pages = bookMeta.hasPages() ? (List<IChatBaseComponent>) this.pagesField.get(bookMeta) : new ArrayList<>();
        JsonArray jsonPages = new JsonArray();
        for (IChatBaseComponent page : pages) {
            jsonPages.add(super.gson.fromJson(IChatBaseComponent.ChatSerializer.a(page), JsonElement.class));
        }
        JsonPrimitive jsonAuthor = new JsonPrimitive(bookMeta.hasAuthor() ? bookMeta.getAuthor() : "Server");
        JsonPrimitive jsonTitle = new JsonPrimitive(bookMeta.hasTitle() ? bookMeta.getTitle() : "Title");
        JsonObject jsonBook = new JsonObject();
        jsonBook.add("author", jsonAuthor);
        jsonBook.add("title", jsonTitle);
        jsonBook.add("pages", jsonPages);
        return jsonBook;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public ItemStack convertJsonToBook(JsonObject jsonBook) throws IllegalAccessException {
        ItemStack newBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) newBook.getItemMeta();
        JsonElement jsonAuthor = jsonBook.get("author");
        JsonElement jsonTitle = jsonBook.get("title");
        JsonElement jsonPages = jsonBook.get("pages");
        bookMeta.setAuthor(jsonAuthor != null && jsonAuthor.isJsonPrimitive() ? jsonAuthor.getAsString() : "Server");
        bookMeta.setTitle(jsonTitle != null && jsonTitle.isJsonPrimitive() ? jsonTitle.getAsString() : "Title");
        List<IChatBaseComponent> pages = new ArrayList<>();
        if (jsonPages != null && jsonPages.isJsonArray())
            for (JsonElement jsonPage : ((JsonArray) jsonPages)) {
                pages.add(IChatBaseComponent.ChatSerializer.a(jsonPage.toString()));
            }
        this.pagesField.set(bookMeta, pages);
        newBook.setItemMeta(bookMeta);
        return newBook;
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    @Override
    public ItemStack applyPlaceholders(Player player, ItemStack book, NPC npc) throws IllegalAccessException {
        Preconditions.checkNotNull(book, "The ItemStack is null! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        Preconditions.checkArgument(book.getType() == Material.WRITTEN_BOOK, "The ItemStack is not a written book! This is not an error with CitizensBooks," +
                " so please don't report it. Make sure the plugins that uses CitizensBooks as dependency are correctly configured.");
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        List<String> pages = bookMeta.hasPages() ? super.placeholders(player, ((List<IChatBaseComponent>) this.pagesField.get(bookMeta)).stream().map(IChatBaseComponent.ChatSerializer::a).toList(), Optional.ofNullable(npc)) : new ArrayList<>();
        String author = bookMeta.hasAuthor() ? super.placeholders(player, bookMeta.getAuthor(), Optional.ofNullable(npc)) : "Server";
        String title = bookMeta.hasTitle() ? super.placeholders(player, bookMeta.getTitle(), Optional.ofNullable(npc)) : "Title";
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
