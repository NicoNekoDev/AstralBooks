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

package ro.niconeko.astralbooks.storage.settings;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.settings.Settings;

import java.util.List;
import java.util.Optional;

@Getter
public class StorageEmbedSettings extends Settings {
    private String fileName = "database";
    private boolean EncryptionEnabled = false;
    private int saveInterval = 60;

    public StorageEmbedSettings(AstralBooksPlugin plugin) {
        super(plugin);
    }

    @Override
    public void load(ConfigurationSection section) {
        this.fileName = super.getOrSetStringFunction(section, "file_name", this.fileName);
        this.EncryptionEnabled = super.getOrSetBooleanFunction(section, "encryption_enabled", this.EncryptionEnabled,
                Optional.of(List.of(
                        "Enable AES encryption.",
                        "Don't change if you don't know what you're doing!",
                        "Only for H2 database."
                )));
        this.saveInterval = super.getOrSetIntFunction(section, "save_interval", this.saveInterval, Optional.of(List.of("Only for Json database.")));
    }
}
