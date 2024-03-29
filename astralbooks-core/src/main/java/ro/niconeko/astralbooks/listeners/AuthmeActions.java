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

import fr.xephi.authme.events.LoginEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ro.niconeko.astralbooks.AstralBooksCore;
import ro.niconeko.astralbooks.AstralBooksPlugin;

@SuppressWarnings("unused")
public class AuthmeActions implements Listener {
    private final AstralBooksPlugin plugin;
    private final AstralBooksCore api;

    public AuthmeActions(AstralBooksPlugin plugin) {
        this.plugin = plugin;
        this.api = this.plugin.getAPI();
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        if (!this.plugin.getSettings().isJoinBookEnabled())
            return;
        if (!this.plugin.getPluginStorage().hasJoinBook())
            return;
        Player player = event.getPlayer();
        if (this.api.hasPermission(player, "astralbooks.nojoinbook"))
            return;
        if (!this.plugin.getSettings().isJoinBookAlwaysShow()) {
            if (this.plugin.getPluginStorage().hasJoinBookLastSeen(player))
                if (this.plugin.getPluginStorage().getJoinBookLastSeen(player) >= this.plugin.getPluginStorage().getJoinBookLastChange())
                    return;
            this.plugin.getPluginStorage().setJoinBookLastSeen(player, System.currentTimeMillis());
        }
        this.api.openBook(event.getPlayer(), this.api.placeholderHook(player, this.plugin.getPluginStorage().getJoinBook(), null));
    }
}
