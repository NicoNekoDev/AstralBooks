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

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.event.NPCRightClickEvent;

public class CitizensActions implements Listener {
    private final CitizensBooksPlugin plugin;
    private final CitizensBooksAPI api;

    public CitizensActions(CitizensBooksPlugin plugin) {
        api = (this.plugin = plugin).getAPI();
    }

    @EventHandler
    public void event(NPCRightClickEvent event) {
        int npcId = event.getNPC().getId();
        if (!this.plugin.getSettings().isString("save." + npcId + ".right_side"))
            return;
        ItemStack book = this.api.stringToBook(this.plugin.getSettings().getString("save." + npcId + ".right_side"));
        BookNPCClickEvent e = new BookNPCClickEvent(event.getClicker(), event.getNPC(), book, BookNPCClickEvent.ClickType.RIGHT);
        this.plugin.getServer().getPluginManager().callEvent(e);
        if (e.isCancelled())
            return;
        book = e.getBook();
        if (e.usePlaceHolders())
            this.api.openBook(event.getClicker(), this.api.placeholderHook(event.getClicker(), book, event.getNPC()));
        else
            this.api.openBook(event.getClicker(), book);
    }

    @EventHandler
    public void event(NPCLeftClickEvent event) {
        int npcId = event.getNPC().getId();
        if (!this.plugin.getSettings().isString("save." + npcId + ".left_side"))
            return;
        ItemStack book = this.api.stringToBook(this.plugin.getSettings().getString("save." + npcId + ".left_side"));
        BookNPCClickEvent e = new BookNPCClickEvent(event.getClicker(), event.getNPC(), book, BookNPCClickEvent.ClickType.LEFT);
        this.plugin.getServer().getPluginManager().callEvent(e);
        if (e.isCancelled())
            return;
        book = e.getBook();
        if (e.usePlaceHolders())
            this.api.openBook(event.getClicker(), this.api.placeholderHook(event.getClicker(), book, event.getNPC()));
        else
            this.api.openBook(event.getClicker(), book);
    }
}
