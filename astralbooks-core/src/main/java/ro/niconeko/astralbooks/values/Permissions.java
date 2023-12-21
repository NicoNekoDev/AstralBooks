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

import org.bukkit.command.CommandSender;
import ro.niconeko.astralbooks.managers.HooksManager;
import ro.niconeko.astralbooks.utils.Permission;

public class Permissions {
    public final static Permission COMMAND_HELP = new Permission("astralbooks.command.help");
    public final static Permission COMMAND_SECURITY = new Permission("astralbooks.command.security");
    public final static Permission COMMAND_SECURITY_LIST = new Permission("astralbooks.command.security.list");
    public final static Permission COMMAND_SECURITY_GETBOOK = new Permission("astralbooks.command.security.getbook");
    public final static Permission COMMAND_INTERACTION = new Permission("astralbooks.command.interaction");
    public final static Permission COMMAND_INTERACTION_SET = new Permission("astralbooks.command.interaction.set");
    public final static Permission COMMAND_INTERACTION_SET_BLOCK = new Permission("astralbooks.command.interaction.set.block");
    public final static Permission COMMAND_INTERACTION_SET_ENTITY = new Permission("astralbooks.command.interaction.set.entity");
    public final static Permission COMMAND_INTERACTION_REMOVE = new Permission("astralbooks.command.interaction.remove");
    public final static Permission COMMAND_INTERACTION_REMOVE_BLOCK = new Permission("astralbooks.command.interaction.remove.block");
    public final static Permission COMMAND_INTERACTION_REMOVE_ENTITY = new Permission("astralbooks.command.interaction.remove.entity");
    public final static Permission COMMAND_NPC = new Permission("astralbooks.command.npc");
    public final static Permission COMMAND_NPC_SET = new Permission("astralbooks.command.npc.set");
    public final static Permission COMMAND_NPC_REMOVE = new Permission("astralbooks.command.npc.remove");
    public final static Permission COMMAND_NPC_GETBOOK = new Permission("astralbooks.command.npc.getbook");
    public final static Permission COMMAND_ACTIONITEM = new Permission("astralbooks.command.actionitem");
    public final static Permission COMMAND_ACTIONITEM_SET = new Permission("astralbooks.command.actionitem.set");
    public final static Permission COMMAND_ACTIONITEM_REMOVE = new Permission("astralbooks.command.actionitem.remove");
    public final static Permission COMMAND_FORCEOPEN = new Permission("astralbooks.command.forceopen");
    public final static Permission COMMAND_RELOAD = new Permission("astralbooks.command.reload");
    public final static Permission NO_JOIN_BOOK = new Permission("astralbooks.nojoinbook");
    public final static Permission NOTIFY = new Permission("astralbooks.notify");
    public static final Permission COMMAND = new Permission("astralbooks.command");

    public static boolean has(CommandSender sender, String permission) {
        if (HooksManager.PERMISSIONS.isEnabled()) {
            return HooksManager.PERMISSIONS.hasPermission(sender, permission);
        }
        return sender.hasPermission(permission);
    }

}
