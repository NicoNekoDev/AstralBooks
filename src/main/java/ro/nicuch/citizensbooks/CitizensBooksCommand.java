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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class CitizensBooksCommand implements TabExecutor {
    private final CitizensBooksPlugin plugin;
    private final CitizensBooksAPI api;

    public CitizensBooksCommand(CitizensBooksPlugin plugin) {
        api = (this.plugin = plugin).getAPI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Optional<NPC> npc = this.plugin.isCitizensEnabled() ? Optional.ofNullable(CitizensAPI.getDefaultNPCSelector().getSelected(sender)) : Optional.empty();
        int npcId = this.plugin.isCitizensEnabled() ? npc.map(NPC::getId).orElse(0) : 0;//using only npc.map(NPC::getId()).orElse(0); causes NoClassFoundException

        String bookPathBasedByHand = "save." + npcId + ".right_side"; //Default

        if (args.length > 1 && "left".equalsIgnoreCase(args[1]))
            bookPathBasedByHand = "save." + npcId + ".left_side";

        if (args.length > 0) {
            switch (args[0]) {
                case "forceopen":
                    if (this.api.hasPermission(sender, "npcbook.command.forceopen")) {
                        if (args.length > 2) {
                            if (this.api.hasFilter(args[1])) {
                                if ("*".equals(args[2]))
                                    Bukkit.getOnlinePlayers().forEach(player -> this.api.placeholderHook(player, this.api.getFilter(args[1]), null));
                                else {
                                    Optional<Player> optionalPlayer = Optional.ofNullable(this.api.getPlayer(args[2])); //spigot likes to fuck with us
                                    if (optionalPlayer.isPresent()) {
                                        this.api.openBook(optionalPlayer.get(), this.api.placeholderHook(optionalPlayer.get(), this.api.getFilter(args[1]), null));
                                    } else
                                        sender.sendMessage(this.plugin.getMessage("lang.player_not_found", ConfigDefaults.player_not_found));
                                }
                            } else
                                sender.sendMessage(this.plugin.getMessage("lang.filter_not_found", ConfigDefaults.filter_not_found));
                        } else
                            sender.sendMessage(this.plugin.getMessage("lang.usage.forceopen", ConfigDefaults.usage_forceopen));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
                    break;
                case "about":
                    this.sendAbout(sender);
                    break;
                case "reload":
                    if (this.api.hasPermission(sender, "npcbook.command.reload")) {
                        /*
                         * this.plugin.saveSettings();
                         *
                         * No need to be saved anymore!
                         * If config is edited, when reloaded is
                         * overriten the file, so the edit is lost
                         */
                        this.plugin.reloadSettings();
                        sender.sendMessage(this.plugin.getMessage("lang.config_reloaded", ConfigDefaults.config_reloaded));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
                    break;
                case "set":
                    if (!this.isPlayer(sender)) {
                        sender.sendMessage(this.plugin.getMessage("console_cannot_use_command", ConfigDefaults.console_cannot_use_command));
                        break;
                    }
                    if (this.api.hasPermission(sender, "npcbook.command.set")) {
                        if (this.plugin.isCitizensEnabled()) {
                            if (this.hasBookInHand((Player) sender)) {
                                if (npc.isPresent()) {
                                    this.plugin.getSettings().set(bookPathBasedByHand,
                                            this.api.bookToString(this.getBookFromHand((Player) sender)));
                                    this.plugin.saveSettings(); //Allways saved
                                    sender.sendMessage(this.plugin
                                            .getMessage("lang.set_book_successfully", ConfigDefaults.set_book_successfully)
                                            .replace("%npc%", npc.get().getFullName()));
                                } else
                                    sender.sendMessage(
                                            this.plugin.getMessage("lang.no_npc_selected", ConfigDefaults.no_npc_selected));
                            } else
                                sender.sendMessage(
                                        this.plugin.getMessage("lang.no_book_in_hand", ConfigDefaults.no_book_in_hand));
                        } else
                            sender.sendMessage(
                                    this.plugin.getMessage("lang.citizens_not_enabled", ConfigDefaults.citizens_not_enabled));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
                    break;
                case "setjoin":
                    if (!this.isPlayer(sender)) {
                        sender.sendMessage(this.plugin.getMessage("console_cannot_use_command", ConfigDefaults.console_cannot_use_command));
                        break;
                    }
                    if (this.api.hasPermission(sender, "npcbook.command.setjoin")) {
                        if (this.hasBookInHand((Player) sender)) {
                            this.plugin.getSettings().set("join_book",
                                    this.api.bookToString(this.getBookFromHand((Player) sender)));
                            this.plugin.saveSettings(); //Allways saved
                            sender.sendMessage(this.plugin
                                    .getMessage("lang.set_join_book_successfully", ConfigDefaults.set_join_book_successfully));
                        } else
                            sender.sendMessage(
                                    this.plugin.getMessage("lang.no_book_in_hand", ConfigDefaults.no_book_in_hand));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
                    break;
                case "remove":
                    if (this.api.hasPermission(sender, "npcbook.command.remove")) {
                        if (this.plugin.isCitizensEnabled()) {
                            if (npc.isPresent()) {
                                /* if (this.plugin.getSettings().isString("save." + npcId)) {} */
                                // Useless check, we just remove the data whatever exist or not
                                this.plugin.getSettings().set(bookPathBasedByHand, null);
                                this.plugin.saveSettings(); // Save is not mandatory, because the value may exist
                                sender.sendMessage(this.plugin
                                        .getMessage("lang.remove_book_successfully", ConfigDefaults.remove_book_successfully)
                                        .replace("%npc%", npc.get().getFullName()));
                            } else
                                sender.sendMessage(
                                        this.plugin.getMessage("lang.no_npc_selected", ConfigDefaults.no_npc_selected));
                        } else
                            sender.sendMessage(
                                    this.plugin.getMessage("lang.citizens_not_enabled", ConfigDefaults.citizens_not_enabled));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
                    break;
                case "remjoin":
                    if (this.api.hasPermission(sender, "npcbook.command.remjoin")) {
                        this.plugin.getSettings().set("join_book", null);
                        this.plugin.saveSettings(); //Allways saved
                        sender.sendMessage(this.plugin
                                .getMessage("lang.remove_join_book_successfully", ConfigDefaults.remove_join_book_successfully));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
                    break;
                case "getbook":
                    if (!this.isPlayer(sender)) {
                        sender.sendMessage(this.plugin.getMessage("console_cannot_use_command", ConfigDefaults.console_cannot_use_command));
                        break;
                    }
                    if (this.api.hasPermission(sender, "npcbook.command.getbook")) {
                        if (this.plugin.isCitizensEnabled()) {
                            if (npc.isPresent()) {
                                if (this.plugin.getSettings().isString(bookPathBasedByHand)) {
                                    ItemStack book = this.api.stringToBook(this.plugin.getSettings().getString(bookPathBasedByHand));
                                    ((Player) sender).getInventory().addItem(book);
                                    sender.sendMessage(this.plugin.getMessage("lang.book_recived", ConfigDefaults.book_recived));
                                } else
                                    sender.sendMessage(
                                            this.plugin.getMessage("lang.no_book_for_npc", ConfigDefaults.no_book_for_npc)
                                                    .replace("%npc%", npc.get().getFullName()));
                            } else
                                sender.sendMessage(
                                        this.plugin.getMessage("lang.no_npc_selected", ConfigDefaults.no_npc_selected));
                        } else
                            sender.sendMessage(
                                    this.plugin.getMessage("lang.citizens_not_enabled", ConfigDefaults.citizens_not_enabled));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
                    break;
                case "openbook":
                    if (!this.isPlayer(sender)) {
                        sender.sendMessage(this.plugin.getMessage("console_cannot_use_command", ConfigDefaults.console_cannot_use_command));
                        break;
                    }
                    if (this.api.hasPermission(sender, "npcbook.command.getbook")) {
                        if (this.hasBookInHand((Player) sender)) {
                            this.openBook((Player) sender, this.getBookFromHand((Player) sender));
                        } else
                            sender.sendMessage(
                                    this.plugin.getMessage("lang.no_book_in_hand", ConfigDefaults.no_book_in_hand));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
                    break;
                case "setcmd":
                    if (this.api.hasPermission(sender, "npcbook.command.setcmd")) {
                        if (args.length > 2) {
                            this.plugin.getSettings().set("commands." + args[1] + ".filter_name", args[2]);
                            this.plugin.getSettings().set("commands." + args[1] + ".permission", args.length > 3 ? args[3] : "none"); //Optional permission
                            this.plugin.saveSettings();
                            sender.sendMessage(this.plugin
                                    .getMessage("lang.set_custom_command_successfully",
                                            ConfigDefaults.set_custom_command_successfully)
                                    .replace("%command%", args[1]).replace("%filter_name%", args[2]));
                        } else
                            sender.sendMessage(this.plugin.getMessage("lang.usage.setcmd", ConfigDefaults.usage_setcmd));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
                    break;
                case "remcmd":
                    if (this.api.hasPermission(sender, "npcbook.command.remcmd")) {
                        if (args.length > 1) {
                            this.plugin.getSettings().set("commands." + args[1], null);
                            this.plugin.saveSettings();
                            sender.sendMessage(this.plugin
                                    .getMessage("lang.remove_custom_command_successfully",
                                            ConfigDefaults.remove_custom_command_successfully)
                                    .replace("%command%", args[1]));
                        } else
                            sender.sendMessage(this.plugin.getMessage("lang.usage.remcmd", ConfigDefaults.usage_remcmd));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
                    break;
                case "filter":
                    if (args.length > 1) {
                        switch (args[1]) {
                            case "set":
                                if (!this.isPlayer(sender)) {
                                    sender.sendMessage(this.plugin.getMessage("console_cannot_use_command", ConfigDefaults.console_cannot_use_command));
                                    break;
                                }
                                if (this.api.hasPermission(sender, "npcbook.command.filter.set")) {
                                    if (args.length > 2) {
                                        if (this.hasBookInHand((Player) sender)) {
                                            this.api.createFilter(args[2], this.getBookFromHand((Player) sender));
                                            sender.sendMessage(
                                                    this.plugin.getMessage("lang.filter_saved", ConfigDefaults.filter_saved)
                                                            .replace("%filter_name%", args[2]));
                                        } else
                                            sender.sendMessage(this.plugin.getMessage("lang.no_book_in_hand",
                                                    ConfigDefaults.no_book_in_hand));
                                    } else
                                        sender.sendMessage(
                                                this.plugin.getMessage("lang.usage.filter.set", ConfigDefaults.usage_filter_set));
                                } else
                                    sender.sendMessage(
                                            this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
                                break;
                            case "remove":
                                if (this.api.hasPermission(sender, "npcbook.command.filter.remove")) {
                                    if (args.length > 2) {
                                        this.api.removeFilter(args[2]);
                                        sender.sendMessage(
                                                this.plugin.getMessage("lang.filter_removed", ConfigDefaults.filter_removed)
                                                        .replace("%filter_name%", args[2]));
                                    } else
                                        sender.sendMessage(this.plugin.getMessage("lang.usage.filter.remove",
                                                ConfigDefaults.usage_filter_remove));
                                } else
                                    sender.sendMessage(
                                            this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
                                break;
                            case "getbook":
                                if (!this.isPlayer(sender)) {
                                    sender.sendMessage(this.plugin.getMessage("console_cannot_use_command", ConfigDefaults.console_cannot_use_command));
                                    break;
                                }
                                if (this.api.hasPermission(sender, "npcbook.command.filter.getbook")) {
                                    if (args.length > 2) {
                                        if (this.api.hasFilter(args[2])) {
                                            ItemStack book = this.api.getFilter(args[2]);
                                            ((Player) sender).getInventory().addItem(book);
                                            sender.sendMessage(
                                                    this.plugin.getMessage("lang.book_recived", ConfigDefaults.book_recived));
                                        } else
                                            sender.sendMessage(this.plugin.getMessage("lang.no_book_for_filter",
                                                    ConfigDefaults.no_book_for_filter));
                                    } else
                                        sender.sendMessage(this.plugin.getMessage("lang.usage.filter.getbook",
                                                ConfigDefaults.usage_filter_getbook));
                                } else
                                    sender.sendMessage(
                                            this.plugin.getMessage("lang.no_permission", ConfigDefaults.no_permission));
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
                        this.sendHelp(sender);
                    else
                        this.sendAbout(sender);
                    break;
            }
        } else {
            if (this.api.hasPermission(sender, "npcbook.command.help"))
                this.sendHelp(sender);
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
            if (this.api.hasPermission(sender, "npcbook.command.set"))
                commands.add("set");
            if (this.api.hasPermission(sender, "npcbook.command.remove"))
                commands.add("remove");
            if (this.api.hasPermission(sender, "npcbook.command.setjoin"))
                commands.add("setjoin");
            if (this.api.hasPermission(sender, "npcbook.command.remjoin"))
                commands.add("remjoin");
            if (this.api.hasPermission(sender, "npcbook.command.getbook"))
                commands.add("getbook");
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
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if (args.length == 2) {
            if (args[0].equals("filter")) {
                if (this.api.hasPermission(sender, "npcbook.command.filter.set"))
                    commands.add("set");
                if (this.api.hasPermission(sender, "npcbook.command.filter.remove"))
                    commands.add("remove");
                if (this.api.hasPermission(sender, "npcbook.command.filter.getbook"))
                    commands.add("getbook");
            }
            StringUtil.copyPartialMatches(args[1], commands, completions);
        }
        Collections.sort(completions);
        return completions;
    }

    @SuppressWarnings("deprecation")
    private boolean hasBookInHand(Player player) {
        ItemStack item;
        switch (CitizensBooksAPI.version) {
            case "v1_8_R3":
            case "v1_8_R2":
            case "v1_8_R1":
                item = player.getItemInHand();
                break;
            default:
                item = player.getInventory().getItemInMainHand();
                break;
        }
        if (item == null)
            return false;
        return item.getType() == Material.WRITTEN_BOOK;
    }

    private boolean isPlayer(CommandSender sender) {
        return (sender instanceof Player);
    }

    @SuppressWarnings("deprecation")
    private ItemStack getBookFromHand(Player player) {
        switch (CitizensBooksAPI.version) {
            case "v1_8_R3":
            case "v1_8_R2":
            case "v1_8_R1":
                return player.getItemInHand();
            default:
                return player.getInventory().getItemInMainHand();
        }
    }

    @SuppressWarnings("deprecation")
    private void openBook(Player player, ItemStack book) {
        BookMeta meta = (BookMeta) book.getItemMeta();
        Material material = Material.getMaterial("BOOK_AND_QUILL");
        if (material == null)
            // 1.13+
            material = Material.getMaterial("WRITABLE_BOOK");
        ItemStack item = new ItemStack(material);
        item.setItemMeta(meta);
        switch (CitizensBooksAPI.version) {
            case "v1_8_R3":
            case "v1_8_R2":
            case "v1_8_R1":
                player.setItemInHand(item);
            default:
                player.getInventory().setItemInMainHand(item);
        }
    }

    private void sendAbout(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------+");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.RED + "<+ CitizensBooks +>");
        sender.sendMessage(ChatColor.GOLD + "Version: " + ChatColor.RED + this.plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GOLD + "Auhtor: " + ChatColor.RED + "nicuch");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------+");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------+");
        sender.sendMessage("");
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.about", ConfigDefaults.help_about));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.set", ConfigDefaults.help_set));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.remove", ConfigDefaults.help_remove));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.setjoin", ConfigDefaults.help_setjoin));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.remjoin", ConfigDefaults.help_remjoin));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.reload", ConfigDefaults.help_reload));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.forceopen", ConfigDefaults.help_forceopen));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.getbook", ConfigDefaults.help_getbook));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.openbook", ConfigDefaults.help_openbook));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.setcmd", ConfigDefaults.help_setcmd));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.remcmd", ConfigDefaults.help_remcmd));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.filter.set", ConfigDefaults.help_filter_set));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.filter.remove", ConfigDefaults.help_filter_remove));
        sender.sendMessage(
                this.plugin.getMessageNoHeader("lang.help.filter.getbook", ConfigDefaults.help_filter_getbook));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------+");
    }

    private void sendFilterHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------+");
        sender.sendMessage("");
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.filter.set", ConfigDefaults.help_filter_set));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.filter.remove", ConfigDefaults.help_filter_remove));
        sender.sendMessage(
                this.plugin.getMessageNoHeader("lang.help.filter.getbook", ConfigDefaults.help_filter_getbook));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------+");
    }
}