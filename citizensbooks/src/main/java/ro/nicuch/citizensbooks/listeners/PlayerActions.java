/*

    CitizensBooks
    Copyright (c) 2021 @ Drăghiciu 'nicuch' Nicolae

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
import org.jetbrains.annotations.NotNull;
import ro.nicuch.citizensbooks.CitizensBooksAPI;
import ro.nicuch.citizensbooks.CitizensBooksPlugin;
import ro.nicuch.citizensbooks.utils.Message;
import ro.nicuch.citizensbooks.utils.References;

import java.util.Objects;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class PlayerActions implements Listener {
    private final CitizensBooksPlugin plugin;
    private final CitizensBooksAPI api;
    private BukkitTask pullTask;
    private final DelayQueue<DelayedPlayer> delayedPlayers = new DelayQueue<>();

    public PlayerActions(CitizensBooksPlugin plugin) {
        this.plugin = plugin;
        this.api = this.plugin.getAPI();
        this.onReload();
    }

    public void onDisable() {
        if (this.pullTask != null)
            this.pullTask.cancel();
    }

    public void onReload() {
        if (this.pullTask != null)
            this.pullTask.cancel();
        if (!this.plugin.getSettings().getBoolean("join_book_enabled", false))
            return;
        this.pullTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            DelayedPlayer delayedPlayer = this.delayedPlayers.poll();
            while (delayedPlayer != null) {
                Player player = delayedPlayer.getPlayer();
                if (!this.plugin.getSettings().getBoolean("join_book_always_show", false)) {
                    if (this.plugin.getSettings().isLong("join_book_last_seen_by_players." + player.getUniqueId().toString()))
                        if (this.plugin.getSettings().getLong("join_book_last_seen_by_players." + player.getUniqueId().toString(), 0) >= this.plugin.getSettings().getLong("join_book_last_change", 0))
                            continue;
                    this.plugin.getSettings().set("join_book_last_seen_by_players." + player.getUniqueId().toString(), System.currentTimeMillis());
                    this.plugin.saveSettings();
                }
                this.api.openBook(player, this.api.placeholderHook(player, this.api.getJoinBook(), null));
                delayedPlayer = this.delayedPlayers.poll();
            }
        }, 1L, 1L);
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
        if (!this.plugin.getSettings().getBoolean("join_book_enabled", false))
            return;
        if (this.api.getJoinBook() == null)
            return;
        Player player = event.getPlayer();
        if (this.api.hasPermission(player, "npcbook.nojoinbook"))
            return;
        if (this.plugin.getSettings().getBoolean("join_book_enable_delay", false)) {
            int delay = this.plugin.getSettings().getInt("join_book_delay", 0);
            if (delay <= 0)
                delay = 0;
            this.delayedPlayers.offer(new DelayedPlayer(player, delay * 50L)); // delay (ticks) * 50 (milliseconds)
        } else
            this.delayedPlayers.offer(new DelayedPlayer(player, 0));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!this.plugin.getSettings().getBoolean("join_book_enabled", false))
            return;
        if (this.plugin.getSettings().getBoolean("join_book_enable_delay", false))
            this.delayedPlayers.remove(new DelayedPlayer(event.getPlayer(), 0));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onClickWithItem(PlayerInteractEvent event) {
        if (!this.plugin.isNBTAPIEnabled())
            return;
        if (!event.hasItem())
            return;
        ItemStack item = event.getItem();
        NBTItem nbtItem = new NBTItem(item);
        String filterName =
                switch (event.getAction()) {
                    case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> nbtItem.getString(References.NBTAPI_ITEM_LEFT_KEY);
                    case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> nbtItem.getString(References.NBTAPI_ITEM_RIGHT_KEY);
                    default -> null;
                };
        if (filterName == null || filterName.isEmpty())
            return;
        if (!this.api.hasFilter(filterName))
            return;
        ItemStack book = this.api.getFilter(filterName);
        this.api.openBook(event.getPlayer(), this.api.placeholderHook(event.getPlayer(), book));
        event.setCancelled(true);
    }

    private static class DelayedPlayer implements Delayed {
        private final long startTime = System.currentTimeMillis();
        private final long maxLifeTimeMillis;
        private final Player player;

        public DelayedPlayer(Player player, long maxLifeTimeMillis) {
            this.maxLifeTimeMillis = maxLifeTimeMillis;
            this.player = player;
        }

        public Player getPlayer() {
            return this.player;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DelayedPlayer that = (DelayedPlayer) o;
            return Objects.equals(this.player.getUniqueId(), that.player.getUniqueId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.player.getUniqueId());
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(getDelayMillis(), TimeUnit.MILLISECONDS);
        }

        private long getDelayMillis() {
            return (this.startTime + this.maxLifeTimeMillis) - System.currentTimeMillis();
        }

        @Override
        public int compareTo(@NotNull Delayed that) {
            return Long.compare(this.getDelayMillis(), ((DelayedPlayer) that).getDelayMillis());
        }
    }
}