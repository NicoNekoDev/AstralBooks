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

import io.github.NicoNekoDev.SimpleTuples.Pair;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ro.nicuch.citizensbooks.CitizensBooksAPI;
import ro.nicuch.citizensbooks.CitizensBooksPlugin;
import ro.nicuch.citizensbooks.persistent.item.ItemData;
import ro.nicuch.citizensbooks.utils.Message;
import ro.nicuch.citizensbooks.utils.PersistentKey;
import ro.nicuch.citizensbooks.utils.Side;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class PlayerActions implements Listener {
    private final CitizensBooksPlugin plugin;
    private final CitizensBooksAPI api;
    private BukkitTask pullTask;
    private final DelayQueue<DelayedPlayer> delayedJoinBookPlayers = new DelayQueue<>();
    private final DelayQueue<DelayedPlayer> delayedInteractionBookBlockOperators = new DelayQueue<>();
    private final Map<Player, Pair<ItemStack, Side>> interactionBookBlockOperatorsMap = new HashMap<>();
    private final DelayQueue<DelayedPlayer> delayedInteractionBookEntityOperators = new DelayQueue<>();
    private final Map<Player, Pair<ItemStack, Side>> interactionBookEntityOperatorsMap = new HashMap<>();

    public PlayerActions(CitizensBooksPlugin plugin) {
        this.plugin = plugin;
        this.api = this.plugin.getAPI();
        this.onReload();
    }

    public void setBookBlockOperator(Player player, ItemStack book, Side side) {
        this.delayedInteractionBookBlockOperators.add(new DelayedPlayer(player, 1000 * 60)); // 1 minute
        this.interactionBookBlockOperatorsMap.put(player, Pair.of(book, side));
    }

    public void setBookEntityOperator(Player player, ItemStack book, Side side) {
        this.delayedInteractionBookEntityOperators.add(new DelayedPlayer(player, 1000 * 60)); // 1 minute
        this.interactionBookEntityOperatorsMap.put(player, Pair.of(book, side));
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
            DelayedPlayer delayedInteractionBookBlockOperator = null;
            while (this.delayedInteractionBookBlockOperators.poll() != null)
                //noinspection DataFlowIssue
                this.interactionBookBlockOperatorsMap.remove(delayedInteractionBookBlockOperator.getPlayer());
            DelayedPlayer delayedInteractionBookEntityOperator = null;
            while (this.delayedInteractionBookEntityOperators.poll() != null)
                //noinspection DataFlowIssue
                this.interactionBookEntityOperatorsMap.remove(delayedInteractionBookEntityOperator.getPlayer());
            //
            boolean needSave = false;
            DelayedPlayer delayedJoinPlayer = this.delayedJoinBookPlayers.poll();
            while (delayedJoinPlayer != null) {
                Player player = delayedJoinPlayer.getPlayer();
                if (!this.plugin.getSettings().getBoolean("join_book_always_show", false)) {
                    if (this.plugin.getSettings().isLong("join_book_last_seen_by_players." + player.getUniqueId()))
                        if (this.plugin.getSettings().getLong("join_book_last_seen_by_players." + player.getUniqueId(), 0) >= this.plugin.getSettings().getLong("join_book_last_change", 0))
                            continue;
                    this.plugin.getSettings().set("join_book_last_seen_by_players." + player.getUniqueId(), System.currentTimeMillis());
                    needSave = true;
                }
                this.api.openBook(player, this.api.placeholderHook(player, this.api.getJoinBook(), null));
                delayedJoinPlayer = this.delayedJoinBookPlayers.poll();
            }
            if (!needSave) // save outside the while loop, only if needed
                this.plugin.saveSettings();
        }, 1L, 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        this.api.removeBookOfBlock(event.getBlock());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            return;
        Block block = event.getClickedBlock();
        ItemStack book = switch (event.getAction()) {
            case LEFT_CLICK_BLOCK -> this.api.getBookOfBlock(block, Side.LEFT);
            case RIGHT_CLICK_BLOCK -> this.api.getBookOfBlock(block, Side.RIGHT);
            default -> null;
        };
        if (book == null) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;
            if (!this.interactionBookBlockOperatorsMap.containsKey(event.getPlayer()))
                return;
            Pair<ItemStack, Side> pair = this.interactionBookBlockOperatorsMap.remove(event.getPlayer());
            if (pair.getFirstValue() == null) {
                this.api.removeBookOfBlock(block, pair.getSecondValue());
                event.getPlayer().sendMessage(this.plugin.getMessage(Message.BOOK_REMOVED_SUCCESSFULLY_FROM_BLOCK)
                        .replace("player", event.getPlayer().getName())
                        .replace("block_x", block.getX() + "")
                        .replace("block_y", block.getY() + "")
                        .replace("block_z", block.getZ() + "")
                        .replace("world", block.getWorld().getName())
                        .replace("type", block.getType().name())
                );
            }
            this.api.putBookOnBlock(block, pair.getFirstValue(), pair.getSecondValue());
            event.getPlayer().sendMessage(this.plugin.getMessage(Message.BOOK_APPLIED_SUCCESSFULLY_TO_BLOCK)
                    .replace("player", event.getPlayer().getName())
                    .replace("block_x", block.getX() + "")
                    .replace("block_y", block.getY() + "")
                    .replace("block_z", block.getZ() + "")
                    .replace("world", block.getWorld().getName())
                    .replace("type", block.getType().name())
            );
            return;
        }
        this.api.openBook(event.getPlayer(), book);
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
        if (!("none".equalsIgnoreCase(permission) || this.api.hasPermission(player, permission)))
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
            this.delayedJoinBookPlayers.offer(new DelayedPlayer(player, delay * 50L)); // delay (ticks) * 50 (milliseconds)
        } else
            this.delayedJoinBookPlayers.offer(new DelayedPlayer(player, 0));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!this.plugin.getSettings().getBoolean("join_book_enabled", false))
            return;
        if (this.plugin.getSettings().getBoolean("join_book_enable_delay", false))
            //noinspection ResultOfMethodCallIgnored
            this.delayedJoinBookPlayers.remove(new DelayedPlayer(event.getPlayer(), 0));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onClickWithItem(PlayerInteractEvent event) {
        if (!(this.plugin.isNBTAPIEnabled() || this.api.noNBTAPIRequired()))
            return;
        if (!event.hasItem())
            return;
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        ItemStack item = event.getItem();
        ItemData data = this.api.itemDataFactory(item);
        String filterName =
                switch (event.getAction()) {
                    case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> data.getString(PersistentKey.ITEM_LEFT_KEY);
                    case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> data.getString(PersistentKey.ITEM_RIGHT_KEY);
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