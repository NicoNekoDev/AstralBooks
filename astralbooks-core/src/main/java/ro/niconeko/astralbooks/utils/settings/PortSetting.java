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

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public class PortSetting extends Setting<Integer> {

    public PortSetting(String key, String defaultValue, List<String> comments, List<String> inlineComments) {
        super(key, Integer.parseInt(defaultValue), comments, inlineComments);
    }

    public PortSetting(String key, String defaultValue, List<String> comments) {
        super(key, Integer.parseInt(defaultValue), comments, List.of());
    }

    public PortSetting(String key, String defaultValue) {
        super(key, Integer.parseInt(defaultValue), List.of(), List.of());
    }

    public PortSetting(String key, int defaultValue, List<String> comments, List<String> inlineComments) {
        super(key, defaultValue, comments, inlineComments);
    }

    public PortSetting(String key, int defaultValue, List<String> comments) {
        super(key, defaultValue, comments, List.of());
    }

    public PortSetting(String key, int defaultValue) {
        super(key, defaultValue, List.of(), List.of());
    }

    @Override
    public final PortSetting load(ConfigurationSection section) {
        if (section.isInt(super.key))
            super.value = section.getInt(super.key);
        else if (section.isString(super.key))
            super.value = Integer.parseInt(section.getString(super.key));
        else super.setup(section);
        return this;
    }
}
