/*

   CitizensBooks
   Copyright (c) 2018 @ Drăghiciu 'nicuch' Nicolae

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

package ro.nicuch.citizensbooks;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

/**
 * When the player right click an NPC before the book is opened.
 * Can also trigger PlayerEvent.
 */
public class BookNPCClickEvent extends PlayerEvent implements Cancellable {
    private final static HandlerList handlers = new HandlerList();
    private final NPC npc;
    private final CitizensBooksPlugin plugin; //Added this for devs who need to access the API
    private ItemStack book;
    private boolean usePlaceHolders = true;
    private boolean cancel;
    private final ClickType clickType;

    /**
     * @param player the player
     * @param npc    the NPC
     * @param book   the book
     * @param clickType the click type
     * @throws NullPointerException if book is null.
     */
    public BookNPCClickEvent(Player player, NPC npc, ItemStack book, ClickType clickType) {
        super(player);
        this.npc = npc;
        this.clickType = clickType;
        if ((this.book = book) == null)
            throw new NullPointerException("ItemStack can\'t be null!");
        this.plugin = (CitizensBooksPlugin) Bukkit.getPluginManager().getPlugin("CitizensBooks");
    }

    /**
     * Get the NPC that was right-clicked
     *
     * @return the NPC
     */
    public NPC getNPC() {
        return this.npc;
    }

    /**
     * Get the books that is opened after
     *
     * @return the book
     */
    public ItemStack getBook() {
        return this.book;
    }

    /**
     * Check if the PlaceholderAPI is gonna be used
     *
     * @return if PlaceholderAPI is gonna be used
     */
    public boolean usePlaceHolders() {
        return this.usePlaceHolders;
    }

    /**
     * Set if PlaceholderAPI is gonna be used, ignored
     * if PlaceholderAPI is disabled or not present
     *
     * @param usePlaceHolders if PlaceholderAPI is gonna be used
     */
    public void setPlaceHoldersUse(boolean usePlaceHolders) {
        this.usePlaceHolders = usePlaceHolders;
    }

    /**
     * Set the book that's gonna be opened after
     *
     * @param book the book
     * @throws NullPointerException     if the book is null
     * @throws IllegalArgumentException if the ItemStack is not a written book
     */
    public void setBook(ItemStack book) {
        if (book == null)
            throw new NullPointerException("ItemStack can\'t be null!");
        if (book.getType() != Material.WRITTEN_BOOK)
            throw new IllegalArgumentException("The filter can only be a written book!");
        this.book = book;
    }

    /**
     *
     * @return the click type (right or left click)
     */
    public ClickType getClickType() {
        return this.clickType;
    }

    /**
     * Get the plugin that implemented this event. (CitizensBooks)
     * You can access the API more easy in this way.
     *
     * @return the plugin
     */
    public CitizensBooksPlugin getCBPlugin() {
        return this.plugin;
    }

    /**
     * Check if event is cancelled
     *
     * @return if the event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * Cancel the event
     *
     * @param cancel if the event is cancelled
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public enum ClickType {
        LEFT, RIGHT
    }
}
