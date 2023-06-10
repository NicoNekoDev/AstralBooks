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

package ro.niconeko.astralbooks.utils;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public enum Message {
    HEADER("header", "&f[&6AstralBooks&f] &r"),
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
    BOOK_RECEIVED("book_received", "&aYou received the book in your inventory!"),
    CITIZENS_NOT_ENABLED("citizens_not_enabled", "&cCitizens plugin is not enabled! This command requires Citizens to be installed!"),
    NBTAPI_NOT_ENABLED("nbtapi_not_enabled", "&cNBTAPI plugin is not enabled! This command requires NBTAPI to be installed!"),
    SET_CUSTOM_COMMAND_SUCCESSFULLY("set_custom_command_successfully", "&aCommand %command% has been set!"),
    REMOVED_CUSTOM_COMMAND_SUCCESSFULLY("removed_custom_command_successfully", "&aCommand %command% has been removed!"),
    CONSOLE_CANNOT_USE_COMMAND("console_cannot_use_command", "&cYou have to be a player if you want to use this command!"),
    PLAYER_CANNOT_USE_COMMAND("player_cannot_use_command", "&cConsole command only!"),
    NEW_VERSION_AVAILABLE("new_version_available", "&aA new version of AstralBooks is available!"),
    FILTER_APPLIED_TO_ITEM("filter_applied_to_item", "&aFilter %filter_name% has been applied to your holding item."),
    FILTER_REMOVED_FROM_ITEM("filter_removed_from_item", "&aThe filter has been removed from your holding item."),
    PLAYER_NOT_FOUND("player_not_found", "&cPlayer not found!"),
    FILTER_NOT_FOUND("filter_not_found", "&cFilter not found!"),
    SET_JOIN_BOOK_SUCCESSFULLY("set_join_book_successfully", "&aYou have set the join book!"),
    REMOVED_JOIN_BOOK_SUCCESSFULLY("removed_join_book_successfully", "&aYou have removed the join book!"),
    FILTER_NAME_INVALID("filter_name_invalid", "&cThe filter name should only contain letters, numbers, dashes and underscores!"),
    COMMAND_NAME_INVALID("command_name_invalid", "&cThe command name should only contain letters, numbers, dashes and underscores!"),
    PERMISSION_INVALID("permission_invalid", "&cThe permission should only contain letters, numbers, dots, dashes and underscores!"),
    OPERATION_FAILED("operation_failed", "&cThe operation failed! Please check the console for errors!"),
    FILTERS_LIST_PAGE_NOT_FOUND("filters_list_page_not_found", "&cThere's no filters at page %page%"),
    FILTERS_LIST_PRESENT("filters_list_present", "&aHere's the list of filters present:"),
    FILTERS_LIST_NO_FILTER_PRESENT("filter_list_no_filter_present", "&cThere is no filter installed!"),
    OPENED_BOOK_FOR_PLAYERS("opened_books_for_player", "&aYou've successfully opened a book for %success% player(s). There was %failed% failed attempts."),
    OPENED_BOOK_FOR_PLAYER("opened_book_for_player", "&aYou've successfully opened a book for %player%."),
    BOOK_APPLY_TO_BLOCK_TIMEOUT("book_apply_to_block_timeout", "&aYou have 1 minute to apply the book! Right click the block you want to apply the book!"),
    BOOK_APPLY_TO_ENTITY_TIMEOUT("book_apply_to_entity_timeout", "&aYou have 1 minute to apply the book! Right click the entity you want to apply the book!"),
    BOOK_REMOVE_FROM_BLOCK_TIMEOUT("book_remove_from_block_timeout", "&aYou have 1 minute to remove the book! Right click the block you want the book to get removed!"),
    BOOK_REMOVE_FROM_ENTITY_TIMEOUT("book_remove_from_entity_timeout", "&aYou have 1 minute to remove the book! Right click the entity you want the book to get removed!"),
    BOOK_APPLIED_SUCCESSFULLY_TO_BLOCK("book_applied_successfully_to_block", "&aThe book have been applied to block! (%block_x%, %block_y%, %block_z%)"),
    BOOK_APPLIED_SUCCESSFULLY_TO_ENTITY("book_applied_successfully_to_entity", "&aThe book have been applied to entity! (%type%)"),
    BOOK_REMOVED_SUCCESSFULLY_FROM_BLOCK("book_removed_successfully_from_block", "&aThe book have been removed from block! (%block_x%, %block_y%, %block_z%)"),
    BOOK_REMOVED_SUCCESSFULLY_FROM_ENTITY("book_removed_successfully_from_entity", "&aThe book have been removed from entity! (%type%)"),
    ENTITY_IS_NPC("entity_is_npc", "&cThe entity is an Citizens NPC. Use &f/abook npc &cto set a book for this type of entity!"),
    BOOK_SECURITY_NOT_ENABLED("book_security_not_enabled", "&cBook security is not enabled!"),
    BOOK_SECURITY_NOT_FOUND("book_security_not_found", "&cNo security book found!"),
    BOOK_SECURITY_LIST_PRESENT("book_security_list_found", "&aHere's the list of books saved:"),
    BOOK_SECURITY_DATE_FORMAT("book_security_date_format", "dd/MM/yyyy-HH:mm:ss", Optional.of(List.of("https://help.gooddata.com/cloudconnect/manual/date-and-time-format.html"))),

    USAGE_NPC_SET("usage.npc.set", "&aUsage: &f/abook npc set [right/left]"),
    USAGE_NPC_REMOVE("usage.npc.remove", "&aUsage: &f/abook npc remove [right/left]"),
    USAGE_NPC_GETBOOK("usage.npc.getbook", "&aUsage: &f/abook npc getbook [right/left]"),
    USAGE_HELP("usage.help", "&aUsage: &f/abook help [page]"),
    USAGE_OPENBOOK("usage.openbook", "&aUsage: &f/abook openbook"),
    USAGE_CLOSEBOOK("usage.closebook", "&aUsage: &f/abook closebook <author> <title>"),
    USAGE_SETCMD("usage.setcmd", "&aUsage: &f/abook setcmd <command> <filter name> [permission]"),
    USAGE_REMCMD("usage.remove", "&aUsage: &f/abook remcmd <command>"),
    USAGE_SETJOIN("usage.setjoin", "&aUsage: &f/abook setjoin"),
    USAGE_REMJOIN("usage.remjoin", "&aUsage: &f/abook remjoin"),
    USAGE_FORCEOPEN("usage.forceopen", "&aUsage: &f/abook forceopen <filter name> <player>"),
    USAGE_FILTER_SET("usage.filter.set", "&aUsage: &f/abook filter set <filter name>"),
    USAGE_FILTER_REMOVE("usage.filter.remove", "&aUsage: &f/abook filter remove <filter name>"),
    USAGE_FILTER_GETBOOK("usage.filter.getbook", "&aUsage: &f/abook filter getbook <filter name>"),
    USAGE_FILTER_LIST("usage.filter.list", "&aUsage: &f/abook filter list [page]"),
    USAGE_ACTIONITEM_SET("usage.actionitem.set", "&aUsage: &f/abook actionitem set <filter name> [right/left]"),
    USAGE_ACTIONITEM_REMOVE("usage.actionitem.remove", "&aUsage: &f/abook actionitem remove [right/left]"),
    USAGE_INTERACTION_SET_BLOCK("usage.interaction.set.block", "&aUsage: &f/abook interaction set block [right/left]"),
    USAGE_INTERACTION_SET_ENTITY("usage.interaction.set.entity", "&aUsage: &f/abook interaction set entity [right/left]"),
    USAGE_INTERACTION_REMOVE_BLOCK("usage.interaction.remove.block", "&aUsage: &f/abook interaction remove block [right/left]"),
    USAGE_INTERACTION_REMOVE_ENTITY("usage.interaction.remove.entity", "&aUsage: &f/abook interaction remove entity [right/left]"),
    USAGE_SECURITY_LIST("usage.security.list", "&aUsage: &f/abook security list <*/player> [page]"),
    USAGE_SECURITY_GETBOOK("usage.security.getbook", "&aUsage: &f/abook security getbook <player> <timestamp>"),

    HELP_INFO("help.info", "&e<    &6Commands and Usage &f| &6Page index (%page%/3) &e    >"),
    HELP_ARGUMENTS("help.arguments", "&f(&c<> &f= &erequired argument&f, &c[] &f= &eoptional argument&f)"),
    HELP_ABOUT("help.about", "&f/abook about"),
    HELP_HELP("help.help", "&f/abook help [page] $ &9Show help page."),
    HELP_NPC_SET("help.npc.set", "&f/abook npc set [right/left] $ &9Set book for npc."),
    HELP_NPC_REMOVE("help.npc.remove", "&f/abook npc remove [right/left] $ &9Remove book for npc."),
    HELP_NPC_GETBOOK("help.npc.getbook", "&f/abook npc getbook [right/left] $ &9Get the book of npc."),
    HELP_OPENBOOK("help.openbook", "&f/abook openbook$ &9Open a writen book."),
    HELP_CLOSEBOOK("help.closebook", "&f/abook closebook <author> <title> $ &9Closes a writable book."),
    HELP_RELOAD("help.reload", "&f/abook reload $ &9Reload config file."),
    HELP_SETCMD("help.setcmd", "&f/abook setcmd <command> <filter name> [permission] $ &9Set command by the given filter name."),
    HELP_REMCMD("help.remcmd", "&f/abook remcmd <command> $ &9Remove command."),
    HELP_SETJOIN("help.setjoin", "&f/abook setjoin $ &9Set the join book."),
    HELP_REMJOIN("help.remjoin", "&f/abook remjoin $ &9Remove the join book."),
    HELP_FORCEOPEN("help.forceopen", "&f/abook forceopen <filter name> <player> $ &9Force a player to open a book."),
    HELP_FILTER_SET("help.filter.set", "&f/abook filter set <filter name> $ &9Set a filter by the given name."),
    HELP_FILTER_REMOVE("help.filter.remove", "&f/abook filter remove <filter name> $ &9Remove a filter by the given name."),
    HELP_FILTER_GETBOOK("help.filter.getbook", "&f/abook filter getbook <filter name> $ &9Get the book of filter by the given name."),
    HELP_FILTER_LIST("help.filter.list", "&f/abook filter list [page] $ &9List the filters present/installed."),
    HELP_ACTIONITEM_SET("help.actionitem.set", "&f/abook actionitem set <filter name> [right/left] $ &9Put a filter on the item you hold."),
    HELP_ACTIONITEM_REMOVE("help.actionitem.remove", "&f/abook actionitem remove [right/left] $ &9Remove the filter from the item you hold."),
    HELP_INTERACTION_SET_BLOCK("help.interaction.set.block", "&f/abook interaction set block [right/left] $ &9Apply a book on a block."),
    HELP_INTERACTION_SET_ENTITY("help.interaction.set.entity", "&f/abook interaction set entity [right/left] $ &9Apply a book on an entity."),
    HELP_INTERACTION_REMOVE_BLOCK("help.interaction.remove.block", "&f/abook interaction remove block [right/left] $ &9Remove a book from a block."),
    HELP_INTERACTION_REMOVE_ENTITY("help.interaction.remove.entity", "&f/abook interaction remove entity [right/left] $ &9Remove a book from an entity."),
    HELP_SECURITY_LIST("help.security.list", "&f/abook security list <*/player> [page] $ &9List all books created by a player or all players."),
    HELP_SECURITY_GETBOOK("help.security.getbook", "&f/abook security getbook <player> <timestamp> $ &9Get the book a player has created.");

    private final String key, def;
    private final Optional<List<String>> comments;

    Message(String key, String def, Optional<List<String>> comments) {
        this.key = key;
        this.def = def;
        this.comments = comments;
    }

    Message(String key, String def) {
        this(key, def, Optional.empty());
    }

    public final String getPath() {
        return this.key;
    }

    public final String getDefault() {
        return this.def;
    }

    public final Optional<List<String>> getComments() {
        return this.comments;
    }
}