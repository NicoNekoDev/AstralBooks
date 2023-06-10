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
import ro.niconeko.astralbooks.storage.StorageType;

import java.util.List;
import java.util.Optional;

public class StorageSettings extends Settings {
    @Getter private StorageType databaseType = StorageType.H2;
    @Getter private int databaseThreads = 2;
    @Getter private boolean securityBookPurgeEnabled = true;
    @Getter private int securityBookPurgeOlderThan = 30;
    @Getter private final StorageRemoteSettings RemoteSettings = new StorageRemoteSettings(super.plugin);
    @Getter private final StorageEmbedSettings EmbedSettings = new StorageEmbedSettings(super.plugin);

    public StorageSettings(AstralBooksPlugin plugin) {
        super(plugin);
    }

    @Override
    public void load(ConfigurationSection section) {
        this.databaseType = StorageType.fromString(super.getOrSetStringFunction(section, "type", this.databaseType.toString(), Optional.of(List.of("Options: h2, sqlite, mysql, json"))));
        this.databaseThreads = super.getOrSetIntFunction(section, "threads", this.databaseThreads, Optional.of(List.of("Number of threads the cache will use")));
        this.securityBookPurgeEnabled = super.getOrSetBooleanFunction(section, "security_books_purge_enabled", this.securityBookPurgeEnabled, Optional.of(List.of(
                "Enable if you want to clean old saved books created by players"
        )));
        this.securityBookPurgeOlderThan = super.getOrSetIntFunction(section, "security_books_purge_older_than", this.securityBookPurgeOlderThan, Optional.of(List.of(
                "In days, default: 30"
        )));
        this.RemoteSettings.load(super.getOrCreateSection(section, "remote"));
        this.EmbedSettings.load(super.getOrCreateSection(section, "embed"));
    }
}
