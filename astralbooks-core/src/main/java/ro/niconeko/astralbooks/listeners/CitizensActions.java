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

package ro.niconeko.astralbooks.listeners;

import net.citizensnpcs.api.event.NPCCloneEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import ro.niconeko.astralbooks.events.BookNPCClickEvent;
import ro.niconeko.astralbooks.AstralBooksCore;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.persistent.item.ItemData;
import ro.niconeko.astralbooks.utils.PersistentKey;
import ro.niconeko.astralbooks.utils.Side;

@SuppressWarnings("unused")
public class CitizensActions implements Listener {
    private final AstralBooksPlugin plugin;
    private final AstralBooksCore api;

    public CitizensActions(AstralBooksPlugin plugin) {
        this.api = (this.plugin = plugin).getAPI();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void rightClick(NPCRightClickEvent event) {
        ItemStack itemInPlayerHand = event.getClicker().getInventory().getItemInMainHand();
        if (itemInPlayerHand.getType() != Material.AIR) {
            if (this.plugin.isNBTAPIEnabled() || this.api.noNBTAPIRequired()) {
                ItemData data = this.api.itemDataFactory(itemInPlayerHand);
                String filterName = data.getString(PersistentKey.ITEM_RIGHT_KEY);
                if (filterName != null && !filterName.isEmpty() && this.plugin.getPluginStorage().hasFilterBook(filterName)) {
                    ItemStack book = this.plugin.getPluginStorage().getFilterBook(filterName);
                    this.api.openBook(event.getClicker(), this.api.placeholderHook(event.getClicker(), book));
                    event.setCancelled(true);
                    return;
                }
            }
        }
        int npcId = event.getNPC().getId();
        if (!this.plugin.getPluginStorage().hasNPCBook(npcId, Side.RIGHT))
            return;
        ItemStack book = this.plugin.getPluginStorage().getNPCBook(npcId, Side.RIGHT, new ItemStack(Material.WRITTEN_BOOK));
        BookNPCClickEvent e = new BookNPCClickEvent(event.getClicker(), event.getNPC(), book, Side.RIGHT);
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
        ItemStack itemInPlayerHand = event.getClicker().getInventory().getItemInMainHand();
        if (itemInPlayerHand.getType() != Material.AIR) {
            if (this.plugin.isNBTAPIEnabled() || this.api.noNBTAPIRequired()) {
                ItemData data = this.api.itemDataFactory(itemInPlayerHand);
                String filterName = data.getString(PersistentKey.ITEM_LEFT_KEY);
                if (filterName != null && !filterName.isEmpty() && this.plugin.getPluginStorage().hasFilterBook(filterName)) {
                    ItemStack book = this.plugin.getPluginStorage().getFilterBook(filterName);
                    this.api.openBook(event.getClicker(), this.api.placeholderHook(event.getClicker(), book));
                    event.setCancelled(true);
                    return;
                }
            }
        }
        int npcId = event.getNPC().getId();
        if (!this.plugin.getPluginStorage().hasNPCBook(npcId, Side.LEFT))
            return;
        ItemStack book = this.plugin.getPluginStorage().getNPCBook(npcId, Side.LEFT, new ItemStack(Material.WRITTEN_BOOK));
        BookNPCClickEvent e = new BookNPCClickEvent(event.getClicker(), event.getNPC(), book, Side.LEFT);
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
        ItemStack right_book = this.plugin.getPluginStorage().hasNPCBook(npcId, Side.RIGHT) ? this.plugin.getPluginStorage().getNPCBook(npcId, Side.RIGHT) : null;
        ItemStack left_book = this.plugin.getPluginStorage().hasNPCBook(npcId, Side.LEFT) ? this.plugin.getPluginStorage().getNPCBook(npcId, Side.LEFT) : null;
        if (right_book != null) this.plugin.getPluginStorage().putNPCBook(cloneId, Side.RIGHT, right_book);
        if (left_book != null) this.plugin.getPluginStorage().putNPCBook(cloneId, Side.LEFT, left_book);
    }
}
