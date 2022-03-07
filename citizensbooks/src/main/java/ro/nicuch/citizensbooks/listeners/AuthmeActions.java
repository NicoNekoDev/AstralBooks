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

import fr.xephi.authme.events.LoginEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ro.nicuch.citizensbooks.CitizensBooksAPI;
import ro.nicuch.citizensbooks.CitizensBooksPlugin;

public class AuthmeActions implements Listener {
    private final CitizensBooksPlugin plugin;
    private final CitizensBooksAPI api;

    public AuthmeActions(CitizensBooksPlugin plugin) {
        this.plugin = plugin;
        this.api = this.plugin.getAPI();
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        if (!this.plugin.getSettings().getBoolean("join_book_enabled", false))
            return;
        if (this.api.getJoinBook() == null)
            return;
        Player player = event.getPlayer();
        if (this.api.hasPermission(player, "npcbook.nojoinbook"))
            return;
        if (!this.plugin.getSettings().getBoolean("join_book_always_show", false)) {
            if (this.plugin.getSettings().isLong("join_book_last_seen_by_players." + player.getUniqueId()))
                if (this.plugin.getSettings().getLong("join_book_last_seen_by_players." + player.getUniqueId(), 0) >= this.plugin.getSettings().getLong("join_book_last_change", 0))
                    return;
            this.plugin.getSettings().set("join_book_last_seen_by_players." + player.getUniqueId(), System.currentTimeMillis());
            this.plugin.saveSettings();
        }
        this.api.openBook(event.getPlayer(), this.api.placeholderHook(player, this.api.getJoinBook(), null));
    }
}
