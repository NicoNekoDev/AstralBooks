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

package ro.niconeko.astralbooks.utils.settings;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import ro.niconeko.astralbooks.utils.MessageUtils;

import java.util.List;
import java.util.function.Function;

public class MessageSetting extends Setting<List<String>> {

    public MessageSetting(String key, List<String> defaultValue, List<String> comments, List<String> inlineComments) {
        super(key, defaultValue, comments, inlineComments);
    }

    public MessageSetting(String key, List<String> defaultValue, List<String> comments) {
        super(key, defaultValue, comments, List.of());
    }

    public MessageSetting(String key, List<String> defaultValue) {
        super(key, defaultValue, List.of(), List.of());
    }

    @Override
    public final MessageSetting load(ConfigurationSection section) {
        if (section.isString(super.key))
            super.value = List.of(section.getString(super.key));
        else if (section.isList(super.key))
            super.value = section.getStringList(super.key);
        else this.setup(section);
        return this;
    }

    @Override
    protected void setup(ConfigurationSection section) {
        if (this.defaultValue.size() > 1)
            section.set(this.key, this.defaultValue);
        else
            section.set(this.key, this.defaultValue.get(0));
        if (!this.comments.isEmpty()) section.setComments(this.key, this.comments);
        if (!this.inlineComments.isEmpty()) section.setInlineComments(this.key, this.inlineComments);
        this.value = this.defaultValue;
    }

    public int send(CommandSender sender) {
        return this.send(sender, Function.identity());
    }

    public int send(CommandSender sender, Function<String, String> replacer) {
        for (String message : super.value) {
            message = replacer.apply(message);
            if (message != null && !message.isEmpty())
                MessageUtils.sendMessage(sender, message);
        }
        return super.value.size();
    }
}
