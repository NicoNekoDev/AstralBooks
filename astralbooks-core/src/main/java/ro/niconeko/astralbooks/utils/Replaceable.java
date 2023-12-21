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

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandListenerWrapper;

import java.util.function.Function;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;

public class Replaceable implements Function<String, String> {
    public final CommandContext<CommandListenerWrapper> context;
    private Function<String, String> func;

    public Replaceable(CommandContext<CommandListenerWrapper> context, String str, String value) {
        this.context = context;
        this.func = s -> s.replace(str, getString(this.context, value));
    }

    public Replaceable replace(String str, String value) {
        this.func = this.func.andThen(s -> s.replace(str, getString(this.context, value)));
        return this;
    }

    @Override
    public String apply(String message) {
        return this.func.apply(message);
    }
}
