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

package ro.niconeko.astralbooks.commands.subs;

import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.commands.AstralCommand;
import ro.niconeko.astralbooks.values.Messages;
import ro.niconeko.astralbooks.values.Permissions;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;

public class HelpCommand extends AstralCommand {
    public HelpCommand(AstralBooksPlugin plugin) {
        super(plugin, "help", Permissions.COMMAND_HELP);
        super.then(super.integerArgument("page").suggests(super.integerRange(1, Messages.COMMAND_HELP_LIST.get().size())).executes(context -> {
                    int page = getInteger(context, "page");
                    if (page < 1 || page > Messages.COMMAND_HELP_LIST.get().size())
                        return Messages.COMMAND_HELP_INVALID_PAGE.send(getSender(context), replaceable(context, "%page%", page + ""));
                    return Messages.COMMAND_HELP_LIST.send(getSender(context), page - 1, replaceable(context, "%page%", page + ""));
                }))
                .then(super.greedyArgument("unknown").executes(context -> Messages.COMMAND_MAIN_UNKNOWN.send(getSender(context), replaceable(context, "%command%", "unknown"))));
        super.executes(context -> Messages.COMMAND_HELP_USAGE.send(getSender(context)));
    }
}
