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

package ro.nicuch.citizensbooks.utils;

public enum Message {
    HEADER("header", "&f[&6CitizensBooks&f] &r"),
    NO_PERMISSION("no_permission", "&cYou don't have permission!"),
    CONFIG_RELOADED("config_reloaded", "&aConfig reloaded."),
    SET_BOOK_SUCCESSFULLY("set_book_successfully", "&aYou have set the book for %npc%&a!"),
    REMOVED_BOOK_SUCCESSFULLY("removed_book_successfully", "&aYou have removed the book for %npc%&a!"),
    NO_NPC_SELECTED("no_npc_selected", "&cYou need to have an NPC selected!"),
    NO_WRITTEN_BOOK_IN_HAND("no_written_book_in_hand", "&cYou need to have a written book in your hand!"),
    NO_WRITABLE_BOOK_IN_HAND("no_writable_book_in_hand", "&cYou need to have a writable book in your hand!"),
    NO_ITEM_IN_HAND("no_item_in_hand", "&cYou need to have an item in your hand!"),
    NO_BOOK_FOR_NPC("no_book_for_npc", "&cThis NPC doesn't have a book!"),
    NO_BOOK_FOR_FILTER("no_book_for_filter", "&cThis filter doesn't have a book!"),
    FILTER_SAVED("filter_saved", "&aFilter %filter_name% was saved."),
    FILTER_REMOVED("filter_removed", "&aFilter %filter_name% was removed."),
    BOOK_RECEIVED("book_received", "&aYou recived the book in your inventory!"),
    CITIZENS_NOT_ENABLED("citizens_not_enabled", "&cCitizens plugin is not enabled! This command requires Citizens to be installed!"),
    NBTAPI_NOT_ENABLED("nbtapi_not_enabled", "&cNBTAPI plugin is not enabled! This command requires NBTAPI to be installed!"),
    SET_CUSTOM_COMMAND_SUCCESSFULLY("set_custom_command_successfully", "&aCommand %command% has been set!"),
    REMOVED_CUSTOM_COMMAND_SUCCESSFULLY("removed_custom_command_successfully", "&aCommand %command% has been removed!"),
    CONSOLE_CANNOT_USE_COMMAND("console_cannot_use_command", "&cYou have to be a player if you want to use this command!"),
    NEW_VERSION_AVAILABLE("new_version_available", "&aA new version of CitizensBooks is available!"),
    FILTER_APPLIED_TO_ITEM("filter_applied_to_item", "&aFilter %filter_name% has been applied to your holding item."),
    FILTER_REMOVED_FROM_ITEM("filter_removed_from_item", "&aThe filter has been removed from your holding item."),
    PLAYER_NOT_FOUND("player_not_found", "&cPlayer not found!"),
    FILTER_NOT_FOUND("filter_not_found", "&cFilter not found!"),
    SET_JOIN_BOOK_SUCCESSFULLY("set_join_book_successfully", "&aYou have set the join book!"),
    REMOVED_JOIN_BOOK_SUCCESSFULLY("removed_join_book_successfully", "&aYou have removed the join book!"),
    FILTER_NAME_INVALID("filter_name_invalid", "&cThe filter name should only contain letters, numbers, dashes and underscores!"),
    COMMAND_NAME_INVALID("command_name_invalid", "&cThe command name should only contain letters, numbers, dashes and underscores!"),

