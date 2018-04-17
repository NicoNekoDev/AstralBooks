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

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;

public class BookNPCRightClickEvent extends PlayerEvent implements Cancellable {
    private final static HandlerList handlers = new HandlerList();
    private final NPC npc;
    private ItemStack book;
    private boolean usePlaceHolders = true;
    private boolean cancel;

    public BookNPCRightClickEvent(Player player, NPC npc, ItemStack book) {
        super(player);
        this.npc = npc;
        this.book = book;
    }

    public NPC getNPC() {
        return this.npc;
    }

    public ItemStack getBook() {
        return this.book;
    }

    public boolean usePlaceHolders() {
        return this.usePlaceHolders;
    }

    public void setPlaceHoldersUse(boolean usePlaceHolders) {
        this.usePlaceHolders = usePlaceHolders;
    }

    public void setBook(ItemStack book) {
        this.book = book;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

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
}
