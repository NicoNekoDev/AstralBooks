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

package ro.nicuch.citizensbooks.utils;

public class ConfigDefaults {
    public static final String header = "&f[&6CitizensBooks&f] &r";
    public static final String no_permission = "&cYou don't have permission!";
    public static final String config_reloaded = "&aConfig reloaded.";
    public static final String set_book_successfully = "&aYou have set the book for %npc%&a!";
    public static final String remove_book_successfully = "&aYou have removed the book for %npc%&a!";
    public static final String no_npc_selected = "&cYou need to have an NPC selected!";
    public static final String no_book_in_hand = "&cYou need to have a book in your hand!";
    public static final String no_item_in_hand = "&cYou need to have an item in your hand!";
    public static final String no_book_for_npc = "&cThis NPC doesn''t have a book!";
    public static final String no_book_for_filter = "&cThis filter doesn''t have a book!";
    public static final String filter_saved = "&aFilter %filter_name% was saved.";
    public static final String filter_removed = "&aFilter %filter_name% was removed.";
    public static final String book_recived = "&aYou recived the book in your inventory!";
    public static final String citizens_not_enabled = "&cCitizens plugin is not enabled! This command requires Citizens to be installed!";
    public static final String nbtapi_not_enabled = "&cNBTAPI plugin is not enabled! This command requires NBTAPI to be installed!";
    public static final String set_custom_command_successfully = "&aCommand %command% has been set!";
    public static final String remove_custom_command_successfully = "&aCommand %command% has been removed!";
    public static final String console_cannot_use_command = "&cYou have to be a player if you want to use this command!";
    public static final String new_version_available = "&aA new version of CitizensBooks is available!";
    public static final String filter_applied_to_item = "&aFilter %filter_name% has been applied to your holding item.";
    public static final String filter_removed_from_item = "&aThe filter has been removed from your holding item.";

    public static final String player_not_found = "&cPlayer not found!";
    public static final String filter_not_found = "&cFilter not found!";

    public static final String set_join_book_successfully = "&aYou have set the join book!";
    public static final String remove_join_book_successfully = "&aYou have removed the join book!";
    public static final String usage_set = "&aUsage: &f/npcbook set [right/left]";
    public static final String usage_remove = "&aUsage: &f/npcbook remove [right/left]";
    public static final String usage_getbook = "&aUsage: &f/npcbook getbook [right/left]";
    public static final String usage_openbook = "&aUsage: &f/npcbook openbook";
    public static final String usage_setcmd = "&aUsage: &f/npcbook setcmd <command> <filter name>";
    public static final String usage_remcmd = "&aUsage: &f/npcbook remcmd <command>";
    public static final String usage_setjoin = "&aUsage: &f/npcbook setjoin";
    public static final String usage_remjoin = "&aUsage: &f/npcbook remjoin";
    public static final String usage_forceopen = "&aUsage: &f/npcbook forceopen <filter name> <player>";
    public static final String usage_filter_set = "&aUsage: &f/npcbook filter set <filter name>";
    public static final String usage_filter_remove = "&aUsage: &f/npcbook filter remove <filter name>";
    public static final String usage_filter_getbook = "&aUsage: &f/npcbook filter getbook <filter name>";
    public static final String usage_actionitem_set = "&aUsage: &f/npcbook actionitem set <filter name> [right/left]";
    public static final String usage_actionitem_remove = "&aUsage: &f/npcbook actionitem remove [right/left]";

    public static final String help_info = "&9Commands and Usage";
    public static final String help_arguments = "&f(&c<> &f= &erequired argument&f, &c[] &f= &eoptional argument&f)";
    public static final String help_about = "&f/npcbook about &c- &9Informations about plugin";
    public static final String help_set = "&f/npcbook set [right/left] &c- &9Set book for npc.";
    public static final String help_remove = "&f/npcbook remove [right/left] &c- &9Remove book for npc.";
    public static final String help_getbook = "&f/npcbook getbook [right/left] &c- &9Get the book of npc.";
    public static final String help_openbook = "&f/npcbook openbook &c- &9Open a writen book.";
    public static final String help_reload = "&f/npcbook reload &c- &9Reload config file.";
    public static final String help_setcmd = "&f/npcbook setcmd <command> <filter name> &c- &9Set command by the given filter name.";
    public static final String help_remcmd = "&f/npcbook remcmd <command> &c- &9Remove command.";
    public static final String help_setjoin = "&f/npcbook setjoin &c- &9Set the join book.";
    public static final String help_remjoin = "&f/npcbook remjoin &c- &9Remove the join book.";
    public static final String help_forceopen = "&f/npcbook forceopen <filter name> <player> &c- &9Force a player to open a book.";
    public static final String help_filter_set = "&f/npcbook filter set <filter name> &c- &9Set a filter by the given name.";
    public static final String help_filter_remove = "&f/npcbook filter remove <filter name> [right/left] &c- &9Remove a filter by the given name.";
    public static final String help_filter_getbook = "&f/npcbook filter getbook <filter name> &c- &9Get the book of filter by the given name.";
    public static final String help_actionitem_set = "&f/npcbook actionitem set <filter name> [right/left] &c- &9Put a filter on the item you hold.";
    public static final String help_actionitem_remove = "&f/npcbook actionitem remove [right/left] &c- &9Remove the filter from the item you hold.";
}