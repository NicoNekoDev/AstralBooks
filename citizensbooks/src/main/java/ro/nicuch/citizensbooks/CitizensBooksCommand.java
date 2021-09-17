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

import de.tr7zw.nbtapi.NBTItem;
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
import ro.nicuch.citizensbooks.utils.Message;
import ro.nicuch.citizensbooks.utils.References;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CitizensBooksCommand implements TabExecutor {
    private final CitizensBooksPlugin plugin;
    private final CitizensBooksAPI api;

    public CitizensBooksCommand(CitizensBooksPlugin plugin) {
        this.api = (this.plugin = plugin).getAPI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = this.isPlayer(sender) ? (Player) sender : null;
        if (args.length > 0) {
            switch (args[0]) {
                case "help":
                    if (!this.api.hasPermission(sender, "npcbook.command.help")) {
                        sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (args.length > 1) {
                        try {
                            int page = Integer.parseInt(args[1]);
                            this.sendHelp(sender, page);
                        } catch (NumberFormatException ex) {
                            sender.sendMessage(this.plugin.getMessage(Message.USAGE_HELP));
                        }
                    } else
                        this.sendHelp(sender, 0);
                    break;
                case "npc":
                    if (!this.api.hasPermission(sender, "npcbook.command.npc")) {
                        sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (!this.plugin.isCitizensEnabled()) {
                        sender.sendMessage(this.plugin.getMessage(Message.CITIZENS_NOT_ENABLED));
                        break;
                    }
                    if (player == null) {
                        sender.sendMessage(this.plugin.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                        break;
                    }
                    Optional<NPC> npc;
                    String side = "right_side";
                    if (args.length > 1) {
                        switch (args[1]) {
                            case "set":
                                if (!this.api.hasPermission(sender, "npcbook.command.npc.set")) {
                                    sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (!this.hasBookInHand(player)) {
                                    sender.sendMessage(this.plugin.getMessage(Message.NO_BOOK_IN_HAND));
                                    break;
                                }
                                npc = Optional.ofNullable(CitizensAPI.getDefaultNPCSelector().getSelected(player));
                                if (npc.isEmpty()) {
                                    sender.sendMessage(this.plugin.getMessage(Message.NO_NPC_SELECTED));
                                    break;
                                }
                                if (args.length > 2) {
                                    if ("right".equalsIgnoreCase(args[2]))
                                        side = "right_side";
                                    else if ("left".equalsIgnoreCase(args[2]))
                                        side = "left_side";
                                }
                                this.api.putNPCBook(npc.get().getId(), side, this.getItemFromHand(player));
                                this.api.saveNPCBooks(this.plugin.getLogger());
                                sender.sendMessage(this.plugin.getMessage(Message.SET_BOOK_SUCCESSFULLY).replace("%npc%", npc.get().getFullName()));
                                break;
                            case "remove":
                                if (!this.api.hasPermission(sender, "npcbook.command.npc.remove")) {
                                    sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                npc = Optional.ofNullable(CitizensAPI.getDefaultNPCSelector().getSelected(player));
                                if (npc.isEmpty()) {
                                    sender.sendMessage(this.plugin.getMessage(Message.NO_NPC_SELECTED));
                                    break;
                                }
                                if (args.length > 2) {
                                    if ("right".equalsIgnoreCase(args[2]))
                                        side = "right_side";
                                    else if ("left".equalsIgnoreCase(args[2]))
                                        side = "left_side";
                                }
                                this.api.removeNPCBook(npc.get().getId(), side);
                                this.api.saveNPCBooks(this.plugin.getLogger());
                                break;
                            case "getbook":
                                if (!this.api.hasPermission(sender, "npcbook.command.npc.getbook")) {
                                    sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                npc = Optional.ofNullable(CitizensAPI.getDefaultNPCSelector().getSelected(player));
                                if (npc.isEmpty()) {
                                    sender.sendMessage(this.plugin.getMessage(Message.NO_NPC_SELECTED));
                                    break;
                                }
                                if (args.length > 2) {
                                    if ("right".equalsIgnoreCase(args[2]))
                                        side = "right_side";
                                    else if ("left".equalsIgnoreCase(args[2]))
                                        side = "left_side";
                                }
                                if (!this.api.hasNPCBook(npc.get().getId(), side)) {
                                    sender.sendMessage(this.plugin.getMessage(Message.NO_BOOK_FOR_NPC).replace("%npc%", npc.get().getFullName()));
                                    break;
                                }
                                ItemStack book = this.api.getNPCBook(npc.get().getId(), side, new ItemStack(Material.WRITTEN_BOOK));
                                player.getInventory().addItem(book);
                                sender.sendMessage(this.plugin.getMessage(Message.BOOK_RECIVED));
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                case "actionitem":
                case "ai":
                    if (!this.api.hasPermission(sender, "npcbook.command.actionitem")) {
                        sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (!this.plugin.isNBTAPIEnabled()) {
                        sender.sendMessage(this.plugin.getMessage(Message.NBTAPI_NOT_ENABLED));
                        break;
                    }
                    if (!this.isPlayer(sender)) {
                        sender.sendMessage(this.plugin.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                        break;
                    }
                    String action = References.NBTAPI_ITEM_RIGHT_KEY;
                    if (args.length > 1) {
                        switch (args[1]) {
                            case "set":
                                if (!this.api.hasPermission(sender, "npcbook.command.actionitem.set")) {
                                    sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (args.length > 2) {
                                    String filter_name = args[2];
                                    if (!this.api.isValidName(filter_name)) {
                                        sender.sendMessage(this.plugin.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter_name%", filter_name));
                                        break;
                                    }
                                    if (!this.api.hasFilter(filter_name)) {
                                        sender.sendMessage(this.plugin.getMessage(Message.FILTER_NOT_FOUND));
                                        break;
                                    }
                                    if (!this.hasItemInHand(player)) {
                                        sender.sendMessage(this.plugin.getMessage(Message.NO_ITEM_IN_HAND));
                                        break;
                                    }
                                    if (args.length > 3) {
                                        if ("right".equalsIgnoreCase(args[3]))
                                            action = References.NBTAPI_ITEM_RIGHT_KEY;
                                        else if ("left".equalsIgnoreCase(args[3]))
                                            action = References.NBTAPI_ITEM_LEFT_KEY;
                                    }
                                    ItemStack item = this.getItemFromHand(player);
                                    NBTItem nbtItem = new NBTItem(item);
                                    nbtItem.setString(action, filter_name);
                                    this.api.getDistribution().setItemInHand(player, item);
                                    sender.sendMessage(this.plugin.getMessage(Message.FILTER_APPLIED_TO_ITEM).replace("%filter_name%", filter_name));

                                } else
                                    sender.sendMessage(this.plugin.getMessage(Message.USAGE_ACTIONITEM_SET));
                                break;
                            case "remove":
                                if (!this.api.hasPermission(sender, "npcbook.command.actionitem.remove")) {
                                    sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (!this.hasItemInHand(player)) {
                                    sender.sendMessage(this.plugin.getMessage(Message.NO_ITEM_IN_HAND));
                                    break;
                                }
                                if (args.length > 2) {
                                    if ("right".equalsIgnoreCase(args[2]))
                                        action = References.NBTAPI_ITEM_RIGHT_KEY;
                                    else if ("left".equalsIgnoreCase(args[2]))
                                        action = References.NBTAPI_ITEM_LEFT_KEY;
                                }
                                ItemStack item = this.getItemFromHand(player);
                                NBTItem nbtItem = new NBTItem(item);
                                if (nbtItem.hasKey(action))
                                    nbtItem.removeKey(action);
                                this.api.getDistribution().setItemInHand(player, item);
                                sender.sendMessage(this.plugin.getMessage(Message.FILTER_REMOVED_FROM_ITEM));
                                break;
                            default:
                                this.sendActionItemHelp(sender);
                                break;
                        }
                    } else
                        this.sendActionItemHelp(sender);
                    break;
                case "forceopen":
                    if (!this.api.hasPermission(sender, "npcbook.command.forceopen")) {
                        sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (args.length > 2) {
                        String filter_name = args[1];
                        if (!this.api.isValidName(filter_name)) {
                            sender.sendMessage(this.plugin.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter_name%", filter_name));
                            break;
                        }
                        if (!this.api.hasFilter(filter_name)) {
                            sender.sendMessage(this.plugin.getMessage(Message.FILTER_NOT_FOUND));
                            break;
                        }
                        if ("*".equals(args[2]) || "@a".equals(args[2]))
                            Bukkit.getOnlinePlayers().forEach(p -> this.api.openBook(p, this.api.placeholderHook(p, this.api.getFilter(filter_name), null)));
                        else {
                            Optional<Player> optionalPlayer = this.api.getPlayer(args[2]);
                            if (optionalPlayer.isPresent()) {
                                this.api.openBook(optionalPlayer.get(), this.api.placeholderHook(optionalPlayer.get(), this.api.getFilter(filter_name), null));
                            } else
                                sender.sendMessage(this.plugin.getMessage(Message.PLAYER_NOT_FOUND));
                        }
                    } else
                        sender.sendMessage(this.plugin.getMessage(Message.USAGE_FORCEOPEN));
                    break;
                case "about":
                    this.sendAbout(sender);
                    break;
                case "reload":
                    if (!this.api.hasPermission(sender, "npcbook.command.reload")) {
                        sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    /*
                     * this.plugin.saveSettings();
                     *
                     * No need to be saved anymore!
                     * If config file is edited, the config is
                     * overwritten, so the edit is lost
                     */
                    this.plugin.reloadSettings();
                    if (this.plugin.isDatabaseEnabled()) // disable the database
                        this.plugin.getDatabase().disableDatabase(this.plugin.getLogger());
                    if (!this.plugin.setDatabaseEnabled(this.plugin.getDatabase().enableDatabase(this.plugin.getLogger())))
                        // 1) try to enable database
                        // 2) set the database being enabled
                        // 3) if is disabled, load default/normal filters
                        this.api.reloadFilters(this.plugin.getLogger()); // reload filters too
                    sender.sendMessage(this.plugin.getMessage(Message.CONFIG_RELOADED));
                    break;
                case "setjoin":
                    if (player == null) {
                        sender.sendMessage(this.plugin.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                        break;
                    }
                    if (!this.api.hasPermission(sender, "npcbook.command.setjoin")) {
                        sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (!this.hasBookInHand(player)) {
                        sender.sendMessage(this.plugin.getMessage(Message.NO_BOOK_IN_HAND));
                        break;
                    }
                    this.api.setJoinBook(this.getItemFromHand(player));
                    this.plugin.getSettings().set("join_book_last_change", System.currentTimeMillis());
                    this.plugin.saveSettings(); //Always saved
                    sender.sendMessage(this.plugin.getMessage(Message.SET_JOIN_BOOK_SUCCESSFULLY));
                    break;
                case "remjoin":
                    if (!this.api.hasPermission(sender, "npcbook.command.remjoin")) {
                        sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    this.api.removeJoinBook();
                    this.plugin.getSettings().set("join_book_last_change", 0);
                    this.plugin.saveSettings(); //Always saved
                    sender.sendMessage(this.plugin.getMessage(Message.REMOVED_JOIN_BOOK_SUCCESSFULLY));
                    break;
                case "openbook":
                    if (player == null) {
                        sender.sendMessage(this.plugin.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                        break;
                    }
                    if (!this.api.hasPermission(sender, "npcbook.command.getbook")) {
                        sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (!this.hasBookInHand(player)) {
                        sender.sendMessage(this.plugin.getMessage(Message.NO_BOOK_IN_HAND));
                        break;
                    }
                    this.openBook(player, this.getItemFromHand(player));
                    break;
                case "setcmd":
                    if (!this.api.hasPermission(sender, "npcbook.command.setcmd")) {
                        sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (args.length > 2) {
                        String command_name = args[1];
                        if (!this.api.isValidName(command_name)) {
                            sender.sendMessage(this.plugin.getMessage(Message.COMMAND_NAME_INVALID).replace("%invalid_command_name%", command_name));
                            break;
                        }
                        String filter_name = args[2];
                        if (!this.api.isValidName(filter_name)) {
                            sender.sendMessage(this.plugin.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter_name%", filter_name));
                            break;
                        }
                        this.plugin.getSettings().set("commands." + command_name + ".filter_name", filter_name);
                        this.plugin.getSettings().set("commands." + command_name + ".permission", args.length > 3 ? args[3] : "none"); //Optional permission
                        this.plugin.saveSettings();
                        sender.sendMessage(this.plugin.getMessage(Message.SET_CUSTOM_COMMAND_SUCCESSFULLY).replace("%command_name%", args[1]).replace("%filter_name%", filter_name));
                    } else
                        sender.sendMessage(this.plugin.getMessage(Message.USAGE_SETCMD));
                    break;
                case "remcmd":
                    if (!this.api.hasPermission(sender, "npcbook.command.remcmd")) {
                        sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (args.length > 1) {
                        String command_name = args[1];
                        if (!this.api.isValidName(command_name)) {
                            sender.sendMessage(this.plugin.getMessage(Message.COMMAND_NAME_INVALID).replace("%invalid_command_name%", command_name));
                            break;
                        }
                        this.plugin.getSettings().set("commands." + command_name, null);
                        this.plugin.saveSettings();
                        sender.sendMessage(this.plugin.getMessage(Message.REMOVED_CUSTOM_COMMAND_SUCCESSFULLY).replace("%command%", command_name));
                    } else
                        sender.sendMessage(this.plugin.getMessage(Message.USAGE_REMCMD));
                    break;
                case "filter":
                    if (!this.api.hasPermission(sender, "npcbook.command.filter")) {
                        sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                        break;
                    }
                    if (args.length > 1) {
                        switch (args[1]) {
                            case "set":
                                if (!this.isPlayer(sender)) {
                                    sender.sendMessage(this.plugin.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                                    break;
                                }
                                if (!this.api.hasPermission(sender, "npcbook.command.filter.set")) {
                                    sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (args.length > 2) {
                                    String filter_name = args[2];
                                    if (!this.api.isValidName(filter_name)) {
                                        sender.sendMessage(this.plugin.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter_name%", filter_name));
                                        break;
                                    }
                                    if (!this.hasBookInHand((Player) sender)) {
                                        sender.sendMessage(this.plugin.getMessage(Message.NO_BOOK_IN_HAND));
                                        break;
                                    }
                                    this.api.createFilter(filter_name, this.getItemFromHand((Player) sender));
                                    sender.sendMessage(this.plugin.getMessage(Message.FILTER_SAVED).replace("%filter_name%", filter_name));
                                } else
                                    sender.sendMessage(this.plugin.getMessage(Message.USAGE_FILTER_SET));
                                break;
                            case "remove":
                                if (!this.api.hasPermission(sender, "npcbook.command.filter.remove")) {
                                    sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (args.length > 2) {
                                    String filter_name = args[2];
                                    if (!this.api.isValidName(filter_name)) {
                                        sender.sendMessage(this.plugin.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter_name%", filter_name));
                                        break;
                                    }
                                    this.api.removeFilter(filter_name);
                                    sender.sendMessage(this.plugin.getMessage(Message.FILTER_REMOVED).replace("%filter_name%", filter_name));
                                } else
                                    sender.sendMessage(this.plugin.getMessage(Message.USAGE_FILTER_REMOVE));
                                break;
                            case "getbook":
                                if (player == null) {
                                    sender.sendMessage(this.plugin.getMessage(Message.CONSOLE_CANNOT_USE_COMMAND));
                                    break;
                                }
                                if (!this.api.hasPermission(sender, "npcbook.command.filter.getbook")) {
                                    sender.sendMessage(this.plugin.getMessage(Message.NO_PERMISSION));
                                    break;
                                }
                                if (args.length > 2) {
                                    String filter_name = args[2];
                                    if (!this.api.isValidName(filter_name)) {
                                        sender.sendMessage(this.plugin.getMessage(Message.FILTER_NAME_INVALID).replace("%invalid_filter_name%", filter_name));
                                        break;
                                    }
                                    if (!this.api.hasFilter(filter_name)) {
                                        sender.sendMessage(this.plugin.getMessage(Message.NO_BOOK_FOR_FILTER));
                                        break;
                                    }
                                    ItemStack book = this.api.getFilter(filter_name);
                                    player.getInventory().addItem(book);
                                    sender.sendMessage(this.plugin.getMessage(Message.BOOK_RECIVED));
                                } else
                                    sender.sendMessage(this.plugin.getMessage(Message.USAGE_FILTER_GETBOOK));
                                break;
                            default:
                                this.sendFilterHelp(sender);
                                break;
                        }
                    } else
                        this.sendFilterHelp(sender);
                    break;
                default:
                    if (this.api.hasPermission(sender, "npcbook.command.help"))
                        this.sendHelp(sender, 0);
                    else
                        this.sendAbout(sender);
                    break;
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
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
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
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if (args.length == 2) {
            switch (args[0]) {
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
                    break;
                case "forceopen":
                    if (this.api.hasPermission(sender, "npcbook.command.forceopen"))
                        commands.addAll(this.api.getFilters());
                    break;
                case "remcmd":
                    if (this.api.hasPermission(sender, "npcbook.command.remcmd"))
                        commands.addAll(this.plugin.getSettings().getConfigurationSection("commands").getKeys(false));
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
                case "filter":
                    if (args[1].equals("remove") || args[1].equals("getbook")) {
                        if (this.api.hasPermission(sender, "npcbook.command.filter.remove")
                                || this.api.hasPermission(sender, "npcbook.command.filter.getbook")) {
                            commands.addAll(this.api.getFilters());
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
                        commands.addAll(this.api.getFilters());
                    break;
                case "actionitem":
                case "ai":
                    switch (args[1]) {
                        case "set":
                            if (this.api.hasPermission(sender, "npcbook.command.actionitem.set"))
                                commands.addAll(this.api.getFilters());
                            break;
                        case "remove":
                            if (this.api.hasPermission(sender, "npcbook.command.actionitem.remove"))
                                commands.addAll(List.of("right", "left"));
                            break;
                        default:
                            break;
                    }
                case "npc":
                    switch (args[1]) {
                        case "set":
                            if (this.api.hasPermission(sender, "npcbook.command.npc.set"))
                                commands.addAll(List.of("right", "left"));
                            break;
                        case "remove":
                            if (this.api.hasPermission(sender, "npcbook.command.npc.remove"))
                                commands.addAll(List.of("right", "left"));
                            break;
                        case "getbook":
                            if (this.api.hasPermission(sender, "npcbook.command.npc.getbook"))
                                commands.addAll(List.of("right", "left"));
                            break;
                        default:
                            break;
                    }
                default:
                    break;
            }
            StringUtil.copyPartialMatches(args[2], commands, completions);
        } else if (args.length == 4) {
            switch (args[0]) {
                case "actionitem":
                case "ai":
                    if (this.api.hasPermission(sender, "npcbook.command.actionitem.set"))
                        if ("set".equalsIgnoreCase(args[1]))
                            commands.addAll(List.of("right", "left"));
                    break;
                default:
                    break;
            }
            StringUtil.copyPartialMatches(args[3], commands, completions);
        }
        Collections.sort(completions);
        return completions;
    }

    private boolean hasBookInHand(Player player) {
        ItemStack item = this.api.getDistribution().getItemInHand(player);
        if (item == null)
            return false;
        return item.getType() == Material.WRITTEN_BOOK;
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
        ItemStack item = new ItemStack(material);
        item.setItemMeta(meta);
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
        if (page < 1 || page > 3) page = 1;
        sender.sendMessage("");
        sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_INFO).replace("%page%", page + ""));
        sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_ARGUMENTS));
        sender.sendMessage("");
        if (page == 2) {
            sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_RELOAD).split("\\$"));
            sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_FORCEOPEN).split("\\$"));
            sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_GETBOOK).split("\\$"));
            sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_OPENBOOK).split("\\$"));
            sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_SETCMD).split("\\$"));
            sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_REMCMD).split("\\$"));
        } else if (page == 3) {
            sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_FILTER_SET).split("\\$"));
            sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_FILTER_REMOVE).split("\\$"));
            sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_FILTER_GETBOOK).split("\\$"));
            sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_ACTIONITEM_SET).split("\\$"));
            sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_ACTIONITEM_REMOVE).split("\\$"));
        } else {
            sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_HELP).split("\\$"));
            sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_ABOUT).split("\\$"));
            sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_SET).split("\\$"));
            sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_REMOVE).split("\\$"));
            sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_SETJOIN).split("\\$"));
            sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_REMJOIN).split("\\$"));
        }
        sender.sendMessage("");
    }

    private void sendFilterHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
        sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_ARGUMENTS));
        sender.sendMessage("");
        sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_FILTER_SET).split("\\$"));
        sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_FILTER_REMOVE).split("\\$"));
        sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_FILTER_GETBOOK).split("\\$"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
    }

    private void sendActionItemHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
        sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_ARGUMENTS));
        sender.sendMessage("");
        sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_ACTIONITEM_SET).split("\\$"));
        sender.sendMessage(this.plugin.getMessageNoHeader(Message.HELP_ACTIONITEM_REMOVE).split("\\$"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------------------+");
    }
}