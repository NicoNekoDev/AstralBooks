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

package ro.niconeko.astralbooks.values;

import ro.niconeko.astralbooks.utils.settings.ListedMessageSetting;
import ro.niconeko.astralbooks.utils.settings.MessageSetting;
import ro.niconeko.astralbooks.utils.settings.StringSetting;

import java.util.List;

public class Messages {
    public static final StringSetting HEADER = new StringSetting("header", "#ffcf96&lAstralBooks &8» ");
    public static final MessageSetting NO_PERMISSION = new MessageSetting("no_permission", List.of("[header]#ff8080You do not have permission!"));
    public static final MessageSetting CONFIG_RELOADED = new MessageSetting("config_reloaded", List.of("[header]#cdfad5Config reloaded!"));
    public static final MessageSetting SET_BOOK_SUCCESSFULLY = new MessageSetting("set_book_successfully", List.of("[header]#cdfad5You have set the book for %npc%!"));
    public static final MessageSetting REMOVED_BOOK_SUCCESSFULLY = new MessageSetting("removed_book_successfully", List.of("[header]#cdfad5You have removed the book for %npc%!"));
    public static final MessageSetting NO_NPC_SELECTED = new MessageSetting("no_npc_selected", List.of("[header]#ff8080You need to have an NPC selected!"));
    public static final MessageSetting NO_WRITTEN_BOOK_IN_HAND = new MessageSetting("no_written_book_in_hand", List.of("[header]#ff8080You need to have a written book in your hand!"));
    public static final MessageSetting NO_WRITABLE_BOOK_IN_HAND = new MessageSetting("no_writable_book_in_hand", List.of("[header]#ff8080You need to have a writable book in your hand!"));
    public static final MessageSetting NO_ITEM_IN_HAND = new MessageSetting("no_item_in_hand", List.of("[header]#ff8080You need to have an item in your hand!"));
    public static final MessageSetting NO_BOOK_FOR_NPC = new MessageSetting("no_book_for_npc", List.of("[header]#ff8080This NPC doesn't have a book!"));
    public static final MessageSetting NO_BOOK_FOR_FILTER = new MessageSetting("no_book_for_filter", List.of("[header]#ff8080This filter doesn't have a book!"));
    public static final MessageSetting FILTER_SAVED = new MessageSetting("filter_saved", List.of("[header]#cdfad5Filter %filter_name% was saved."));
    public static final MessageSetting FILTER_REMOVED = new MessageSetting("filter_removed", List.of("[header]#cdfad5Filter %filter_name% was removed."));
    public static final MessageSetting BOOK_RECEIVED = new MessageSetting("book_received", List.of("[header]#cdfad5You received the book in your inventory!"));
    public static final MessageSetting CITIZENS_NOT_ENABLED = new MessageSetting("citizens_not_enabled", List.of("[header]#ff8080Citizens plugin is not enabled! This command requires Citizens to be installed!"));
    public static final MessageSetting SET_CUSTOM_COMMAND_SUCCESSFULLY = new MessageSetting("set_custom_command_successfully", List.of("[header]#cdfad5Command %command% has been set!"));
    public static final MessageSetting REMOVED_CUSTOM_COMMAND_SUCCESSFULLY = new MessageSetting("removed_custom_command_successfully", List.of("[header]#cdfad5Command %command% has been removed!"));
    public static final MessageSetting CONSOLE_CANNOT_USE_COMMAND = new MessageSetting("console_cannot_use_command", List.of("[header]#ff8080You have to be a player if you want to use this command!"));
    public static final MessageSetting PLAYER_CANNOT_USE_COMMAND = new MessageSetting("player_cannot_use_command", List.of("[header]#ff8080Console command only!"));
    public static final MessageSetting NEW_VERSION_AVAILABLE = new MessageSetting("new_version_available", List.of("[header]#cdfad5A new version of AstralBooks is available!"));
    public static final MessageSetting FILTER_APPLIED_TO_ITEM = new MessageSetting("filter_applied_to_item", List.of("[header]#cdfad5Filter %filter_name% has been applied to your holding item."));
    public static final MessageSetting FILTER_REMOVED_FROM_ITEM = new MessageSetting("filter_removed_from_item", List.of("[header]#cdfad5The filter has been removed from your holding item."));
    public static final MessageSetting PLAYER_NOT_FOUND = new MessageSetting("player_not_found", List.of("[header]#ff8080Player not found!"));
    public static final MessageSetting FILTER_NOT_FOUND = new MessageSetting("filter_not_found", List.of("[header]#ff8080Filter not found!"));
    public static final MessageSetting SET_JOIN_BOOK_SUCCESSFULLY = new MessageSetting("set_join_book_successfully", List.of("[header]#cdfad5You have set the join book!"));
    public static final MessageSetting REMOVED_JOIN_BOOK_SUCCESSFULLY = new MessageSetting("removed_join_book_successfully", List.of("[header]#cdfad5You have removed the join book!"));
    public static final MessageSetting FILTER_NAME_INVALID = new MessageSetting("filter_name_invalid", List.of("[header]#ff8080The filter name should only contain letters, numbers, dashes and underscores!"));
    public static final MessageSetting COMMAND_NAME_INVALID = new MessageSetting("command_name_invalid", List.of("[header]#ff8080The command name should only contain letters, numbers, dashes and underscores!"));
    public static final MessageSetting PERMISSION_INVALID = new MessageSetting("permission_invalid", List.of("[header]#ff8080The permission should only contain letters, numbers, dots, dashes and underscores!"));
    public static final MessageSetting OPERATION_FAILED = new MessageSetting("operation_failed", List.of("[header]#ff8080The operation failed! Please check the console for errors!"));
    public static final MessageSetting FILTERS_LIST_PAGE_NOT_FOUND = new MessageSetting("filters_list_page_not_found", List.of("[header]#ff8080There's no filters at page %page%"));
    public static final MessageSetting FILTERS_LIST_PRESENT = new MessageSetting("filters_list_present", List.of("[header]#cdfad5Here's the list of filters present:"));
    public static final MessageSetting FILTERS_LIST_NO_FILTER_PRESENT = new MessageSetting("filter_list_no_filter_present", List.of("[header]#ff8080There is no filter installed!"));
    public static final MessageSetting OPENED_BOOK_FOR_PLAYERS = new MessageSetting("opened_books_for_player", List.of("[header]#cdfad5You've successfully opened a book for %success% player(s). There was %failed% failed attempts."));
    public static final MessageSetting OPENED_BOOK_FOR_PLAYER = new MessageSetting("opened_book_for_player", List.of("[header]#cdfad5You've successfully opened a book for %player%."));
    public static final MessageSetting BOOK_APPLY_TO_BLOCK_TIMEOUT = new MessageSetting("book_apply_to_block_timeout", List.of("[header]#cdfad5You have 1 minute to apply the book! Right click the block you want to apply the book!"));
    public static final MessageSetting BOOK_APPLY_TO_ENTITY_TIMEOUT = new MessageSetting("book_apply_to_entity_timeout", List.of("[header]#cdfad5You have 1 minute to apply the book! Right click the entity you want to apply the book!"));
    public static final MessageSetting BOOK_REMOVE_FROM_BLOCK_TIMEOUT = new MessageSetting("book_remove_from_block_timeout", List.of("[header]#cdfad5You have 1 minute to remove the book! Right click the block you want the book to get removed!"));
    public static final MessageSetting BOOK_REMOVE_FROM_ENTITY_TIMEOUT = new MessageSetting("book_remove_from_entity_timeout", List.of("[header]#cdfad5You have 1 minute to remove the book! Right click the entity you want the book to get removed!"));
    public static final MessageSetting BOOK_APPLIED_SUCCESSFULLY_TO_BLOCK = new MessageSetting("book_applied_successfully_to_block", List.of("[header]#cdfad5The book have been applied to block! (%block_x%, %block_y%, %block_z%)"));
    public static final MessageSetting BOOK_APPLIED_SUCCESSFULLY_TO_ENTITY = new MessageSetting("book_applied_successfully_to_entity", List.of("[header]#cdfad5The book have been applied to entity! (%type%)"));
    public static final MessageSetting BOOK_REMOVED_SUCCESSFULLY_FROM_BLOCK = new MessageSetting("book_removed_successfully_from_block", List.of("[header]#cdfad5The book have been removed from block! (%block_x%, %block_y%, %block_z%)"));
    public static final MessageSetting BOOK_REMOVED_SUCCESSFULLY_FROM_ENTITY = new MessageSetting("book_removed_successfully_from_entity", List.of("[header]#cdfad5The book have been removed from entity! (%type%)"));
    public static final MessageSetting ENTITY_IS_NPC = new MessageSetting("entity_is_npc", List.of("[header]#ff8080The entity is an Citizens NPC. Use &f/abook npc #ff8080to set a book for this type of entity!"));
    public static final MessageSetting BOOK_SECURITY_NOT_ENABLED = new MessageSetting("book_security_not_enabled", List.of("[header]#ff8080Book security is not enabled!"));
    public static final MessageSetting BOOK_SECURITY_NOT_FOUND = new MessageSetting("book_security_not_found", List.of("[header]#ff8080No security book found!"));
    public static final MessageSetting BOOK_SECURITY_LIST_PRESENT = new MessageSetting("book_security_list_found", List.of("[header]#cdfad5Here's the list of books saved:"));
    public static final StringSetting BOOK_SECURITY_DATE_FORMAT = new StringSetting("book_security_date_format", "dd/MM/yyyy-HH:mm:ss", List.of("https://help.gooddata.com/cloudconnect/manual/date-and-time-format.html"));


