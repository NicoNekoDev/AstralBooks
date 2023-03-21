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

package ro.nicuch.astralbooks;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import ro.nicuch.astralbooks.persistent.item.ItemData;
import ro.nicuch.astralbooks.settings.MessageSettings;
import ro.nicuch.astralbooks.utils.Message;
import ro.nicuch.astralbooks.utils.PersistentKey;
import ro.nicuch.astralbooks.utils.SettingsUtil;
import ro.nicuch.astralbooks.utils.Side;

import java.util.*;

public class CitizensBooksCommand implements TabExecutor {
    private final CitizensBooksPlugin plugin;
    private final CitizensBooksAPI api;

    public CitizensBooksCommand(CitizensBooksPlugin plugin) {
        this.api = (this.plugin = plugin).getAPI();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Optional<Player> player = this.isPlayer(sender) ? Optional.of((Player) sender) : Optional.empty();
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        if (args.length > 0) {
            switch (args[0]) {
                case "help" -> {
                    if (!this.api.hasPermission(sender, "npcbook.command.help")) {
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
                case "interaction" -> {
                    if (!this.api.hasPermission(sender, "npcbook.command.interaction")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (player.isEmpty()) {
                        sender.sendMessage(messageSettings.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                        break;
                    }
                    Side side = Side.RIGHT;
                    if (args.length > 1) {
                        switch (args[1]) {
                            case "set" -> {
                                if (!this.api.hasPermission(sender, "npcbook.command.interaction.set")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                ItemStack book = this.getItemFromHand(player.get());
                                if (args.length > 2) {
                                    switch (args[2]) {
                                        case "block" -> {
                                            if (!this.api.hasPermission(sender, "npcbook.command.interaction.set.block")) {
                                                sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                                break;
                                            }
                                            if (args.length > 3) {
                                                if ("left".equalsIgnoreCase(args[3]))
                                                    side = Side.LEFT;
                                                else {
                                                    sender.sendMessage(messageSettings.getMessage(Message.USAGE_INTERACTION_SET_BLOCK));
                                                    break;
                                                }
                                            }
                                            if (!this.hasItemTypeInHand(player.get(), Material.WRITTEN_BOOK)) {
                                                sender.sendMessage(messageSettings.getMessage(Message.NO_WRITTEN_BOOK_IN_HAND));
                                                break;
                                            }
                                            this.plugin.getPlayerActionsListener().setBookBlockOperator(player.get(), book, side);
                                            sender.sendMessage(messageSettings.getMessage(Message.BOOK_APPLY_TO_BLOCK_TIMEOUT));
                                        }
                                        case "entity" -> {
                                            if (!this.api.hasPermission(sender, "npcbook.command.interaction.set.entity")) {
                                                sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                                break;
                                            }
                                            if (args.length > 3) {
                                                if ("left".equalsIgnoreCase(args[3]))
                                                    side = Side.LEFT;
                                                else {
                                                    sender.sendMessage(messageSettings.getMessage(Message.USAGE_INTERACTION_SET_ENTITY));
                                                    break;
                                                }
                                            }
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
                                if (!this.api.hasPermission(sender, "npcbook.command.interaction.remove")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (args.length > 2) {
                                    switch (args[2]) {
                                        case "block" -> {
                                            if (!this.api.hasPermission(sender, "npcbook.command.interaction.remove.block")) {
                                                sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                                break;
                                            }
                                            if (args.length > 3) {
                                                if ("left".equalsIgnoreCase(args[3]))
                                                    side = Side.LEFT;
                                                else {
                                                    sender.sendMessage(messageSettings.getMessage(Message.USAGE_INTERACTION_REMOVE_BLOCK));
                                                    break;
                                                }
                                            }
                                            this.plugin.getPlayerActionsListener().setBookBlockOperator(player.get(), null, side);
                                            sender.sendMessage(messageSettings.getMessage(Message.BOOK_REMOVE_FROM_BLOCK_TIMEOUT));
                                        }
                                        case "entity" -> {
                                            if (!this.api.hasPermission(sender, "npcbook.command.interaction.remove.entity")) {
                                                sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                                break;
                                            }
                                            if (args.length > 3) {
                                                if ("left".equalsIgnoreCase(args[3]))
                                                    side = Side.LEFT;
                                                else {
                                                    sender.sendMessage(messageSettings.getMessage(Message.USAGE_INTERACTION_REMOVE_ENTITY));
                                                    break;
                                                }
                                            }
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
                    if (!this.api.hasPermission(sender, "npcbook.command.npc")) {
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
                    String side = "right_side";
                    if (args.length > 1) {
                        switch (args[1]) {
                            case "set" -> {
                                if (!this.api.hasPermission(sender, "npcbook.command.npc.set")) {
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
                                        side = "right_side";
                                    else if ("left".equalsIgnoreCase(args[2]))
                                        side = "left_side";
                                    else {
                                        sender.sendMessage(messageSettings.getMessage(Message.USAGE_NPC_SET).replace("%npc%", npc.get().getFullName()));
                                        break;
                                    }
                                }
                                if (!this.plugin.getStorage().putNPCBook(npc.get().getId(), Side.fromString(side), this.getItemFromHand(player.get()))) {
                                    sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                                    break;
                                }
                                sender.sendMessage(messageSettings.getMessage(Message.SET_BOOK_SUCCESSFULLY).replace("%npc%", npc.get().getFullName()));
                            }
                            case "remove" -> {
                                if (!this.api.hasPermission(sender, "npcbook.command.npc.remove")) {
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
                                        side = "right_side";
                                    else if ("left".equalsIgnoreCase(args[2]))
                                        side = "left_side";
                                    else {
                                        sender.sendMessage(messageSettings.getMessage(Message.USAGE_NPC_REMOVE).replace("%npc%", npc.get().getFullName()));
                                        break;
                                    }
                                }
                                if (!this.plugin.getStorage().removeNPCBook(npc.get().getId(), Side.fromString(side))) {
                                    sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                                    break;
                                }
                                sender.sendMessage(messageSettings.getMessage(Message.REMOVED_BOOK_SUCCESSFULLY).replace("%npc%", npc.get().getFullName()));
                            }
                            case "getbook" -> {
                                if (!this.api.hasPermission(sender, "npcbook.command.npc.getbook")) {
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
                                        side = "right_side";
                                    else if ("left".equalsIgnoreCase(args[2]))
                                        side = "left_side";
                                    else {
                                        sender.sendMessage(messageSettings.getMessage(Message.USAGE_NPC_GETBOOK).replace("%npc%", npc.get().getFullName()));
                                        break;
                                    }
                                }
                                if (!this.plugin.getStorage().hasNPCBook(npc.get().getId(), Side.valueOf(side))) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_BOOK_FOR_NPC).replace("%npc%", npc.get().getFullName()));
                                    break;
                                }
                                ItemStack book = this.plugin.getStorage().getNPCBook(npc.get().getId(), Side.valueOf(side), new ItemStack(Material.WRITTEN_BOOK));
                                player.get().getInventory().addItem(book);
                                sender.sendMessage(messageSettings.getMessage(Message.BOOK_RECEIVED));
                            }
                            default -> this.sendNpcHelp(sender);
                        }
                    } else
                        this.sendNpcHelp(sender);
                }
                case "actionitem", "ai" -> {
                    if (!this.api.hasPermission(sender, "npcbook.command.actionitem")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (!(this.plugin.isNBTAPIEnabled() || this.api.noNBTAPIRequired())) {
                        sender.sendMessage(messageSettings.getMessage(Message.NBTAPI_NOT_ENABLED));
                        break;
                    }
                    if (player.isEmpty()) {
                        sender.sendMessage(messageSettings.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                        break;
                    }
                    PersistentKey action = null;
                    if (args.length > 1) {
                        switch (args[1]) {
                            case "set" -> {
                                if (!this.api.hasPermission(sender, "npcbook.command.actionitem.set")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (args.length > 2) {
                                    String filter_name = args[2];
                                    if (!this.api.isValidName(filter_name)) {
                                        sender.sendMessage(messageSettings.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter_name%", filter_name));
                                        break;
                                    }
                                    if (!this.plugin.getStorage().hasFilterBook(filter_name)) {
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
                                    }
                                    ItemStack item = this.getItemFromHand(player.get());
                                    ItemData data = this.api.itemDataFactory(item);
                                    data.putString(action, filter_name);
                                    this.api.getDistribution().setItemInHand(player.get(), data.build());
                                    sender.sendMessage(messageSettings.getMessage(Message.FILTER_APPLIED_TO_ITEM).replace("%filter_name%", filter_name));

                                } else
                                    sender.sendMessage(messageSettings.getMessage(Message.USAGE_ACTIONITEM_SET));
                            }
                            case "remove" -> {
                                if (!this.api.hasPermission(sender, "npcbook.command.actionitem.remove")) {
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
                                }
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
                    if (!this.api.hasPermission(sender, "npcbook.command.forceopen")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (args.length > 1) {
                        String filter_name = args[1];
                        if (!this.api.isValidName(filter_name)) {
                            sender.sendMessage(messageSettings.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter_name%", filter_name));
                            break;
                        }
                        if (!this.plugin.getStorage().hasFilterBook(filter_name)) {
                            sender.sendMessage(messageSettings.getMessage(Message.FILTER_NOT_FOUND));
                            break;
                        }
                        if (args.length == 2) {
                            if (player.isEmpty()) {
                                sender.sendMessage(messageSettings.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                                sender.sendMessage(messageSettings.getMessage(Message.USAGE_FORCEOPEN));
                                break;
                            }
                            if (!this.api.openBook(player.get(), this.api.placeholderHook(player.get(), this.plugin.getStorage().getFilterBook(filter_name), null)))
                                sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                            else
                                sender.sendMessage(messageSettings.getMessage(Message.OPENED_BOOK_FOR_PLAYER)
                                        .replace("%player%", sender.getName()));
                        } else {
                            if ("*".equals(args[2]) || "@a".equals(args[2])) {
                                int failedReceiver = 0;
                                int successfulReceiver = 0;
                                for (Player receiver : Bukkit.getOnlinePlayers())
                                    if (!this.api.openBook(receiver, this.api.placeholderHook(receiver, this.plugin.getStorage().getFilterBook(filter_name), null)))
                                        failedReceiver++;
                                    else
                                        successfulReceiver++;
                                sender.sendMessage(messageSettings.getMessage(Message.OPENED_BOOK_FOR_PLAYERS)
                                        .replace("%success%", successfulReceiver + "")
                                        .replace("%failed%", failedReceiver + ""));
                            } else {
                                Optional<? extends Player> optionalPlayer = Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().equals(args[2])).findFirst();
                                if (optionalPlayer.isPresent())
                                    if (!this.api.openBook(optionalPlayer.get(), this.api.placeholderHook(optionalPlayer.get(), this.plugin.getStorage().getFilterBook(filter_name), null)))
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
                    if (!this.api.hasPermission(sender, "npcbook.command.reload")) {
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
                    if (!this.api.hasPermission(sender, "npcbook.command.setjoin")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (!this.hasItemTypeInHand(player.get(), Material.WRITTEN_BOOK)) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_WRITTEN_BOOK_IN_HAND));
                        break;
                    }
                    this.api.setJoinBook(this.getItemFromHand(player.get()));
                    this.plugin.getSettings().setJoinBookLastChange(System.currentTimeMillis());
                    sender.sendMessage(messageSettings.getMessage(Message.SET_JOIN_BOOK_SUCCESSFULLY));
                }
                case "remjoin" -> {
                    if (!this.api.hasPermission(sender, "npcbook.command.remjoin")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (!this.api.removeJoinBook()) {
                        sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                        break;
                    }
                    this.plugin.getSettings().setJoinBookLastChange(0);
                    sender.sendMessage(messageSettings.getMessage(Message.REMOVED_JOIN_BOOK_SUCCESSFULLY));
                }
                case "openbook" -> {
                    if (player.isEmpty()) {
                        sender.sendMessage(messageSettings.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                        break;
                    }
                    if (!this.api.hasPermission(sender, "npcbook.command.getbook")) {
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
                    if (!this.api.hasPermission(sender, "npcbook.command.closebook")) {
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
                    if (!this.api.hasPermission(sender, "npcbook.command.setcmd")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (args.length > 2) {
                        String command_name = args[1];
                        if (!this.api.isValidName(command_name)) {
                            sender.sendMessage(messageSettings.getMessage(Message.COMMAND_NAME_INVALID).replace("%invalid_command_name%", command_name));
                            break;
                        }
                        String filter_name = args[2];
                        if (!this.api.isValidName(filter_name)) {
                            sender.sendMessage(messageSettings.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter_name%", filter_name));
                            break;
                        }
                        if (!this.plugin.getStorage().putCommandFilter(command_name, filter_name, args.length > 3 ? args[3] : "none")) {
                            sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                            break;
                        }
                        sender.sendMessage(messageSettings.getMessage(Message.SET_CUSTOM_COMMAND_SUCCESSFULLY).replace("%command_name%", args[1]).replace("%filter_name%", filter_name));
                    } else
                        sender.sendMessage(messageSettings.getMessage(Message.USAGE_SETCMD));
                }
                case "remcmd" -> {
                    if (!this.api.hasPermission(sender, "npcbook.command.remcmd")) {
                        sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (args.length > 1) {
                        String command_name = args[1];
                        if (!this.api.isValidName(command_name)) {
                            sender.sendMessage(messageSettings.getMessage(Message.COMMAND_NAME_INVALID).replace("%invalid_command_name%", command_name));
                            break;
                        }
                        if (!this.plugin.getStorage().removeCommandFilter(command_name)) {
                            sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                            break;
                        }
                        sender.sendMessage(messageSettings.getMessage(Message.REMOVED_CUSTOM_COMMAND_SUCCESSFULLY).replace("%command%", command_name));
                    } else
                        sender.sendMessage(messageSettings.getMessage(Message.USAGE_REMCMD));
                }
                case "filter" -> {
                    if (!this.api.hasPermission(sender, "npcbook.command.filter")) {
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
                                if (!this.api.hasPermission(sender, "npcbook.command.filter.set")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (args.length > 2) {
                                    String filter_name = args[2];
                                    if (!this.api.isValidName(filter_name)) {
                                        sender.sendMessage(messageSettings.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter_name%", filter_name));
                                        break;
                                    }
                                    if (!this.hasItemTypeInHand(player.get(), Material.WRITTEN_BOOK)) {
                                        sender.sendMessage(messageSettings.getMessage(Message.NO_WRITTEN_BOOK_IN_HAND));
                                        break;
                                    }
                                    if (!this.plugin.getStorage().putFilterBook(filter_name, this.getItemFromHand((Player) sender))) {
                                        sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                                        break;
                                    }
                                    sender.sendMessage(messageSettings.getMessage(Message.FILTER_SAVED).replace("%filter_name%", filter_name));
                                } else
                                    sender.sendMessage(messageSettings.getMessage(Message.USAGE_FILTER_SET));
                            }
                            case "remove" -> {
                                if (!this.api.hasPermission(sender, "npcbook.command.filter.remove")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (args.length > 2) {
                                    String filter_name = args[2];
                                    if (!this.api.isValidName(filter_name)) {
                                        sender.sendMessage(messageSettings.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter_name%", filter_name));
                                        break;
                                    }
                                    if (!this.plugin.getStorage().removeFilterBook(filter_name)) {
                                        sender.sendMessage(messageSettings.getMessage(Message.OPERATION_FAILED));
                                        break;
                                    }
                                    sender.sendMessage(messageSettings.getMessage(Message.FILTER_REMOVED).replace("%filter_name%", filter_name));
                                } else
                                    sender.sendMessage(messageSettings.getMessage(Message.USAGE_FILTER_REMOVE));
                            }
                            case "getbook" -> {
                                if (player.isEmpty()) {
                                    sender.sendMessage(messageSettings.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                                    break;
                                }
                                if (!this.api.hasPermission(sender, "npcbook.command.filter.getbook")) {
                                    sender.sendMessage(messageSettings.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (args.length > 2) {
                                    String filter_name = args[2];
                                    if (!this.api.isValidName(filter_name)) {
                                        sender.sendMessage(messageSettings.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter_name%", filter_name));
                                        break;
                                    }
                                    if (!this.plugin.getStorage().hasFilterBook(filter_name)) {
                                        sender.sendMessage(messageSettings.getMessage(Message.NO_BOOK_FOR_FILTER));
                                        break;
                                    }
                                    ItemStack book = this.plugin.getStorage().getFilterBook(filter_name);
                                    player.get().getInventory().addItem(book);
                                    sender.sendMessage(messageSettings.getMessage(Message.BOOK_RECEIVED));
                                } else
                                    sender.sendMessage(messageSettings.getMessage(Message.USAGE_FILTER_GETBOOK));
                            }
                            case "list" -> {
                                if (!this.api.hasPermission(sender, "npcbook.command.filter.list")) {
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
                    if (this.api.hasPermission(sender, "npcbook.command.help"))
                        this.sendHelp(sender, 0);
                    else
                        this.sendAbout(sender);
                }
            }
        } else {
            if (this.api.hasPermission(sender, "npcbook.command.help"))
                this.sendHelp(sender, 0);
            else
                this.sendAbout(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>();

        if (args.length == 1) {
            if (this.api.hasPermission(sender, "npcbook.command"))
                commands.add("help");
            if (this.api.hasPermission(sender, "npcbook.command.npc"))
                commands.add("npc");
            if (this.api.hasPermission(sender, "npcbook.command.setjoin"))
                commands.add("setjoin");
            if (this.api.hasPermission(sender, "npcbook.command.remjoin"))
                commands.add("remjoin");
            if (this.api.hasPermission(sender, "npcbook.command.openbook"))
                commands.add("openbook");
            if (this.api.hasPermission(sender, "npcbook.command.filter"))
                commands.add("filter");
            if (this.api.hasPermission(sender, "npcbook.command.setcmd"))
                commands.add("setcmd");
            if (this.api.hasPermission(sender, "npcbook.command.remcmd"))
                commands.add("remcmd");
            if (this.api.hasPermission(sender, "npcbook.command.forceopen"))
                commands.add("forceopen");
            if (this.api.hasPermission(sender, "npcbook.command.actionitem"))
                commands.add("actionitem");
            if (this.api.hasPermission(sender, "npcbook.command.reload"))
                commands.add("reload");
            if (this.api.hasPermission(sender, "npcbook.command.interaction"))
                commands.add("interaction");
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if (args.length == 2) {
            switch (args[0]) {
                case "interaction":
                    if (this.api.hasPermission(sender, "npcbook.command.interaction.set"))
                        commands.add("set");
                    if (this.api.hasPermission(sender, "npcbook.command.interaction.remove"))
                        commands.add("remove");
                    break;
                case "actionitem":
                case "ai":
                    if (this.api.hasPermission(sender, "npcbook.command.actionitem.set"))
                        commands.add("set");
                    if (this.api.hasPermission(sender, "npcbook.command.actionitem.remove"))
                        commands.add("remove");
                    break;
                case "filter":
                    if (this.api.hasPermission(sender, "npcbook.command.filter.set"))
                        commands.add("set");
                    if (this.api.hasPermission(sender, "npcbook.command.filter.remove"))
                        commands.add("remove");
                    if (this.api.hasPermission(sender, "npcbook.command.filter.getbook"))
                        commands.add("getbook");
                    if (this.api.hasPermission(sender, "npcbook.command.filter.list"))
                        commands.add("list");
                    break;
                case "forceopen":
                    if (this.api.hasPermission(sender, "npcbook.command.forceopen"))
                        commands.addAll(this.plugin.getStorage().getFilterNames());
                    break;
                case "remcmd":
                    if (this.api.hasPermission(sender, "npcbook.command.remcmd"))
                        commands.addAll(this.plugin.getStorage().getCommandFilterNames());
                    break;
                case "npc":
                    if (this.api.hasPermission(sender, "npcbook.command.npc.set"))
                        commands.add("set");
                    if (this.api.hasPermission(sender, "npcbook.command.npc.remove"))
                        commands.add("remove");
                    if (this.api.hasPermission(sender, "npcbook.command.npc.getbook"))
                        commands.add("getbook");
                    break;
                case "help":
                    if (this.api.hasPermission(sender, "npcbook.command"))
                        commands.addAll(List.of("1", "2", "3"));
                default:
                    break;
            }
            StringUtil.copyPartialMatches(args[1], commands, completions);
        } else if (args.length == 3) {
            switch (args[0]) {
                case "interaction":
                    switch (args[1]) {
                        case "set" -> {
                            if (this.api.hasPermission(sender, "npcbook.command.interaction.set.block"))
                                commands.add("block");
                            if (this.api.hasPermission(sender, "npcbook.command.interaction.set.entity"))
                                commands.add("entity");
                        }
                        case "remove" -> {
                            if (this.api.hasPermission(sender, "npcbook.command.interaction.remove.block"))
                                commands.add("block");
                            if (this.api.hasPermission(sender, "npcbook.command.interaction.remove.entity"))
                                commands.add("entity");
                        }
                    }
                case "filter":
                    if (args[1].equals("remove") || args[1].equals("getbook")) {
                        if (this.api.hasPermission(sender, "npcbook.command.filter.remove")
                                || this.api.hasPermission(sender, "npcbook.command.filter.getbook")) {
                            commands.addAll(this.plugin.getStorage().getFilterNames());
                        }
                    }
                    break;
                case "forceopen":
                    if (this.api.hasPermission(sender, "npcbook.command.forceopen")) {
                        commands.add("@a");
                        commands.addAll(this.api.getPlayers());
                    }
                    break;
                case "setcmd":
                    if (this.api.hasPermission(sender, "npcbook.command.setcmd"))
                        commands.addAll(this.plugin.getStorage().getFilterNames());
                    break;
                case "actionitem":
                case "ai":
                    switch (args[1]) {
                        case "set" -> {
                            if (this.api.hasPermission(sender, "npcbook.command.actionitem.set"))
                                commands.addAll(this.plugin.getStorage().getFilterNames());
                        }
                        case "remove" -> {
                            if (this.api.hasPermission(sender, "npcbook.command.actionitem.remove"))
                                commands.addAll(List.of("right", "left"));
                        }
                    }
                case "npc":
                    switch (args[1]) {
                        case "set" -> {
                            if (this.api.hasPermission(sender, "npcbook.command.npc.set"))
                                commands.addAll(List.of("right", "left"));
                        }
                        case "remove" -> {
                            if (this.api.hasPermission(sender, "npcbook.command.npc.remove"))
                                commands.addAll(List.of("right", "left"));
                        }
                        case "getbook" -> {
                            if (this.api.hasPermission(sender, "npcbook.command.npc.getbook"))
                                commands.addAll(List.of("right", "left"));
                        }
                    }
                default:
                    break;
            }
            StringUtil.copyPartialMatches(args[2], commands, completions);
        } else if (args.length == 4) {
            switch (args[0]) {
                case "actionitem", "ai" -> {
                    if (this.api.hasPermission(sender, "npcbook.command.actionitem.set"))
                        if ("set".equalsIgnoreCase(args[1]))
                            commands.addAll(List.of("right", "left"));
                }
                case "interaction" -> {
                    switch (args[1]) {
                        case "set" -> {
                            switch (args[2]) {
                                case "block" -> {
                                    if (this.api.hasPermission(sender, "npcbook.command.interaction.set.block"))
                                        commands.addAll(List.of("right", "left"));
                                }
                                case "entity" -> {
                                    if (this.api.hasPermission(sender, "npcbook.command.interaction.set.entity"))
                                        commands.addAll(List.of("right", "left"));
                                }
                            }
                        }
                        case "remove" -> {
                            switch (args[2]) {
                                case "block" -> {
                                    if (this.api.hasPermission(sender, "npcbook.command.interaction.remove.block"))
                                        commands.addAll(List.of("right", "left"));
                                }
                                case "entity" -> {
                                    if (this.api.hasPermission(sender, "npcbook.command.interaction.remove.entity"))
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
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.RED + "<+ CitizensBooks +>");
        sender.sendMessage(ChatColor.GOLD + "Version: " + ChatColor.RED + this.plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GOLD + "Auhtor: " + ChatColor.RED + "NicoNekoDev");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
    }

    private void sendHelp(CommandSender sender, int page) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        if (page < 1 || page > 4) page = 1;
        sender.sendMessage("");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INFO).replace("%page%", page + ""));
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
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ARGUMENTS));
        sender.sendMessage("");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_FILTER_SET).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_FILTER_REMOVE).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_FILTER_GETBOOK).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_FILTER_LIST).split("\\$"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
    }

    private void sendActionItemHelp(CommandSender sender) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ARGUMENTS));
        sender.sendMessage("");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ACTIONITEM_SET).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ACTIONITEM_REMOVE).split("\\$"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
    }

    private void sendNpcHelp(CommandSender sender) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ARGUMENTS));
        sender.sendMessage("");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_NPC_SET).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_NPC_REMOVE).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_NPC_GETBOOK).split("\\$"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
    }

    private void sendInteractionSetHelp(CommandSender sender) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ARGUMENTS));
        sender.sendMessage("");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_SET_BLOCK).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_SET_ENTITY).split("\\$"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
    }

    private void sendInteractionRemoveHelp(CommandSender sender) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ARGUMENTS));
        sender.sendMessage("");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_REMOVE_BLOCK).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_REMOVE_ENTITY).split("\\$"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
    }

    private void sendInteractionHelp(CommandSender sender) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_ARGUMENTS));
        sender.sendMessage("");
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_SET_BLOCK).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_SET_ENTITY).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_REMOVE_BLOCK).split("\\$"));
        sender.sendMessage(messageSettings.getMessageNoHeader(Message.HELP_INTERACTION_REMOVE_ENTITY).split("\\$"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
    }

    private void sendFiltersList(CommandSender sender, int pageNum) {
        MessageSettings messageSettings = this.plugin.getSettings().getMessageSettings();
        LinkedList<String[]> pages = this.getSplitList(this.plugin.getStorage().getFilterNames());
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
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
        for (int i = 0; i < 10; i++) {
            String row = page[i];
            if (row == null) continue;
            sender.sendMessage(SettingsUtil.parseMessage("&c&l  " + (((pageNum - 1) * 10) + i + 1) + ") &a" + row));
        }
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
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