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

import ro.niconeko.astralbooks.storage.StorageType;
import ro.niconeko.astralbooks.utils.Section;
import ro.niconeko.astralbooks.utils.settings.*;

public class StorageSettings extends Section {
    public final StorageTypeSetting TYPE = new StorageTypeSetting("type", StorageType.H2);
    public final IntegerSetting THREADS = new IntegerSetting("threads", 2);
    public final BooleanSetting SECURITY_BOOK_PURGE_ENABLED = new BooleanSetting("security_book_purge_enabled", true);
    public final IntegerSetting SECURITY_BOOK_PURGE_OLDER_THAN = new IntegerSetting("security_book_purge_older_than", 30);
    public final SectionSetting<RemoteStorageSettings> REMOTE = new SectionSetting<>(new RemoteStorageSettings());
    public final SectionSetting<EmbedStorageSettings> EMBED = new SectionSetting<>(new EmbedStorageSettings());

    public StorageSettings() {
        super("storage");
    }
}
