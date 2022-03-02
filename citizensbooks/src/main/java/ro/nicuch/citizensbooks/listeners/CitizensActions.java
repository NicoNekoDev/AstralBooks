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

package ro.nicuch.citizensbooks.listeners;

import net.citizensnpcs.api.event.NPCCloneEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import ro.nicuch.citizensbooks.events.BookNPCClickEvent;
import ro.nicuch.citizensbooks.CitizensBooksAPI;
import ro.nicuch.citizensbooks.CitizensBooksPlugin;

public class CitizensActions implements Listener {
    private final CitizensBooksPlugin plugin;
    private final CitizensBooksAPI api;

    public CitizensActions(CitizensBooksPlugin plugin) {
        this.api = (this.plugin = plugin).getAPI();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void rightClick(NPCRightClickEvent event) {
        int npcId = event.getNPC().getId();
        if (!this.api.hasNPCBook(npcId, "right_side"))
            return;
        ItemStack book = this.api.getNPCBook(npcId, "right_side", new ItemStack(Material.WRITTEN_BOOK));
        BookNPCClickEvent e = new BookNPCClickEvent(event.getClicker(), event.getNPC(), book, BookNPCClickEvent.ClickType.RIGHT);
        this.plugin.getServer().getPluginManager().callEvent(e);
        if (e.isCancelled())
            return;
        book = e.getBook();
        if (e.usePlaceHolders())
            this.api.openBook(event.getClicker(), this.api.placeholderHook(event.getClicker(), book, event.getNPC()));
        else
            this.api.openBook(event.getClicker(), book);
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void leftCLick(NPCLeftClickEvent event) {
        int npcId = event.getNPC().getId();
        if (!this.api.hasNPCBook(npcId, "left_side"))
            return;
        ItemStack book = this.api.getNPCBook(npcId, "left_side", new ItemStack(Material.WRITTEN_BOOK));
        BookNPCClickEvent e = new BookNPCClickEvent(event.getClicker(), event.getNPC(), book, BookNPCClickEvent.ClickType.LEFT);
        this.plugin.getServer().getPluginManager().callEvent(e);
        if (e.isCancelled())
            return;
        book = e.getBook();
        if (e.usePlaceHolders())
            this.api.openBook(event.getClicker(), this.api.placeholderHook(event.getClicker(), book, event.getNPC()));
        else
            this.api.openBook(event.getClicker(), book);
        event.setCancelled(true);
    }

    @EventHandler
    public void clone(NPCCloneEvent event) {
        int npcId = event.getNPC().getId();
        int cloneId = event.getClone().getId();
        ItemStack right_book = this.api.hasNPCBook(npcId, "right_side") ? this.api.getNPCBook(npcId, "right_side") : null;
        ItemStack left_book = this.api.hasNPCBook(npcId, "left_side") ? this.api.getNPCBook(npcId, "left_side") : null;
        if (right_book != null) this.api.putNPCBook(cloneId, "right_side", right_book);
        if (left_book != null) this.api.putNPCBook(cloneId, "left_side", left_book);
        this.api.saveNPCBooks(this.plugin.getLogger());
    }
}
