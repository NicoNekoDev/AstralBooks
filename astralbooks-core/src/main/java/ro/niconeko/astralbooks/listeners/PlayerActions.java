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

import io.github.NicoNekoDev.SimpleTuples.Pair;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ro.niconeko.astralbooks.AstralBooksCore;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.persistent.item.ItemData;
import ro.niconeko.astralbooks.utils.Message;
import ro.niconeko.astralbooks.utils.PersistentKey;
import ro.niconeko.astralbooks.utils.Side;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class PlayerActions implements Listener {
    private final AstralBooksPlugin plugin;
    private final AstralBooksCore api;
    private BukkitTask pullTask;
    private final DelayQueue<DelayedPlayer> delayedJoinBookPlayers = new DelayQueue<>();
    private final DelayQueue<DelayedPlayer> delayedInteractionBookBlockOperators = new DelayQueue<>();
    private final Map<Player, Pair<ItemStack, Side>> interactionBookBlockOperatorsMap = new HashMap<>();
    private final DelayQueue<DelayedPlayer> delayedInteractionBookEntityOperators = new DelayQueue<>();
    private final Map<Player, Pair<ItemStack, Side>> interactionBookEntityOperatorsMap = new HashMap<>();

    public PlayerActions(AstralBooksPlugin plugin) {
        this.plugin = plugin;
        this.api = this.plugin.getAPI();
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
        if (this.pullTask != null) {
            this.pullTask.cancel();
            this.pullTask = null;
        }
    }

    public void onReload() {
        if (this.pullTask != null) {
            this.pullTask.cancel();
            this.pullTask = null;
        }
        this.pullTask = Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            DelayedPlayer delayedInteractionBookBlockOperator;
            while ((delayedInteractionBookBlockOperator = this.delayedInteractionBookBlockOperators.poll()) != null) {
                this.interactionBookBlockOperatorsMap.remove(delayedInteractionBookBlockOperator.getPlayer());
            }
            DelayedPlayer delayedInteractionBookEntityOperator;
            while ((delayedInteractionBookEntityOperator = this.delayedInteractionBookEntityOperators.poll()) != null)
                this.interactionBookEntityOperatorsMap.remove(delayedInteractionBookEntityOperator.getPlayer());
            //
            if (this.plugin.getSettings().isJoinBookEnabled()) {
                DelayedPlayer delayedJoinPlayer;
                while ((delayedJoinPlayer = this.delayedJoinBookPlayers.poll()) != null) {
                    Player player = delayedJoinPlayer.getPlayer();
                    if (!this.plugin.getSettings().isJoinBookAlwaysShow()) {
                        if (this.plugin.getPluginStorage().hasJoinBookLastSeen(player))
                            if (this.plugin.getPluginStorage().getJoinBookLastSeen(player) >= this.plugin.getPluginStorage().getJoinBookLastChange())
                                continue;
                        this.plugin.getPluginStorage().setJoinBookLastSeen(player, System.currentTimeMillis());
                    }
                    this.api.openBook(player, this.api.placeholderHook(player, this.plugin.getPluginStorage().getJoinBook(), null));
                }
            }
        }, 20L, 20L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        this.api.removeBookOfBlock(event.getBlock());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (this.interactionBookBlockOperatorsMap.containsKey(event.getPlayer()) && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && !event.getPlayer().isSneaking()) {
            Pair<ItemStack, Side> pair = this.interactionBookBlockOperatorsMap.remove(event.getPlayer());
            Block block = event.getClickedBlock();
            if (pair.getFirstValue() == null) {
                this.api.removeBookOfBlock(block, pair.getSecondValue());
                event.getPlayer().sendMessage(this.plugin.getSettings().getMessageSettings().getMessage(Message.BOOK_REMOVED_SUCCESSFULLY_FROM_BLOCK)
                        .replace("%player%", event.getPlayer().getName())
                        .replace("%block_x%", String.valueOf(block.getX()))
                        .replace("%block_y%", String.valueOf(block.getY()))
                        .replace("%block_z%", String.valueOf(block.getZ()))
                        .replace("%world%", block.getWorld().getName())
                        .replace("%type%", block.getType().name())
                );
                event.setCancelled(true);
                return;
            }
            this.api.putBookOnBlock(block, pair.getFirstValue(), pair.getSecondValue());
            event.getPlayer().sendMessage(this.plugin.getSettings().getMessageSettings().getMessage(Message.BOOK_APPLIED_SUCCESSFULLY_TO_BLOCK)
                    .replace("%player%", event.getPlayer().getName())
                    .replace("%block_x%", String.valueOf(block.getX()))
                    .replace("%block_y%", String.valueOf(block.getY()))
                    .replace("%block_z%", String.valueOf(block.getZ()))
                    .replace("%world%", block.getWorld().getName())
                    .replace("%type%", block.getType().name())
            );
            event.setCancelled(true);
            return;
        }
        if (event.hasItem() && (this.plugin.isNBTAPIEnabled() || !this.api.getDistribution().isNBTAPIRequired())) {
            ItemStack item = event.getItem();
            ItemData data = this.api.itemDataFactory(item);
            String filterName =
                    switch (event.getAction()) {
                        case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> data.getString(PersistentKey.ITEM_LEFT_KEY);
                        case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> data.getString(PersistentKey.ITEM_RIGHT_KEY);
                        default -> null;
                    };
            if (filterName != null && !filterName.isEmpty() && this.plugin.getPluginStorage().hasFilterBook(filterName)) {
                ItemStack book = this.plugin.getPluginStorage().getFilterBook(filterName);
                this.api.openBook(event.getPlayer(), this.api.placeholderHook(event.getPlayer(), book));
                event.setCancelled(true);
                return;
            }
        }
        if (!(event.hasItem() && event.getPlayer().isSneaking()) && event.getClickedBlock() != null && this.api.getDistribution().isPersistentDataContainerEnabled()) {
            Block block = event.getClickedBlock();
            ItemStack book = switch (event.getAction()) {
                case LEFT_CLICK_BLOCK -> this.api.getBookOfBlock(block, Side.LEFT);
                case RIGHT_CLICK_BLOCK -> this.api.getBookOfBlock(block, Side.RIGHT);
                default -> null;
            };
            if (book == null)
                return;
            this.api.openBook(event.getPlayer(), book);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        Entity entity = event.getRightClicked();
        if (this.interactionBookEntityOperatorsMap.containsKey(event.getPlayer()) && !event.getPlayer().isSneaking()) {
            if (this.plugin.isCitizensEnabled()) {
                if (CitizensAPI.getNPCRegistry().isNPC(entity)) {
                    event.getPlayer().sendMessage(this.plugin.getSettings().getMessageSettings().getMessage(Message.ENTITY_IS_NPC));
                    event.setCancelled(true);
                    return;
                }
            }
            Pair<ItemStack, Side> pair = this.interactionBookEntityOperatorsMap.remove(event.getPlayer());
            if (pair.getFirstValue() == null) {
                this.api.removeBookOfEntity(entity, pair.getSecondValue());
                event.getPlayer().sendMessage(this.plugin.getSettings().getMessageSettings().getMessage(Message.BOOK_REMOVED_SUCCESSFULLY_FROM_ENTITY)
                        .replace("%player%", event.getPlayer().getName())
                        .replace("%type%", entity.getType().name())
                );
                event.setCancelled(true);
                return;
            }
            this.api.putBookOnEntity(entity, pair.getFirstValue(), pair.getSecondValue());
            event.getPlayer().sendMessage(this.plugin.getSettings().getMessageSettings().getMessage(Message.BOOK_APPLIED_SUCCESSFULLY_TO_ENTITY)
                    .replace("%player%", event.getPlayer().getName())
                    .replace("%type%", entity.getType().name())
            );
            event.setCancelled(true);
            return;
        }
        ItemStack itemInPlayerHand = event.getPlayer().getInventory().getItemInMainHand();
        if (itemInPlayerHand.getType() != Material.AIR) {
            if (this.plugin.isNBTAPIEnabled() || !this.api.getDistribution().isNBTAPIRequired()) {
                ItemData data = this.api.itemDataFactory(itemInPlayerHand);
                String filterName = data.getString(PersistentKey.ITEM_RIGHT_KEY);
                if (filterName != null && !filterName.isEmpty() && this.plugin.getPluginStorage().hasFilterBook(filterName)) {
                    ItemStack book = this.plugin.getPluginStorage().getFilterBook(filterName);
                    this.api.openBook(event.getPlayer(), this.api.placeholderHook(event.getPlayer(), book));
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (!(itemInPlayerHand.getType() != Material.AIR && event.getPlayer().isSneaking())) {
            if (this.plugin.isCitizensEnabled())
                if (CitizensAPI.getNPCRegistry().isNPC(entity))
                    return;
            ItemStack book = this.api.getBookOfEntity(entity, Side.RIGHT);
            if (book == null)
                return;
            this.api.openBook(event.getPlayer(), book);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            Entity entity = event.getEntity();
            ItemStack itemInPlayerHand = player.getInventory().getItemInMainHand();
            if (itemInPlayerHand.getType() != Material.AIR) {
                if (this.plugin.isNBTAPIEnabled() || !this.api.getDistribution().isNBTAPIRequired()) {
                    ItemData data = this.api.itemDataFactory(itemInPlayerHand);
                    String filterName = data.getString(PersistentKey.ITEM_RIGHT_KEY);
                    if (filterName != null && !filterName.isEmpty() && this.plugin.getPluginStorage().hasFilterBook(filterName)) {
                        ItemStack book = this.plugin.getPluginStorage().getFilterBook(filterName);
                        this.api.openBook(player, this.api.placeholderHook(player, book));
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            if (!(itemInPlayerHand.getType() != Material.AIR && player.isSneaking())) {
                if (this.plugin.isCitizensEnabled())
                    if (CitizensAPI.getNPCRegistry().isNPC(entity))
                        return;
                ItemStack book = this.api.getBookOfEntity(entity, Side.LEFT);
                if (book == null)
                    return;
                this.api.openBook(player, book);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().substring(1).split(" ")[0];
        if (!this.plugin.getAPI().isValidName(command))
            return;
        if (!this.plugin.getPluginStorage().hasCommandFilter(command))
            return;
        event.setCancelled(true);
        Pair<String, String> filter = this.plugin.getPluginStorage().getCommandFilter(command);
        String filterName = filter.getFirstValue();
        String permission = filter.getSecondValue() != null || !filter.getSecondValue().isEmpty() ? filter.getSecondValue() : "none";
        if (!("none".equalsIgnoreCase(permission) || this.api.hasPermission(player, permission)))
            return;
        if (!this.plugin.getPluginStorage().hasFilterBook(filterName)) {
            player.sendMessage(this.plugin.getSettings().getMessageSettings().getMessage(Message.NO_BOOK_FOR_FILTER));
            return;
        }
        ItemStack book = this.plugin.getPluginStorage().getFilterBook(filterName);
        this.api.openBook(event.getPlayer(), this.api.placeholderHook(player, book, null));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (this.plugin.isAuthMeEnabled())
            return;
        if (!this.plugin.getSettings().isJoinBookEnabled())
            return;
        if (!this.plugin.getPluginStorage().hasJoinBook())
            return;
        Player player = event.getPlayer();
        if (this.api.hasPermission(player, "astralbooks.nojoinbook"))
            return;
        if (this.plugin.getSettings().isJoinBookEnableDelay()) {
            int delay = this.plugin.getSettings().getJoinBookDelay();
            if (delay <= 0)
                delay = 0;
            this.delayedJoinBookPlayers.offer(new DelayedPlayer(player, delay * 50L)); // delay (ticks) * 50 (milliseconds)
        } else
            this.delayedJoinBookPlayers.offer(new DelayedPlayer(player, 0));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!this.plugin.getSettings().isJoinBookEnabled())
            return;
        if (this.plugin.getSettings().isJoinBookEnableDelay())
            //noinspection ResultOfMethodCallIgnored
            this.delayedJoinBookPlayers.remove(new DelayedPlayer(event.getPlayer(), 0));
    }

    @EventHandler
    public void onBookSign(PlayerEditBookEvent event) {
        if (!this.plugin.getSettings().isBookSignSecurityEnabled())
            return;
        if (!event.isSigning())
            return;
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        book.setItemMeta(event.getNewBookMeta());
        this.api.putBookSecurity(event.getPlayer().getUniqueId(), new Date(), book);
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