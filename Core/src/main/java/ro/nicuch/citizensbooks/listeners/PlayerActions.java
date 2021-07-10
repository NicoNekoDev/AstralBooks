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

package ro.nicuch.citizensbooks.listeners;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import ro.nicuch.citizensbooks.CitizensBooksAPI;
import ro.nicuch.citizensbooks.CitizensBooksPlugin;
import ro.nicuch.citizensbooks.utils.DelayHashMap;
import ro.nicuch.citizensbooks.utils.DelayMap;
import ro.nicuch.citizensbooks.utils.Message;
import ro.nicuch.citizensbooks.utils.References;

import java.util.UUID;

public class PlayerActions implements Listener {
    private final CitizensBooksPlugin plugin;
    private final CitizensBooksAPI api;
    private BukkitTask cleanupTask = null;
    private final DelayMap<UUID, BukkitTask> delayedPlayers = new DelayHashMap<>();

    public PlayerActions(CitizensBooksPlugin plugin) {
        this.plugin = plugin;
        this.api = this.plugin.getAPI();

    }

    public void onDisable() {
        if (this.cleanupTask != null)
            this.cleanupTask.cancel();
    }

    public void onReload() {
        if (this.plugin.getSettings().getBoolean("join_book_enable_delay", false))
            this.cleanupTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this.delayedPlayers::cleanup, 1L, 1L);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().substring(1).split(" ")[0];
        if (!this.plugin.getSettings().isString("commands." + command + ".filter_name"))
            return;
        event.setCancelled(true);
        String filterName = this.plugin.getSettings().getString("commands." + command + ".filter_name");
        String permission = this.plugin.getSettings().isString("commands." + command + ".permission") ? this.plugin.getSettings().getString("commands." + command + ".permission") : "none";
        if (!(permission.equalsIgnoreCase("none") || this.api.hasPermission(player, permission)))
            return;
        if (!this.api.hasFilter(filterName)) {
            player.sendMessage(this.plugin.getMessage(Message.NO_BOOK_FOR_FILTER));
            return;
        }
        ItemStack book = this.api.getFilter(filterName);
        this.api.openBook(event.getPlayer(), this.api.placeholderHook(player, book, null));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (this.plugin.isAuthmeEnabled())
            return;
        if (!this.plugin.getSettings().isItemStack("join_book"))
            return;
        if (this.api.hasPermission(event.getPlayer(), "npcbook.nojoinbook"))
            return;
        Player player = event.getPlayer();
        if (!this.plugin.getSettings().getBoolean("join_book_always_show", false)) {
            if (this.plugin.getSettings().isLong("join_book_last_seen_by_players." + player.getUniqueId().toString()))
                if (this.plugin.getSettings().getLong("join_book_last_seen_by_players." + player.getUniqueId().toString(), 0) >= this.plugin.getSettings().getLong("join_book_last_change", 0))
                    return;
            this.plugin.getSettings().set("join_book_last_seen_by_players." + player.getUniqueId().toString(), System.currentTimeMillis());
            this.plugin.saveSettings();
        }
        ItemStack book = this.plugin.getSettings().getItemStack("join_book");
        if (book == null)
            return;
        if (this.plugin.getSettings().getBoolean("join_book_enable_delay", false)) {
            int delay = this.plugin.getSettings().getInt("join_book_delay", 0);
            if (delay <= 0)
                this.api.openBook(event.getPlayer(), this.api.placeholderHook(player, book, null));
            else
                this.delayedPlayers.put(player.getUniqueId(),
                        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.api.openBook(event.getPlayer(), this.api.placeholderHook(player, book, null)), delay)); // 0 ticks by default
        } else
            this.api.openBook(event.getPlayer(), this.api.placeholderHook(player, book, null));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (this.plugin.getSettings().getBoolean("join_book_enable_delay", false))
            if (this.delayedPlayers.containsKey(event.getPlayer().getUniqueId())) {
                BukkitTask task = this.delayedPlayers.remove(event.getPlayer().getUniqueId());
                if (task == null) return;
                task.cancel();
            }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onClickWithItem(PlayerInteractEvent event) {
        if (!this.plugin.isNBTAPIEnabled())
            return;
        if (!event.hasItem())
            return;
        ItemStack item = event.getItem();
        NBTItem nbtItem = new NBTItem(item);
        String filterName = null;
        switch (event.getAction()) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                filterName = nbtItem.getString(References.NBTAPI_ITEM_LEFT_KEY);
                break;
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                filterName = nbtItem.getString(References.NBTAPI_ITEM_RIGHT_KEY);
                break;
            default:
                break;
        }
        if (filterName == null || filterName.isEmpty())
            return;
        if (!this.api.hasFilter(filterName))
            return;
        ItemStack book = this.api.getFilter(filterName);
        this.api.openBook(event.getPlayer(), this.api.placeholderHook(event.getPlayer(), book, null));
        event.setCancelled(true);
    }
}