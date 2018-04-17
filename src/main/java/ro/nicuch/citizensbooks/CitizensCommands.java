package ro.nicuch.citizensbooks;

import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.util.StringUtil;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.milkbowl.vault.permission.Permission;

public class CitizensCommands implements TabExecutor {
    private final CitizensBooks plugin;
    private final CitizensBooksAPI api;

    public CitizensCommands(CitizensBooks plugin) {
        api = (this.plugin = plugin).getAPI();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Permission perm = this.plugin.getPermission();
        NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(sender);
        int npcId = npc != null ? npc.getId() : 0;
        if (args.length > 0) {
            switch (args[0]) {
                case "about":
                    this.sendAbout(sender);
                    break;
                case "reload":
                    if (perm.has(sender, "npcbook.command.reload")) {
                        this.plugin.reloadSettings();
                        sender.sendMessage(this.plugin.getMessage("lang.config_reloaded", LangDefaults.config_reloaded));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
                    break;
                case "set":
                    if (!this.isPlayer(sender)) {
                        sender.sendMessage(this.plugin.getMessage("console_cannot_use_command", LangDefaults.console_cannot_use_command));
                        break;
                    }
                    if (perm.has(sender, "npcbook.command.set")) {
                        if (this.hasBookInHand((Player) sender)) {
                            if (npc != null) {
                                this.plugin.getConfig().set("save." + npcId,
                                        this.api.bookToString(this.getBookFromHand((Player) sender)));
                                this.plugin.saveSettings();
                                sender.sendMessage(this.plugin
                                        .getMessage("lang.set_book_successfully", LangDefaults.set_book_successfully)
                                        .replace("%npc%", npc.getFullName()));
                            } else
                                sender.sendMessage(
                                        this.plugin.getMessage("lang.no_npc_selected", LangDefaults.no_npc_selected));
                        } else
                            sender.sendMessage(
                                    this.plugin.getMessage("lang.no_book_in_hand", LangDefaults.no_book_in_hand));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
                    break;
                case "remove":
                    if (perm.has(sender, "npcbook.command.remove")) {
                        if (npc != null) {
                            /* if (this.plugin.getConfig().isString("save." + npcId)) {} */
                            // Useless check, we just remove the data whatever exist or not
                            this.plugin.getConfig().set("save." + npcId, null);
                            this.plugin.saveSettings(); // Save is not mandatory, because the value may exist
                            sender.sendMessage(this.plugin
                                    .getMessage("lang.remove_book_successfully", LangDefaults.remove_book_successfully)
                                    .replace("%npc%", npc.getFullName()));
                        } else
                            sender.sendMessage(
                                    this.plugin.getMessage("lang.no_npc_selected", LangDefaults.no_npc_selected));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
                    break;
                case "getbook":
                    if (!this.isPlayer(sender)) {
                        sender.sendMessage(this.plugin.getMessage("console_cannot_use_command", LangDefaults.console_cannot_use_command));
                        break;
                    }
                    if (perm.has(sender, "npcbook.command.getbook")) {
                        if (npc != null) {
                            if (this.plugin.getConfig().isString("save." + npcId)) {
                                ItemStack book = this.api.stringToBook(this.plugin.getConfig().getString("save." + npcId));
                                ((Player) sender).getInventory().addItem(book);
                                sender.sendMessage(this.plugin.getMessage("lang.book_recived", LangDefaults.book_recived));
                            } else
                                sender.sendMessage(
                                        this.plugin.getMessage("lang.no_book_for_npc", LangDefaults.no_book_for_npc)
                                                .replace("%npc%", npc.getFullName()));
                        } else
                            sender.sendMessage(
                                    this.plugin.getMessage("lang.no_npc_selected", LangDefaults.no_npc_selected));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
                    break;
                case "openbook":
                    if (!this.isPlayer(sender)) {
                        sender.sendMessage(this.plugin.getMessage("console_cannot_use_command", LangDefaults.console_cannot_use_command));
                        break;
                    }
                    if (perm.has(sender, "npcbook.command.getbook")) {
                        if (this.hasBookInHand((Player) sender)) {
                            this.openBook((Player) sender, this.getBookFromHand((Player) sender));
                        } else
                            sender.sendMessage(
                                    this.plugin.getMessage("lang.no_book_in_hand", LangDefaults.no_book_in_hand));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
                    break;
                case "setcmd":
                    if (perm.has(sender, "npcbook.command.setcmd")) {
                        if (args.length > 2) {
                            this.plugin.getConfig().set("commands." + args[1], args[2]);
                            this.plugin.saveSettings();
                            sender.sendMessage(this.plugin
                                    .getMessage("lang.set_custom_command_successfully",
                                            LangDefaults.set_custom_command_successfully)
                                    .replace("%command%", args[1]).replace("%filter_name%", args[2]));
                        } else
                            sender.sendMessage(this.plugin.getMessage("lang.usage.setcmd", LangDefaults.usage_setcmd));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
                    break;
                case "remcmd":
                    if (perm.has(sender, "npcbook.command.remcmd")) {
                        if (args.length > 1) {
                            this.plugin.getConfig().set("commands." + args[1], null);
                            this.plugin.saveSettings();
                            sender.sendMessage(this.plugin
                                    .getMessage("lang.remove_custom_command_successfully",
                                            LangDefaults.remove_custom_command_successfully)
                                    .replace("%command%", args[1]));
                        } else
                            sender.sendMessage(this.plugin.getMessage("lang.usage.remcmd", LangDefaults.usage_remcmd));
                    } else
                        sender.sendMessage(this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
                    break;
                case "filter":
                    if (args.length > 1) {
                        switch (args[1]) {
                            case "set":
                                if (!this.isPlayer(sender)) {
                                    sender.sendMessage(this.plugin.getMessage("console_cannot_use_command", LangDefaults.console_cannot_use_command));
                                    break;
                                }
                                if (perm.has(sender, "npcbook.command.filter.set")) {
                                    if (args.length > 2) {
                                        if (this.hasBookInHand((Player) sender)) {
                                            this.api.createFilter(args[2], this.getBookFromHand((Player) sender));
                                            sender.sendMessage(
                                                    this.plugin.getMessage("lang.filter_saved", LangDefaults.filter_saved)
                                                            .replace("%filter_name%", args[2]));
                                        } else
                                            sender.sendMessage(this.plugin.getMessage("lang.no_book_in_hand",
                                                    LangDefaults.no_book_in_hand));
                                    } else
                                        sender.sendMessage(
                                                this.plugin.getMessage("lang.usage.filter.set", LangDefaults.usage_filter_set));
                                } else
                                    sender.sendMessage(
                                            this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
                                break;
                            case "remove":
                                if (perm.has(sender, "npcbook.command.filter.remove")) {
                                    if (args.length > 2) {
                                        this.api.removeFilter(args[2]);
                                        sender.sendMessage(
                                                this.plugin.getMessage("lang.filter_removed", LangDefaults.filter_removed)
                                                        .replace("%filter_name%", args[2]));
                                    } else
                                        sender.sendMessage(this.plugin.getMessage("lang.usage.filter.remove",
                                                LangDefaults.usage_filter_remove));
                                } else
                                    sender.sendMessage(
                                            this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
                                break;
                            case "getbook":
                                if (!this.isPlayer(sender)) {
                                    sender.sendMessage(this.plugin.getMessage("console_cannot_use_command", LangDefaults.console_cannot_use_command));
                                    break;
                                }
                                if (perm.has(sender, "npcbook.command.filter.getbook")) {
                                    if (args.length > 2) {
                                        if (this.api.hasFilter(args[2])) {
                                            ItemStack book = this.api.getFilter(args[2]);
                                            ((Player) sender).getInventory().addItem(book);
                                            sender.sendMessage(
                                                    this.plugin.getMessage("lang.book_recived", LangDefaults.book_recived));
                                        } else
                                            sender.sendMessage(this.plugin.getMessage("lang.no_book_for_filter",
                                                    LangDefaults.no_book_for_filter));
                                    } else
                                        sender.sendMessage(this.plugin.getMessage("lang.usage.filter.getbook",
                                                LangDefaults.usage_filter_getbook));
                                } else
                                    sender.sendMessage(
                                            this.plugin.getMessage("lang.no_permission", LangDefaults.no_permission));
                                break;
                            default:
                                this.sendFilterHelp(sender);
                                break;
                        }
                    } else
                        this.sendFilterHelp(sender);
                    break;
                default:
                    if (perm.has(sender, "npcbook.command.help"))
                        this.sendHelp(sender);
                    else
                        this.sendAbout(sender);
                    break;
            }
        } else {
            if (perm.has(sender, "npcbook.command.help"))
                this.sendHelp(sender);
            else
                this.sendAbout(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = Lists.newArrayList();
        List<String> commands = Lists.newArrayList();
        Permission perm = this.plugin.getPermission();
        if (args.length == 1) {
            if (perm.has(sender, "npcbook.command.set"))
                commands.add("set");
            if (perm.has(sender, "npcbook.command.remove"))
                commands.add("remove");
            if (perm.has(sender, "npcbook.command.getbook"))
                commands.add("getbook");
            if (perm.has(sender, "npcbook.command.openbook"))
                commands.add("openbook");
            if (perm.has(sender, "npcbook.command.filter"))
                commands.add("filter");
            if (perm.has(sender, "npcbook.command.setcmd"))
                commands.add("setcmd");
            if (perm.has(sender, "npcbook.command.remcmd"))
                commands.add("remcmd");
            StringUtil.copyPartialMatches(args[0], commands, completions);
        } else if (args.length == 2) {
            if (args[0].equals("filter")) {
                if (perm.has(sender, "npcbook.command.filter.set"))
                    commands.add("set");
                if (perm.has(sender, "npcbook.command.filter.remove"))
                    commands.add("remove");
                if (perm.has(sender, "npcbook.command.filter.getbook"))
                    commands.add("getbook");
            }
            StringUtil.copyPartialMatches(args[1], commands, completions);
        }
        Collections.sort(completions);
        return completions;
    }

    private void sendFilterHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------+");
        sender.sendMessage("");
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.filter.set", LangDefaults.help_filter_set));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.filter.remove", LangDefaults.help_filter_remove));
        sender.sendMessage(
                this.plugin.getMessageNoHeader("lang.help.filter.getbook", LangDefaults.help_filter_getbook));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------+");
    }

    private boolean hasBookInHand(Player player) {
        ItemStack item;
        switch (api.version) {
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
        return item.getType().equals(Material.WRITTEN_BOOK);
    }

    private boolean isPlayer(CommandSender sender) {
        return (sender instanceof Player);
    }

    private ItemStack getBookFromHand(Player player) {
        switch (api.version) {
            case "v1_8_R3":
            case "v1_8_R2":
            case "v1_8_R1":
                return player.getItemInHand();
            default:
                return player.getInventory().getItemInMainHand();
        }
    }

    private void openBook(Player player, ItemStack book) {
        BookMeta meta = (BookMeta) book.getItemMeta();
        ItemStack item = new ItemStack(Material.BOOK_AND_QUILL);
        item.setItemMeta(meta);
        switch (api.version) {
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
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.about", LangDefaults.help_about));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.set", LangDefaults.help_set));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.remove", LangDefaults.help_remove));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.reload", LangDefaults.help_reload));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.getbook", LangDefaults.help_getbook));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.openbook", LangDefaults.help_openbook));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.setcmd", LangDefaults.help_setcmd));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.remcmd", LangDefaults.help_remcmd));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.filter.set", LangDefaults.help_filter_set));
        sender.sendMessage(this.plugin.getMessageNoHeader("lang.help.filter.remove", LangDefaults.help_filter_remove));
        sender.sendMessage(
                this.plugin.getMessageNoHeader("lang.help.filter.getbook", LangDefaults.help_filter_getbook));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "+----------------------+");
    }
}