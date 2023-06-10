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

package ro.niconeko.astralbooks.settings;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.utils.Message;

public class MessageSettings extends Settings {
    private ConfigurationSection section = new YamlConfiguration();

    public MessageSettings(AstralBooksPlugin plugin) {
        super(plugin);
    }

    @Override
    public void load(ConfigurationSection section) {
        this.section = section;
        for (Message msg : Message.values())
            super.getOrSetStringFunction(section, msg.getPath(), msg.getDefault(), msg.getComments());
    }

    public String getMessage(Message msg) {
        return super.parseMessage(this.section.getString(Message.HEADER.getPath(), Message.HEADER.getDefault()))
                + super.parseMessage(this.section.getString(msg.getPath(), msg.getDefault()));
    }

    public String getMessageNoHeader(Message msg) {
        return super.parseMessage(this.section.getString(msg.getPath(), msg.getPath()));
    }
}
