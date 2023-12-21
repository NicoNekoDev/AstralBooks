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

import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import ro.niconeko.astralbooks.utils.Section;

import java.lang.reflect.Field;

public class SectionSetting<T extends Section> extends Setting<T> {

    public SectionSetting(T section) {
        super(section.getName(), section, section.getComments(), section.getInlineComments());
    }

    @SneakyThrows
    @Override
    public SectionSetting<T> load(ConfigurationSection section) {
        super.value = super.defaultValue;
        ConfigurationSection upper;
        if (section.isConfigurationSection(super.key))
            upper = section.getConfigurationSection(super.key);
        else {
            upper = section.createSection(super.key);
            upper.setComments(super.key, super.value.getComments());
            upper.setInlineComments(super.key, super.value.getInlineComments());
        }
        this.loadValues(upper);
        return this;
    }

    private void loadValues(ConfigurationSection section) throws IllegalAccessException {
        for (Field field : super.value.getClass().getDeclaredFields()) {
            if (field.get(super.value) instanceof Setting<?> setting)
                setting.load(section);
        }
    }
}