    USAGE_NPC_SET("usage.npc.set", "&aUsage: &f/npcbook npc set [right/left]"),
    USAGE_NPC_REMOVE("usage.npc.remove", "&aUsage: &f/npcbook npc remove [right/left]"),
    USAGE_NPC_GETBOOK("usage.npc.getbook", "&aUsage: &f/npcbook npc getbook [right/left]"),
    USAGE_HELP("usage.help", "&aUsage: &f/npcbook help [page]"),
    USAGE_OPENBOOK("usage.openbook", "&aUsage: &f/npcbook openbook"),
    USAGE_CLOSEBOOK("usage.closebook", "&aUsage: &f/npcbook closebook <author> <title>"),
    USAGE_SETCMD("usage.setcmd", "&aUsage: &f/npcbook setcmd <command> <filter name>"),
    USAGE_REMCMD("usage.remove", "&aUsage: &f/npcbook remcmd <command>"),
    USAGE_SETJOIN("usage.setjoin", "&aUsage: &f/npcbook setjoin"),
    USAGE_REMJOIN("usage.remjoin", "&aUsage: &f/npcbook remjoin"),
    USAGE_FORCEOPEN("usage.forceopen", "&aUsage: &f/npcbook forceopen <filter name> <player>"),
    USAGE_FILTER_SET("usage.filter.set", "&aUsage: &f/npcbook filter set <filter name>"),
    USAGE_FILTER_REMOVE("usage.filter.remove", "&aUsage: &f/npcbook filter remove <filter name>"),
    USAGE_FILTER_GETBOOK("usage.filter.getbook", "&aUsage: &f/npcbook filter getbook <filter name>"),
    USAGE_ACTIONITEM_SET("usage.actionitem.set", "&aUsage: &f/npcbook actionitem set <filter name> [right/left]"),
    USAGE_ACTIONITEM_REMOVE("usage.actionitem.remove", "&aUsage: &f/npcbook actionitem remove [right/left]"),


    HELP_INFO("help.info", "&e<    &6Commands and Usage &f| &6Page index (%page%/3) &e    >"),
    HELP_ARGUMENTS("help.arguments", "&f(&c<> &f= &erequired argument&f, &c[] &f= &eoptional argument&f)"),
    HELP_ABOUT("help.about", "&f/npcbook about"),
    HELP_HELP("help.help", "&f/npcbook help [page] $ &9Show help page."),
    HELP_NPC_SET("help.npc.set", "&f/npcbook npc set [right/left] $ &9Set book for npc."),
    HELP_NPC_REMOVE("help.npc.remove", "&f/npcbook npc remove [right/left] $ &9Remove book for npc."),
    HELP_NPC_GETBOOK("help.npc.getbook", "&f/npcbook npc getbook [right/left] $ &9Get the book of npc."),
    HELP_OPENBOOK("help.openbook", "&f/npcbook openbook $ &9Open a writen book."),
    HELP_CLOSEBOOK("help.closebook", "&f/npcbook closebook <author> <title> $ &9Closes a writable book."),
    HELP_RELOAD("help.reload", "&f/npcbook reload $ &9Reload config file."),
    HELP_SETCMD("help.setcmd", "&f/npcbook setcmd <command> <filter name> $ &9Set command by the given filter name."),
    HELP_REMCMD("help.remcmd", "&f/npcbook remcmd <command> $ &9Remove command."),
    HELP_SETJOIN("help.setjoin", "&f/npcbook setjoin $ &9Set the join book."),
    HELP_REMJOIN("help.remjoin", "&f/npcbook remjoin $ &9Remove the join book."),
    HELP_FORCEOPEN("help.forceopen", "&f/npcbook forceopen <filter name> <player> $ &9Force a player to open a book."),
    HELP_FILTER_SET("help.filter.set", "&f/npcbook filter set <filter name> $ &9Set a filter by the given name."),
    HELP_FILTER_REMOVE("help.filter.remove", "&f/npcbook filter remove <filter name> [right/left] $ &9Remove a filter by the given name."),
    HELP_FILTER_GETBOOK("help.filter.getbook", "&f/npcbook filter getbook <filter name> $ &9Get the book of filter by the given name."),
    HELP_ACTIONITEM_SET("help.actionitem.set", "&f/npcbook actionitem set <filter name> [right/left] $ &9Put a filter on the item you hold."),
    HELP_ACTIONITEM_REMOVE("help.actionitem.remove", "&f/npcbook actionitem remove [right/left] $ &9Remove the filter from the item you hold.");

    private final String key, def;

    Message(String key, String def) {
        this.key = key;
        this.def = def;
    }

    public final String getPath() {
        return "lang." + this.key;
    }

    public final String getPathStripped() {
        return this.key;
    }

    public final String getDefault() {
        return this.def;
    }
}