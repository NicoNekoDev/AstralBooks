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

package ro.niconeko.astralbooks;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import ro.niconeko.astralbooks.persistent.item.ItemData;
import ro.niconeko.astralbooks.settings.MessageSettings;
import ro.niconeko.astralbooks.storage.StorageType;
import ro.niconeko.astralbooks.utils.Message;
import ro.niconeko.astralbooks.utils.PersistentKey;
import ro.niconeko.astralbooks.utils.Side;
import ro.niconeko.astralbooks.utils.tuples.PairTuple;
import ro.niconeko.astralbooks.utils.tuples.TripletTuple;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AstralBooksCommand implements TabExecutor {
    private final AstralBooksPlugin plugin;
    private final AstralBooksCore api;

    public AstralBooksCommand(AstralBooksPlugin plugin) {
        this.api = (this.plugin = plugin).getAPI();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Optional<Player> player = this.isPlayer(sender) ? Optional.of((Player) sender) : Optional.empty();
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        Side side;
        PersistentKey action;
        if (args.length > 0) {
            switch (args[0]) {
                case "help" -> {
                    if (!this.api.hasPermission(sender, "astralbooks.command.help")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (args.length > 1) {
                        try {
                            int page = Integer.parseInt(args[1]);
                            this.sendHelp(sender, page);
                        } catch (NumberFormatException ex) {
                            sender.sendMessage(messageSettings.getMessage(Message.USAGE_HELP));
                        }
                    } else
                        this.sendHelp(sender, 0);
                }
                case "security" -> {
                    if (!this.api.hasPermission(sender, "astralbooks.command.security")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (!this.plugin.getSettings().isBookSignSecurityEnabled()) {
                        sender.sendMessage(messageSettings.getMessage(Message.BOOK_SECURITY_NOT_ENABLED));
                        break;
                    }
                    if (args.length > 1) {
                        switch (args[1]) {
                            case "list" -> {
                                if (!this.api.hasPermission(sender, "astralbooks.command.security.list")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                int page = 1;
                                if (args.length > 2) {
                                    if (args.length > 3)
                                        try {
                                            page = Integer.parseInt(args[3]);
                                        } catch (NumberFormatException ex) {
                                            sender.sendMessage(messageSettings.getMessage(Message.USAGE_SECURITY_LIST));
                                            break;
                                        }
                                    if (page < 1) {
                                        sender.sendMessage(messageSettings.getMessage(Message.USAGE_SECURITY_LIST));
                                        break;
                                    }
                                    if (args[2].equalsIgnoreCase("*"))
                                        this.sendSecurityPage(sender, page);
                                    else {
                                        @SuppressWarnings("deprecation")
                                        OfflinePlayer offlineSelected = Bukkit.getOfflinePlayer(args[2]);
                                        if (offlineSelected.hasPlayedBefore())
                                            this.sendSecurityPage(sender, offlineSelected, page);
                                        else
                                            sender.sendMessage(messageSettings.getMessage(Message.PLAYER_NOT_FOUND));
                                    }
                                } else
                                    this.sendSecurityPage(sender, page);
                            }
                            case "getbook" -> {
                                if (player.isEmpty()) {
                                    sender.sendMessage(messageSettings.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                                    break;
                                }
                                if (!this.api.hasPermission(sender, "astralbooks.command.security.getbook")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (args.length > 2) {
                                    @SuppressWarnings("deprecation")
                                    OfflinePlayer offlineSelected = Bukkit.getOfflinePlayer(args[2]);
                                    if (offlineSelected.hasPlayedBefore()) {
                                        if (args.length > 3)
                                            try {
                                                long timestamp = Long.parseLong(args[3]);
                                                Date date = new Date(timestamp);
                                                ItemStack book = this.plugin.getPluginStorage().getBookSecurity(offlineSelected.getUniqueId(), date);
                                                if (book == null) {
                                                    sender.sendMessage(messageSettings.getMessage(Message.BOOK_SECURITY_NOT_FOUND));
                                                    break;
                                                }
                                                player.get().getInventory().addItem(book);
                                            } catch (NumberFormatException ex) {
                                                sender.sendMessage(messageSettings.getMessage(Message.BOOK_SECURITY_NOT_FOUND));
                                            }
                                        else
                                            sender.sendMessage(messageSettings.getMessage(Message.USAGE_SECURITY_GETBOOK));
                                    } else
                                        sender.sendMessage(messageSettings.getMessage(Message.PLAYER_NOT_FOUND));
                                } else
                                    sender.sendMessage(messageSettings.getMessage(Message.USAGE_SECURITY_GETBOOK));
                            }
                            default -> this.sendSecurityHelp(sender);
                        }
                    } else
                        this.sendSecurityHelp(sender);
                }
                case "import" -> {
                    if (player.isPresent()) {
                        sender.sendMessage(messageSettings.getMessage(Message.PLAYER_CANNOT_USE_COMMAND));
                        break;
                    }
                    if (args.length > 1 && args[1].equals("citizensbooks"))
                        this.api.importFromCitizensBooks();
                    else
                        plugin.getLogger().info("Argument: citizensbooks");
                }
                case "convert" -> {
                    if (player.isPresent()) {
                        sender.sendMessage(messageSettings.getMessage(Message.PLAYER_CANNOT_USE_COMMAND));
                        break;
                    }
                    if (args.length > 1) {
                        StorageType type = switch (args[1]) {
                            case "mysql" -> StorageType.MYSQL;
                            case "sqlite" -> StorageType.SQLITE;
                            case "json" -> StorageType.JSON;
                            case "h2" -> StorageType.H2;
                            case "mariadb" -> StorageType.MARIADB;
                            default -> null;
                        };
                        if (type == null) {
                            plugin.getLogger().info("Argument: mysql, sqlite, json, h2, mariadb");
                            break;
                        }
                        this.plugin.getPluginStorage().convertFrom(type);
                    } else plugin.getLogger().info("Argument: mysql, sqlite, json, h2, mariadb");
                }
                case "interaction" -> {
                    if (!this.api.hasPermission(sender, "astralbooks.command.interaction")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (player.isEmpty()) {
                        sender.sendMessage(messageSettings.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                        break;
                    }
                    if (args.length > 1) {
                        switch (args[1]) {
                            case "set" -> {
                                if (!this.api.hasPermission(sender, "astralbooks.command.interaction.set")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                ItemStack book = this.getItemFromHand(player.get());
                                if (args.length > 2) {
                                    switch (args[2]) {
                                        case "block" -> {
                                            if (!this.api.hasPermission(sender, "astralbooks.command.interaction.set.block")) {
                                                sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                                break;
                                            }
                                            if (args.length > 3) {
                                                if ("right".equalsIgnoreCase(args[3]))
                                                    side = Side.RIGHT;
                                                else if ("left".equalsIgnoreCase(args[3]))
                                                    side = Side.LEFT;
                                                else {
                                                    sender.sendMessage(messageSettings.getMessage(Message.USAGE_INTERACTION_SET_BLOCK));
                                                    break;
                                                }
                                            } else side = Side.RIGHT;
                                            if (!this.hasItemTypeInHand(player.get(), Material.WRITTEN_BOOK)) {
                                                sender.sendMessage(messageSettings.getMessage(Message.NO_WRITTEN_BOOK_IN_HAND));
                                                break;
                                            }
                                            this.plugin.getPlayerActionsListener().setBookBlockOperator(player.get(), book, side);
                                            sender.sendMessage(messageSettings.getMessage(Message.BOOK_APPLY_TO_BLOCK_TIMEOUT));
                                        }
                                        case "entity" -> {
                                            if (!this.api.hasPermission(sender, "astralbooks.command.interaction.set.entity")) {
                                                sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                                break;
                                            }
                                            if (args.length > 3) {
                                                if ("right".equalsIgnoreCase(args[3]))
                                                    side = Side.RIGHT;
                                                else if ("left".equalsIgnoreCase(args[3]))
                                                    side = Side.LEFT;
                                                else {
                                                    sender.sendMessage(messageSettings.getMessage(Message.USAGE_INTERACTION_SET_ENTITY));
                                                    break;
                                                }
                                            } else side = Side.RIGHT;
                                            if (!this.hasItemTypeInHand(player.get(), Material.WRITTEN_BOOK)) {
                                                sender.sendMessage(messageSettings.getMessage(Message.NO_WRITTEN_BOOK_IN_HAND));
                                                break;
                                            }
                                            this.plugin.getPlayerActionsListener().setBookEntityOperator(player.get(), book, side);
                                            sender.sendMessage(messageSettings.getMessage(Message.BOOK_APPLY_TO_ENTITY_TIMEOUT));
                                        }
                                        default -> this.sendInteractionSetHelp(sender);
                                    }
                                } else
                                    this.sendInteractionSetHelp(sender);
                            }
                            case "remove" -> {
                                if (!this.api.hasPermission(sender, "astralbooks.command.interaction.remove")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (args.length > 2) {
                                    switch (args[2]) {
                                        case "block" -> {
                                            if (!this.api.hasPermission(sender, "astralbooks.command.interaction.remove.block")) {
                                                sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                                break;
                                            }
                                            if (args.length > 3) {
                                                if ("right".equalsIgnoreCase(args[3]))
                                                    side = Side.RIGHT;
                                                else if ("left".equalsIgnoreCase(args[3]))
                                                    side = Side.LEFT;
                                                else {
                                                    sender.sendMessage(messageSettings.getMessage(Message.USAGE_INTERACTION_REMOVE_BLOCK));
                                                    break;
                                                }
                                            } else side = Side.RIGHT;
                                            this.plugin.getPlayerActionsListener().setBookBlockOperator(player.get(), null, side);
                                            sender.sendMessage(messageSettings.getMessage(Message.BOOK_REMOVE_FROM_BLOCK_TIMEOUT));
                                        }
                                        case "entity" -> {
                                            if (!this.api.hasPermission(sender, "astralbooks.command.interaction.remove.entity")) {
                                                sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                                break;
                                            }
                                            if (args.length > 3) {
                                                if ("right".equalsIgnoreCase(args[3]))
                                                    side = Side.RIGHT;
                                                else if ("left".equalsIgnoreCase(args[3]))
                                                    side = Side.LEFT;
                                                else {
                                                    sender.sendMessage(messageSettings.getMessage(Message.USAGE_INTERACTION_REMOVE_ENTITY));
                                                    break;
                                                }
                                            } else side = Side.RIGHT;
                                            this.plugin.getPlayerActionsListener().setBookEntityOperator(player.get(), null, side);
                                            sender.sendMessage(messageSettings.getMessage(Message.BOOK_REMOVE_FROM_ENTITY_TIMEOUT));
                                        }
                                        default -> this.sendInteractionRemoveHelp(sender);
                                    }
                                } else
                                    this.sendInteractionRemoveHelp(sender);
                            }
                            default -> this.sendInteractionHelp(sender);
                        }
                    } else
                        this.sendInteractionHelp(sender);

                }
                case "npc" -> {
                    if (!this.api.hasPermission(sender, "astralbooks.command.npc")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (!this.plugin.isCitizensEnabled()) {
                        sender.sendMessage(messageSettings.getMessage(Message.CITIZENS_NOT_ENABLED));
                        break;
                    }
                    if (player.isEmpty()) {
                        sender.sendMessage(messageSettings.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                        break;
                    }
                    Optional<NPC> npc;
                    if (args.length > 1) {
                        switch (args[1]) {
                            case "set" -> {
                                if (!this.api.hasPermission(sender, "astralbooks.command.npc.set")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (!this.hasItemTypeInHand(player.get(), Material.WRITTEN_BOOK)) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_WRITTEN_BOOK_IN_HAND));
                                    break;
                                }
                                npc = Optional.ofNullable(CitizensAPI.getDefaultNPCSelector().getSelected(player.get()));
                                if (npc.isEmpty()) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_NPC_SELECTED));
                                    break;
                                }
                                if (args.length > 2) {
                                    if ("right".equalsIgnoreCase(args[2]))
                                        side = Side.RIGHT;
                                    else if ("left".equalsIgnoreCase(args[2]))
                                        side = Side.LEFT;
                                    else {
                                        sender.sendMessage(messageSettings.getMessage(Message.USAGE_NPC_SET).replace("%npc%", npc.get().getFullName()));
                                        break;
                                    }
                                } else side = Side.RIGHT;
                                if (!this.plugin.getPluginStorage().putNPCBook(npc.get().getId(), side, this.getItemFromHand(player.get()))) {
                                    sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                                    break;
                                }
                                sender.sendMessage(messageSettings.getMessage(Message.SET_BOOK_SUCCESSFULLY).replace("%npc%", npc.get().getFullName()));
                            }
                            case "remove" -> {
                                if (!this.api.hasPermission(sender, "astralbooks.command.npc.remove")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                npc = Optional.ofNullable(CitizensAPI.getDefaultNPCSelector().getSelected(player.get()));
                                if (npc.isEmpty()) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_NPC_SELECTED));
                                    break;
                                }
                                if (args.length > 2) {
                                    if ("right".equalsIgnoreCase(args[2]))
                                        side = Side.RIGHT;
                                    else if ("left".equalsIgnoreCase(args[2]))
                                        side = Side.LEFT;
                                    else {
                                        sender.sendMessage(messageSettings.getMessage(Message.USAGE_NPC_REMOVE).replace("%npc%", npc.get().getFullName()));
                                        break;
                                    }
                                } else side = Side.RIGHT;
                                if (!this.plugin.getPluginStorage().removeNPCBook(npc.get().getId(), side)) {
                                    sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                                    break;
                                }
                                sender.sendMessage(messageSettings.getMessage(Message.REMOVED_BOOK_SUCCESSFULLY).replace("%npc%", npc.get().getFullName()));
                            }
                            case "getbook" -> {
                                if (!this.api.hasPermission(sender, "astralbooks.command.npc.getbook")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                npc = Optional.ofNullable(CitizensAPI.getDefaultNPCSelector().getSelected(player.get()));
                                if (npc.isEmpty()) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_NPC_SELECTED));
                                    break;
                                }
                                if (args.length > 2) {
                                    if ("right".equalsIgnoreCase(args[2]))
                                        side = Side.RIGHT;
                                    else if ("left".equalsIgnoreCase(args[2]))
                                        side = Side.LEFT;
                                    else {
                                        sender.sendMessage(messageSettings.getMessage(Message.USAGE_NPC_GETBOOK).replace("%npc%", npc.get().getFullName()));
                                        break;
                                    }
                                } else
                                    side = Side.RIGHT;
                                if (!this.plugin.getPluginStorage().hasNPCBook(npc.get().getId(), side)) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_BOOK_FOR_NPC).replace("%npc%", npc.get().getFullName()));
                                    break;
                                }
                                ItemStack book = this.plugin.getPluginStorage().getNPCBook(npc.get().getId(), side, new ItemStack(Material.WRITTEN_BOOK));
                                player.get().getInventory().addItem(book);
                                sender.sendMessage(messageSettings.getMessage(Message.BOOK_RECEIVED));
                            }
                            default -> this.sendNpcHelp(sender);
                        }
                    } else
                        this.sendNpcHelp(sender);
                }
                case "actionitem", "ai" -> {
                    if (!this.api.hasPermission(sender, "astralbooks.command.actionitem")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (!this.plugin.isNBTAPIEnabled() || this.api.getDistribution().isNBTAPIRequired()) {
                        sender.sendMessage(messageSettings.getMessage(Message.NBTAPI_NOT_ENABLED));
                        break;
                    }
                    if (player.isEmpty()) {
                        sender.sendMessage(messageSettings.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                        break;
                    }
                    if (args.length > 1) {
                        switch (args[1]) {
                            case "set" -> {
                                if (!this.api.hasPermission(sender, "astralbooks.command.actionitem.set")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (args.length > 2) {
                                    String filter_name = args[2];
                                    if (!this.api.isValidName(filter_name)) {
                                        sender.sendMessage(messageSettings.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter_name%", filter_name));
                                        break;
                                    }
                                    if (!this.plugin.getPluginStorage().hasFilterBook(filter_name)) {
                                        sender.sendMessage(messageSettings.getMessage(Message.FILTER_NOT_FOUND));
                                        break;
                                    }
                                    if (!this.hasItemInHand(player.get())) {
                                        sender.sendMessage(messageSettings.getMessage(Message.NO_ITEM_IN_HAND));
                                        break;
                                    }
                                    if (args.length > 3) {
                                        if ("right".equalsIgnoreCase(args[3]))
                                            action = PersistentKey.ITEM_RIGHT_KEY;
                                        else if ("left".equalsIgnoreCase(args[3]))
                                            action = PersistentKey.ITEM_LEFT_KEY;
                                        else {
                                            sender.sendMessage(messageSettings.getMessage(Message.USAGE_ACTIONITEM_SET).replace("%filter_name%", filter_name));
                                            break;
                                        }
                                    } else action = PersistentKey.ITEM_RIGHT_KEY;
                                    ItemStack item = this.getItemFromHand(player.get());
                                    ItemData data = this.api.itemDataFactory(item);
                                    data.putString(action, filter_name);
                                    this.api.getDistribution().setItemInHand(player.get(), data.build());
                                    sender.sendMessage(messageSettings.getMessage(Message.FILTER_APPLIED_TO_ITEM).replace("%filter_name%", filter_name));

                                } else
                                    sender.sendMessage(messageSettings.getMessage(Message.USAGE_ACTIONITEM_SET));
                            }
                            case "remove" -> {
                                if (!this.api.hasPermission(sender, "astralbooks.command.actionitem.remove")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (!this.hasItemInHand(player.get())) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_ITEM_IN_HAND));
                                    break;
                                }
                                if (args.length > 2) {
                                    if ("right".equalsIgnoreCase(args[2]))
                                        action = PersistentKey.ITEM_RIGHT_KEY;
                                    else if ("left".equalsIgnoreCase(args[2]))
                                        action = PersistentKey.ITEM_LEFT_KEY;
                                    else {
                                        sender.sendMessage(messageSettings.getMessage(Message.USAGE_ACTIONITEM_SET));
                                        break;
                                    }
                                } else action = PersistentKey.ITEM_RIGHT_KEY;
                                ItemStack item = this.getItemFromHand(player.get());
                                ItemData data = this.api.itemDataFactory(item);
                                if (data.hasStringKey(action))
                                    data.removeKey(action);
                                this.api.getDistribution().setItemInHand(player.get(), data.build());
                                sender.sendMessage(messageSettings.getMessage(Message.FILTER_REMOVED_FROM_ITEM));
                            }
                            default -> this.sendActionItemHelp(sender);
                        }
                    } else
                        this.sendActionItemHelp(sender);
                }
                case "forceopen" -> {
                    if (!this.api.hasPermission(sender, "astralbooks.command.forceopen")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (args.length > 1) {
                        String filter_name = args[1];
                        if (!this.api.isValidName(filter_name)) {
                            sender.sendMessage(messageSettings.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter_name%", filter_name));
                            break;
                        }
                        if (!this.plugin.getPluginStorage().hasFilterBook(filter_name)) {
                            sender.sendMessage(messageSettings.getMessage(Message.FILTER_NOT_FOUND));
                            break;
                        }
                        if (args.length == 2) {
                            if (player.isEmpty()) {
                                sender.sendMessage(messageSettings.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                                sender.sendMessage(messageSettings.getMessage(Message.USAGE_FORCEOPEN));
                                break;
                            }
                            if (!this.api.openBook(player.get(), this.api.placeholderHook(player.get(), this.plugin.getPluginStorage().getFilterBook(filter_name), null)))
                                sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                            else
                                sender.sendMessage(messageSettings.getMessage(Message.OPENED_BOOK_FOR_PLAYER)
                                        .replace("%player%", sender.getName()));
                        } else {
                            if ("*".equals(args[2])) {
                                int failedReceiver = 0;
                                int successfulReceiver = 0;
                                for (Player receiver : Bukkit.getOnlinePlayers())
                                    if (!this.api.openBook(receiver, this.api.placeholderHook(receiver, this.plugin.getPluginStorage().getFilterBook(filter_name), null)))
                                        failedReceiver++;
                                    else
                                        successfulReceiver++;
                                sender.sendMessage(messageSettings.getMessage(Message.OPENED_BOOK_FOR_PLAYERS)
                                        .replace("%success%", String.valueOf(successfulReceiver))
                                        .replace("%failed%", String.valueOf(failedReceiver)));
                            } else {
                                Optional<? extends Player> optionalPlayer = Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().equals(args[2])).findFirst();
                                if (optionalPlayer.isPresent())
                                    if (!this.api.openBook(optionalPlayer.get(), this.api.placeholderHook(optionalPlayer.get(), this.plugin.getPluginStorage().getFilterBook(filter_name), null)))
                                        sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                                    else
                                        sender.sendMessage(messageSettings.getMessage(Message.OPENED_BOOK_FOR_PLAYER)
                                                .replace("%player%", optionalPlayer.get().getName()));
                                else
                                    sender.sendMessage(messageSettings.getMessage(Message.PLAYER_NOT_FOUND));
                            }
                        }
                    } else
                        sender.sendMessage(messageSettings.getMessage(Message.USAGE_FORCEOPEN));
                }
                case "about" -> this.sendAbout(sender);
                case "reload" -> {
                    if (!this.api.hasPermission(sender, "astralbooks.command.reload")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    /*
                     * this.plugin.saveSettings();
                     *
                     * No need to be saved anymore!
                     * If config file is edited, the config is
                     * overwritten, so the edit is lost
                     */
                    if (!this.plugin.reloadPlugin()) {
                        sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                        break;
                    }
                    sender.sendMessage(messageSettings.getMessage(Message.CONFIG_RELOADED));
                }
                case "setjoin" -> {
                    if (player.isEmpty()) {
                        sender.sendMessage(messageSettings.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                        break;
                    }
                    if (!this.api.hasPermission(sender, "astralbooks.command.setjoin")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (!this.hasItemTypeInHand(player.get(), Material.WRITTEN_BOOK)) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_WRITTEN_BOOK_IN_HAND));
                        break;
                    }
                    if (!this.plugin.getPluginStorage().setJoinBook(this.getItemFromHand(player.get()))) {
                        sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                        break;
                    }
                    this.plugin.getSettings().setJoinBookEnabled(true);
                    if (!this.plugin.saveSettings()) {
                        sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                        break;
                    }
                    sender.sendMessage(messageSettings.getMessage(Message.SET_JOIN_BOOK_SUCCESSFULLY));
                }
                case "remjoin" -> {
                    if (!this.api.hasPermission(sender, "astralbooks.command.remjoin")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (!this.plugin.getPluginStorage().removeJoinBook()) {
                        sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                        break;
                    }
                    this.plugin.getSettings().setJoinBookEnabled(false);
                    if (!this.plugin.saveSettings()) {
                        sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                        break;
                    }
                    sender.sendMessage(messageSettings.getMessage(Message.REMOVED_JOIN_BOOK_SUCCESSFULLY));
                }
                case "openbook" -> {
                    if (player.isEmpty()) {
                        sender.sendMessage(messageSettings.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                        break;
                    }
                    if (!this.api.hasPermission(sender, "astralbooks.command.getbook")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (!this.hasItemTypeInHand(player.get(), Material.WRITTEN_BOOK)) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_WRITTEN_BOOK_IN_HAND));
                        break;
                    }
                    this.openBook(player.get(), this.getItemFromHand(player.get()));
                }
                case "closebook" -> {
                    if (player.isEmpty()) {
                        sender.sendMessage(messageSettings.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                        break;
                    }
                    if (!this.api.hasPermission(sender, "astralbooks.command.closebook")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (args.length > 2) {
                        Material material = Material.getMaterial("BOOK_AND_QUILL");
                        if (material == null)
                            // 1.13+ (Thanks PikaMug)
                            material = Material.getMaterial("WRITABLE_BOOK");
                        if (!this.hasItemTypeInHand(player.get(), material)) {
                            sender.sendMessage(messageSettings.getMessage(Message.NO_WRITABLE_BOOK_IN_HAND));
                            break;
                        }
                        String author = args[1];
                        String title = args.length > 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : args[2]; // copy if at least 2 title-args
                        this.closeBook(player.get(), this.getItemFromHand(player.get()), author, title);
                    } else
                        sender.sendMessage(messageSettings.getMessage(Message.USAGE_CLOSEBOOK));
                }
                case "setcmd" -> {
                    if (!this.api.hasPermission(sender, "astralbooks.command.setcmd")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (args.length > 2) {
                        String command_name = args[1];
                        if (!this.api.isValidName(command_name)) {
                            sender.sendMessage(messageSettings.getMessage(Message.COMMAND_NAME_INVALID).replace("%invalid_command%", command_name));
                            break;
                        }
                        String filter_name = args[2];
                        if (!this.api.isValidName(filter_name)) {
                            sender.sendMessage(messageSettings.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter%", filter_name));
                            break;
                        }
                        String permission = args.length > 3 ? args[3] : "none";
                        if (!this.api.isValidPermission(permission)) {
                            sender.sendMessage(messageSettings.getMessage(Message.PERMISSION_INVALID).replace("%invalid_permission%", permission));
                            break;
                        }
                        if (!this.plugin.getPluginStorage().putCommandFilter(command_name, filter_name, permission)) {
                            sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                            break;
                        }
                        sender.sendMessage(messageSettings.getMessage(Message.SET_CUSTOM_COMMAND_SUCCESSFULLY).replace("%command%", args[1]).replace("%filter%", filter_name));
                    } else
                        sender.sendMessage(messageSettings.getMessage(Message.USAGE_SETCMD));
                }
                case "remcmd" -> {
                    if (!this.api.hasPermission(sender, "astralbooks.command.remcmd")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (args.length > 1) {
                        String command_name = args[1];
                        if (!this.api.isValidName(command_name)) {
                            sender.sendMessage(messageSettings.getMessage(Message.COMMAND_NAME_INVALID).replace("%invalid_command%", command_name));
                            break;
                        }
                        if (!this.plugin.getPluginStorage().removeCommandFilter(command_name)) {
                            sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                            break;
                        }
                        sender.sendMessage(messageSettings.getMessage(Message.REMOVED_CUSTOM_COMMAND_SUCCESSFULLY).replace("%command%", command_name));
                    } else
                        sender.sendMessage(messageSettings.getMessage(Message.USAGE_REMCMD));
                }
                case "filter" -> {
                    if (!this.api.hasPermission(sender, "astralbooks.command.filter")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (args.length > 1) {
                        switch (args[1]) {
                            case "set" -> {
                                if (player.isEmpty()) {
                                    sender.sendMessage(messageSettings.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                                    break;
                                }
                                if (!this.api.hasPermission(sender, "astralbooks.command.filter.set")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (args.length > 2) {
                                    String filter_name = args[2];
                                    if (!this.api.isValidName(filter_name)) {
                                        sender.sendMessage(messageSettings.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter%", filter_name));
                                        break;
                                    }
                                    if (!this.hasItemTypeInHand(player.get(), Material.WRITTEN_BOOK)) {
                                        sender.sendMessage(messageSettings.getMessage(Message.NO_WRITTEN_BOOK_IN_HAND));
                                        break;
                                    }
                                    if (!this.plugin.getPluginStorage().putFilterBook(filter_name, this.getItemFromHand((Player) sender))) {
                                        sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                                        break;
                                    }
                                    sender.sendMessage(messageSettings.getMessage(Message.FILTER_SAVED).replace("%filter_name%", filter_name));
                                } else
                                    sender.sendMessage(messageSettings.getMessage(Message.USAGE_FILTER_SET));
                            }
                            case "remove" -> {
                                if (!this.api.hasPermission(sender, "astralbooks.command.filter.remove")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (args.length > 2) {
                                    String filter_name = args[2];
                                    if (!this.api.isValidName(filter_name)) {
                                        sender.sendMessage(messageSettings.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter%", filter_name));
                                        break;
                                    }
                                    if (!this.plugin.getPluginStorage().removeFilterBook(filter_name)) {
                                        sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                                        break;
                                    }
                                    sender.sendMessage(messageSettings.getMessage(Message.FILTER_REMOVED).replace("%filter%", filter_name));
                                } else
                                    sender.sendMessage(messageSettings.getMessage(Message.USAGE_FILTER_REMOVE));
                            }
                            case "getbook" -> {
                                if (player.isEmpty()) {
                                    sender.sendMessage(messageSettings.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                                    break;
                                }
                                if (!this.api.hasPermission(sender, "astralbooks.command.filter.getbook")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (args.length > 2) {
                                    String filter_name = args[2];
                                    if (!this.api.isValidName(filter_name)) {
                                        sender.sendMessage(messageSettings.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter%", filter_name));
                                        break;
                                    }
                                    if (!this.plugin.getPluginStorage().hasFilterBook(filter_name)) {
                                        sender.sendMessage(messageSettings.getMessage(Message.NO_BOOK_FOR_FILTER));
                                        break;
                                    }
                                    ItemStack book = this.plugin.getPluginStorage().getFilterBook(filter_name);
                                    player.get().getInventory().addItem(book);
                                    sender.sendMessage(messageSettings.getMessage(Message.BOOK_RECEIVED));
                                } else
                                    sender.sendMessage(messageSettings.getMessage(Message.USAGE_FILTER_GETBOOK));
                            }
                            case "list" -> {
                                if (!this.api.hasPermission(sender, "astralbooks.command.filter.list")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                int pageNum = 1;
                                if (args.length > 2) {
                                    try {
                                        pageNum = Integer.parseInt(args[2]);
                                    } catch (NumberFormatException ex) {
                                        sender.sendMessage(messageSettings.getMessage(Message.USAGE_FILTER_LIST));
                                        break;
                                    }
                                }
                                this.sendFiltersList(sender, pageNum);
                            }
                            default -> this.sendFilterHelp(sender);
                        }
                    } else
                        this.sendFilterHelp(sender);
                }
                default -> {
                    if (this.api.hasPermission(sender, "astralbooks.command.help"))
                        this.sendHelp(sender, 0);
                    else
                        this.sendAbout(sender);
                }
            }
        } else {
            if (this.api.hasPermission(sender, "astralbooks.command.help"))
                this.sendHelp(sender, 0);
            else
                this.sendAbout(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        List<String> completions = new ArrayList<>();
        Set<String> commands = new HashSet<>();

        if (args.length == 1) {
            if (this.api.hasPermission(sender, "astralbooks.command"))
                commands.add("help");
            if (this.api.hasPermission(sender, "astralbooks.command.npc"))
                commands.add("npc");
            if (this.api.hasPermission(sender, "astralbooks.command.setjoin"))
                commands.add("setjoin");
            if (this.api.hasPermission(sender, "astralbooks.command.remjoin"))
                commands.add("remjoin");
            if (this.api.hasPermission(sender, "astralbooks.command.openbook"))
                commands.add("openbook");
            if (this.api.hasPermission(sender, "astralbooks.command.filter"))
                commands.add("filter");
            if (this.api.hasPermission(sender, "astralbooks.command.setcmd"))
                commands.add("setcmd");
            if (this.api.hasPermission(sender, "astralbooks.command.remcmd"))
                commands.add("remcmd");
            if (this.api.hasPermission(sender, "astralbooks.command.forceopen"))
                commands.add("forceopen");
            if (this.api.hasPermission(sender, "astralbooks.command.actionitem"))
                commands.add("actionitem");
            if (this.api.hasPermission(sender, "astralbooks.command.reload"))
                commands.add("reload");
            if (this.api.hasPermission(sender, "astralbooks.command.interaction"))
                commands.add("interaction");
            if (this.api.hasPermission(sender, "astralbooks.command.security"))
                commands.add("security");
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if (args.length == 2) {
            switch (args[0]) {
                case "security" -> {
                    if (this.api.hasPermission(sender, "astralbooks.command.security.list"))
                        commands.add("list");
                    if (this.api.hasPermission(sender, "astralbooks.command.security.getbook"))
                        commands.add("getbook");
                }
                case "interaction" -> {
                    if (this.api.hasPermission(sender, "astralbooks.command.interaction.set"))
                        commands.add("set");
                    if (this.api.hasPermission(sender, "astralbooks.command.interaction.remove"))
                        commands.add("remove");
                }
                case "actionitem", "ai" -> {
                    if (this.api.hasPermission(sender, "astralbooks.command.actionitem.set"))
                        commands.add("set");
                    if (this.api.hasPermission(sender, "astralbooks.command.actionitem.remove"))
                        commands.add("remove");
                }
                case "filter" -> {
                    if (this.api.hasPermission(sender, "astralbooks.command.filter.set"))
                        commands.add("set");
                    if (this.api.hasPermission(sender, "astralbooks.command.filter.remove"))
                        commands.add("remove");
                    if (this.api.hasPermission(sender, "astralbooks.command.filter.getbook"))
                        commands.add("getbook");
                    if (this.api.hasPermission(sender, "astralbooks.command.filter.list"))
                        commands.add("list");
                }
                case "forceopen" -> {
                    if (this.api.hasPermission(sender, "astralbooks.command.forceopen"))
                        commands.addAll(this.plugin.getPluginStorage().getFilterNames());
                }
                case "remcmd" -> {
                    if (this.api.hasPermission(sender, "astralbooks.command.remcmd"))
                        commands.addAll(this.plugin.getPluginStorage().getCommandFilterNames());
                }
                case "npc" -> {
                    if (this.api.hasPermission(sender, "astralbooks.command.npc.set"))
                        commands.add("set");
                    if (this.api.hasPermission(sender, "astralbooks.command.npc.remove"))
                        commands.add("remove");
                    if (this.api.hasPermission(sender, "astralbooks.command.npc.getbook"))
                        commands.add("getbook");
                }
                case "help" -> {
                    if (this.api.hasPermission(sender, "astralbooks.command"))
                        commands.addAll(List.of("1", "2", "3"));
                }
            }
            StringUtil.copyPartialMatches(args[1], commands, completions);
        } else if (args.length == 3) {
            switch (args[0]) {
                case "security" -> {
                    switch (args[1]) {
                        case "list" -> {
                            if (this.api.hasPermission(sender, "astralbooks.command.security.list")) {
                                commands.addAll(this.getPlayers());
                                commands.addAll(this.getOfflinePlayers());
                                commands.add("*");
                            }
                        }
                        case "getbook" -> {
                            if (this.api.hasPermission(sender, "astralbooks.command.security.getbook")) {
                                commands.addAll(this.getPlayers());
                                commands.addAll(this.getOfflinePlayers());
                                commands.add("*");
                            }
                        }
                    }
                }
                case "interaction" -> {
                    switch (args[1]) {
                        case "set" -> {
                            if (this.api.hasPermission(sender, "astralbooks.command.interaction.set.block"))
                                commands.add("block");
                            if (this.api.hasPermission(sender, "astralbooks.command.interaction.set.entity"))
                                commands.add("entity");
                        }
                        case "remove" -> {
                            if (this.api.hasPermission(sender, "astralbooks.command.interaction.remove.block"))
                                commands.add("block");
                            if (this.api.hasPermission(sender, "astralbooks.command.interaction.remove.entity"))
                                commands.add("entity");
                        }
                    }
                }
                case "filter" -> {
                    if (args[1].equals("remove") || args[1].equals("getbook")) {
                        if (this.api.hasPermission(sender, "astralbooks.command.filter.remove")
                                || this.api.hasPermission(sender, "astralbooks.command.filter.getbook")) {
                            commands.addAll(this.plugin.getPluginStorage().getFilterNames());
                        }
                    }
                }
                case "forceopen" -> {
                    if (this.api.hasPermission(sender, "astralbooks.command.forceopen")) {
                        commands.addAll(this.getPlayers());
                        commands.add("*");
                    }
                }
                case "setcmd" -> {
                    if (this.api.hasPermission(sender, "astralbooks.command.setcmd"))
                        commands.addAll(this.plugin.getPluginStorage().getFilterNames());
                }
                case "actionitem", "ai" -> {
                    switch (args[1]) {
                        case "set" -> {
                            if (this.api.hasPermission(sender, "astralbooks.command.actionitem.set"))
                                commands.addAll(this.plugin.getPluginStorage().getFilterNames());
                        }
                        case "remove" -> {
                            if (this.api.hasPermission(sender, "astralbooks.command.actionitem.remove"))
                                commands.addAll(List.of("right", "left"));
                        }
                    }
                }
                case "npc" -> {
                    switch (args[1]) {
                        case "set" -> {
                            if (this.api.hasPermission(sender, "astralbooks.command.npc.set"))
                                commands.addAll(List.of("right", "left"));
                        }
                        case "remove" -> {
                            if (this.api.hasPermission(sender, "astralbooks.command.npc.remove"))
                                commands.addAll(List.of("right", "left"));
                        }
                        case "getbook" -> {
                            if (this.api.hasPermission(sender, "astralbooks.command.npc.getbook"))
                                commands.addAll(List.of("right", "left"));
                        }
                    }
                }
            }
            StringUtil.copyPartialMatches(args[2], commands, completions);
        } else if (args.length == 4) {
            switch (args[0]) {
                case "security" -> {
                    if (this.api.hasPermission(sender, "astralbooks.command.security.getbook") && "getbook".equalsIgnoreCase(args[1])) {
                        // this is stupid!
                        @SuppressWarnings("deprecation")
                        OfflinePlayer player = Bukkit.getOfflinePlayer(args[2]);
                        if (player.hasPlayedBefore())
                            for (Date date : this.plugin.getPluginStorage().getCache().playerTimestamps.getUnchecked(player.getUniqueId()))
                                commands.add(String.valueOf(date.getTime()));
                    }
                }
                case "actionitem", "ai" -> {
                    if (this.api.hasPermission(sender, "astralbooks.command.actionitem.set") && "set".equalsIgnoreCase(args[1]))
                        commands.addAll(List.of("right", "left"));
                }
                case "interaction" -> {
                    switch (args[1]) {
                        case "set" -> {
                            switch (args[2]) {
                                case "block" -> {
                                    if (this.api.hasPermission(sender, "astralbooks.command.interaction.set.block"))
                                        commands.addAll(List.of("right", "left"));
                                }
                                case "entity" -> {
                                    if (this.api.hasPermission(sender, "astralbooks.command.interaction.set.entity"))
                                        commands.addAll(List.of("right", "left"));
                                }
                            }
                        }
                        case "remove" -> {
                            switch (args[2]) {
                                case "block" -> {
                                    if (this.api.hasPermission(sender, "astralbooks.command.interaction.remove.block"))
                                        commands.addAll(List.of("right", "left"));
                                }
                                case "entity" -> {
                                    if (this.api.hasPermission(sender, "astralbooks.command.interaction.remove.entity"))
                                        commands.addAll(List.of("right", "left"));
                                }
                            }
                        }
                    }
                }
            }
            StringUtil.copyPartialMatches(args[3], commands, completions);
        }
        Collections.sort(completions);
        return completions;
    }

    private Set<String> getPlayers() {
        return Bukkit.getOnlinePlayers()
                .stream()
                .map(Player::getName)
                .collect(Collectors.toSet());
    }

    private Set<String> getOfflinePlayers() {
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .collect(Collectors.toSet());
    }

    private boolean hasItemTypeInHand(Player player, Material type) {
        ItemStack item = this.api.getDistribution().getItemInHand(player);
        if (item == null)
            return false;
        return item.getType() == type;
    }

    private boolean hasItemInHand(Player player) {
        ItemStack item = this.api.getDistribution().getItemInHand(player);
        if (item == null)
            return false;
        return item.getType() != Material.AIR;
    }

    private boolean isPlayer(CommandSender sender) {
        return (sender instanceof Player);
    }

    private ItemStack getItemFromHand(Player player) {
        return this.api.getDistribution().getItemInHand(player);
    }

    private void openBook(Player player, ItemStack book) {
        BookMeta meta = (BookMeta) book.getItemMeta();
        Material material = Material.getMaterial("BOOK_AND_QUILL");
        if (material == null)
            // 1.13+
            material = Material.getMaterial("WRITABLE_BOOK");
        if (material == null)
            throw new UnsupportedOperationException("Something went wrong with Bukkit Material!");
        ItemStack item = new ItemStack(material, book.getAmount());
        item.setItemMeta(meta);
        this.api.getDistribution().setItemInHand(player, item);
    }

    private void closeBook(Player player, ItemStack book, String author, String title) {
        BookMeta meta = (BookMeta) book.getItemMeta();
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK, book.getAmount());
        if (meta != null) {
            meta.setAuthor(ChatColor.translateAlternateColorCodes('&', author));
            meta.setTitle(ChatColor.translateAlternateColorCodes('&', title));
            item.setItemMeta(meta);
        }
        this.api.getDistribution().setItemInHand(player, item);
    }

    private void sendAbout(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.RED + "<+ AstralBooks +>");
        sender.sendMessage(ChatColor.GOLD + "Version: " + ChatColor.RED + this.plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GOLD + "Auhtor: " + ChatColor.RED + "NicoNekoDev");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
    }

    private void sendHelp(CommandSender sender, int page) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        if (page < 1 || page > 4) page = 1;
        sender.sendMessage("");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INFO).replace("%page%", String.valueOf(page)));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ARGUMENTS));
        sender.sendMessage("");
        if (page == 2) {
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_FILTER_SET).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_FILTER_REMOVE).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_FILTER_GETBOOK).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_FILTER_LIST).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ACTIONITEM_SET).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ACTIONITEM_REMOVE).split("\\$"));
        } else if (page == 3) {
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_OPENBOOK).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_CLOSEBOOK).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_SETJOIN).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_REMJOIN).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_SETCMD).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_REMCMD).split("\\$"));
        } else if (page == 4) {
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_FORCEOPEN).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_HELP).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_RELOAD).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_NPC_SET).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_NPC_REMOVE).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_NPC_GETBOOK).split("\\$"));
        } else {
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_SET_BLOCK).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_SET_ENTITY).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_REMOVE_BLOCK).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_REMOVE_ENTITY).split("\\$"));
            sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ABOUT).split("\\$"));
        }
        sender.sendMessage("");
    }

    private void sendFilterHelp(CommandSender sender) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ARGUMENTS));
        sender.sendMessage("");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_FILTER_SET).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_FILTER_REMOVE).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_FILTER_GETBOOK).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_FILTER_LIST).split("\\$"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
    }

    private void sendActionItemHelp(CommandSender sender) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ARGUMENTS));
        sender.sendMessage("");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ACTIONITEM_SET).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ACTIONITEM_REMOVE).split("\\$"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
    }

    private void sendNpcHelp(CommandSender sender) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ARGUMENTS));
        sender.sendMessage("");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_NPC_SET).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_NPC_REMOVE).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_NPC_GETBOOK).split("\\$"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
    }

    private void sendInteractionSetHelp(CommandSender sender) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ARGUMENTS));
        sender.sendMessage("");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_SET_BLOCK).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_SET_ENTITY).split("\\$"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
    }

    private void sendInteractionRemoveHelp(CommandSender sender) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ARGUMENTS));
        sender.sendMessage("");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_REMOVE_BLOCK).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_REMOVE_ENTITY).split("\\$"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
    }

    private void sendInteractionHelp(CommandSender sender) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ARGUMENTS));
        sender.sendMessage("");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_SET_BLOCK).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_SET_ENTITY).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_REMOVE_BLOCK).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_REMOVE_ENTITY).split("\\$"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
    }


    private void sendSecurityHelp(CommandSender sender) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ARGUMENTS));
        sender.sendMessage("");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_SECURITY_LIST).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_SECURITY_GETBOOK).split("\\$"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
    }

    private void sendSecurityPage(CommandSender sender, OfflinePlayer player, int page) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        SimpleDateFormat dateFormat;
        try {
            dateFormat = new SimpleDateFormat(messageSettings.getMessageNoHeader(Message.BOOK_SECURITY_DATE_FORMAT));
        } catch (Exception ex) {
            this.plugin.getLogger().warning("The date format for \"book_security_date_format\" is not correctly set. Please check the settings!");
            dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        }
        LinkedList<PairTuple<Date, ItemStack>> securityBooks = this.plugin.getPluginStorage().getAllBookSecurity(player.getUniqueId(), page - 1, 10);
        if (securityBooks.isEmpty()) {
            sender.sendMessage(messageSettings.getMessage(Message.BOOK_SECURITY_NOT_FOUND));
            return;
        }
        sender.sendMessage(messageSettings.getMessage(Message.BOOK_SECURITY_LIST_PRESENT).replace("%page%", String.valueOf(page)));
        int count = 0;
        for (PairTuple<Date, ItemStack> securityBook : securityBooks) {
            ItemStack book = securityBook.secondValue();
            String title = "<no title>";
            if (book.hasItemMeta() && book.getItemMeta() instanceof BookMeta bookMeta && bookMeta.hasTitle())
                title = bookMeta.getTitle();
            sender.sendMessage(messageSettings.parseMessage(
                    "&c&l  " + (((page - 1) * 10) + count + 1) +
                            ") &f" + player.getName() +
                            " &e- &f" + dateFormat.format(securityBook.firstValue()) +
                            " &c(&b" + securityBook.firstValue().getTime() + "&c) &e- &f" +
                            title
            ));
            count++;
        }
    }

    private void sendSecurityPage(CommandSender sender, int page) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        SimpleDateFormat dateFormat;
        try {
            dateFormat = new SimpleDateFormat(messageSettings.getMessageNoHeader(Message.BOOK_SECURITY_DATE_FORMAT));
        } catch (Exception ex) {
            this.plugin.getLogger().warning("The date format for \"book_security_date_format\" is not correctly set. Please check the settings!");
            dateFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss");
        }
        LinkedList<TripletTuple<UUID, Date, ItemStack>> securityBooks = this.plugin.getPluginStorage().getAllBookSecurity(page - 1, 10);
        if (securityBooks.isEmpty()) {
            sender.sendMessage(messageSettings.getMessage(Message.BOOK_SECURITY_NOT_FOUND).replace("%page%", String.valueOf(page)));
            return;
        }
        sender.sendMessage(messageSettings.getMessage(Message.BOOK_SECURITY_LIST_PRESENT));
        int count = 0;
        for (TripletTuple<UUID, Date, ItemStack> securityBook : securityBooks) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(securityBook.firstValue());
            ItemStack book = securityBook.thirdValue();
            String title = "<no title>";
            if (book.hasItemMeta() && book.getItemMeta() instanceof BookMeta bookMeta && bookMeta.hasTitle())
                title = bookMeta.getTitle();
            sender.sendMessage(messageSettings.parseMessage(
                    "&c&l  " + (((page - 1) * 10) + count + 1) +
                            ") &f" + player.getName() +
                            " &e- &f" + dateFormat.format(securityBook.secondValue()) +
                            " &c(&b" + securityBook.secondValue().getTime() + "&c) &e- &f" +
                            title
            ));
            count++;
        }
    }

    private void sendFiltersList(CommandSender sender, int pageNum) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        LinkedList<String[]> pages = this.getSplitList(this.plugin.getPluginStorage().getFilterNames());
        if (pages.isEmpty()) {
            sender.sendMessage(messageSettings.getMessage(Message.FILTERS_LIST_NO_FILTER_PRESENT));
            return;
        }
        if (pages.size() < pageNum || pageNum < 1) {
            sender.sendMessage(messageSettings.getMessage(Message.FILTERS_LIST_PAGE_NOT_FOUND).replace("%page%", String.valueOf(pageNum)));
            return;
        }
        String[] page = pages.get(pageNum - 1);
        sender.sendMessage(messageSettings.getMessage(Message.FILTERS_LIST_PRESENT));
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
        for (int i = 0; i < 10; i++) {
            String row = page[i];
            if (row == null) continue;
            sender.sendMessage(messageSettings.parseMessage("&c&l  " + (((pageNum - 1) * 10) + i + 1) + ") &a" + row));
        }
        sender.sendMessage(ChatColor.GRAY + String.valueOf(ChatColor.STRIKETHROUGH) + "+----------------------------------+");
    }

    private LinkedList<String[]> getSplitList(Collection<String> list) {
        LinkedList<String[]> resultedFilters = new LinkedList<>();
        LinkedList<String> sortedFilters = new LinkedList<>(list);
        String pooledResult;
        while ((pooledResult = sortedFilters.poll()) != null) {
            String[] page = new String[10];
            page[0] = pooledResult;
            for (int n = 1; n < 10; n++) {
                page[n] = sortedFilters.poll();
            }
            resultedFilters.add(page);
        }
        return resultedFilters;
    }
}