    public static final MessageSetting COMMAND_MAIN_HELP = new MessageSetting("command.main.help", List.of("[header]#ff8080For a list of subcommands: [T]/afurnaces help[H]Click to execute.[/H][C]/afurnaces help[/C][/T]"));
    public static final MessageSetting COMMAND_MAIN_UNKNOWN = new MessageSetting("command.main.unknown", List.of("[header]#ff8080Unknown subcommand: #cdfad5%command%"));
    public static final ListedMessageSetting COMMAND_HELP_LIST = new ListedMessageSetting("command.help.list", List.of(
            List.of(
                    "&8&m                     #ffcf96&lAstralBooks&8&m                     ",
                    " ",
                    " #ff8080&l<> &f- Required, #cdfad5&l[] &f- Optional",
                    " ",
                    " [T]#ffcf96/abooks help[H]Click to execute.[/H][C]/abooks help[/C][/T] #f6fdc3- #cdfad5Shows those help messages",
                    " [T]#ffcf96/abooks reload[H]Click to execute.[/H][C]/abooks reload[/C][/T] #f6fdc3- #cdfad5Reloads the plugin",
                    " ",
                    "     [T]&8« &7Prev Page &8«[H]Click to go to previous page.[/H][/T]&8&m             [T]&8» &7Next Page &8»[H]Click to go to next page.[/H][C]/afurnaces help 2[/C][/T]     "
            )
    ));
    public static final MessageSetting COMMAND_HELP_USAGE = new MessageSetting("command.help.usage", List.of("[header]#ff8080Usage: [T]/afurnaces help <page>[H]Click to copy to chat.[/H][SC]/afurnaces help [/SC][/T]"));
    public static final MessageSetting COMMAND_HELP_INVALID_PAGE = new MessageSetting("command.help.invalid_page", List.of("[header]#ff8080Invalid page: #cdfad5%page%"));

}
