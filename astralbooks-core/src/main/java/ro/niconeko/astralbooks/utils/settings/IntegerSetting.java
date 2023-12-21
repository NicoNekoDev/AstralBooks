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

public class IntegerSetting extends Setting<Integer> {

    public IntegerSetting(String value, Integer defaultValue, List<String> comments, List<String> inlineComments) {
        super(value, defaultValue, comments, inlineComments);
    }

    public IntegerSetting(String value, Integer defaultValue, List<String> comments) {
        super(value, defaultValue, comments, List.of());
    }

    public IntegerSetting(String value, Integer defaultValue) {
        super(value, defaultValue, List.of(), List.of());
    }

    @Override
    public final IntegerSetting load(ConfigurationSection section) {
        if (section.isBoolean(super.key))
            super.value = section.getInt(super.key);
        else super.setup(section);
        return this;
    }
}
