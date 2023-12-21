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
import ro.niconeko.astralbooks.storage.StorageType;

import java.util.List;

public class StorageTypeSetting extends Setting<StorageType> {

    public StorageTypeSetting(String key, StorageType defaultValue, List<String> comments, List<String> inlineComments) {
        super(key, defaultValue, comments, inlineComments);
    }

    public StorageTypeSetting(String key, StorageType defaultValue, List<String> comments) {
        super(key, defaultValue, comments, List.of());
    }

    public StorageTypeSetting(String key, StorageType defaultValue) {
        super(key, defaultValue, List.of(), List.of());
    }

    @Override
    public final StorageTypeSetting load(ConfigurationSection section) {
        if (section.isString(super.key))
            super.value = StorageType.fromString(section.getString(super.key));
        else {
            super.value = this.defaultValue;
            section.set(super.key, this.defaultValue.toString());
            if (!this.comments.isEmpty()) section.setComments(super.key, this.comments);
            if (!this.inlineComments.isEmpty()) section.setInlineComments(super.key, this.inlineComments);
        }
        return this;
    }

    @Override
    protected void setup(ConfigurationSection section) {
        section.set(this.key, this.defaultValue.toString());
        if (!this.comments.isEmpty()) section.setComments(this.key, this.comments);
        if (!this.inlineComments.isEmpty()) section.setInlineComments(this.key, this.inlineComments);
        this.value = this.defaultValue;
    }
}
