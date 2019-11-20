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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PlayerActions implements Listener {
    private final CitizensBooksPlugin plugin;
    private final CitizensBooksAPI api;

    public PlayerActions(CitizensBooksPlugin plugin) {
        api = (this.plugin = plugin).getAPI();
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().substring(1).split(" ")[0];
        if (!this.plugin.getSettings().isString("commands." + command))
            return;
        event.setCancelled(true);
        String filterName = this.plugin.getSettings().getString("commands." + command);
        if (!this.api.hasFilter(filterName)) {
            player.sendMessage(this.plugin.getMessage("lang.no_book_for_filter", ConfigDefaults.no_book_for_filter));
            return;
        }
        ItemStack book = this.api.getFilter(filterName);
        api.openBook(event.getPlayer(), this.api.placeholderHook(event.getPlayer(), book, null));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (this.plugin.isAutmeEnabled())
            return;
        if (!this.plugin.getSettings().isString("join_book"))
            return;
        ItemStack book = this.api.stringToBook(this.plugin.getSettings().getString("join_book"));
        this.api.openBook(event.getPlayer(), book);
    }
}