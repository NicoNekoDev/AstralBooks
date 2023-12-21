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

import ro.niconeko.astralbooks.utils.Section;
import ro.niconeko.astralbooks.utils.settings.*;

import java.util.List;

public class EmbedStorageSettings extends Section {
    public final StringSetting FILE_NAME = new StringSetting("file_name", "localhost");
    public final BooleanSetting ENCRYPTION_ENABLED = new BooleanSetting("encryption_enabled", false,
            List.of(
                    "Enable AES encryption.",
                    "Don't change if you don't know what you're doing!",
                    "Only for H2 database."
            ));
    public final IntegerSetting SAVE_INTERVAL = new IntegerSetting("save_interval", 60, List.of("Only for Json database."));

    public EmbedStorageSettings() {
        super("embed");
    }
}
