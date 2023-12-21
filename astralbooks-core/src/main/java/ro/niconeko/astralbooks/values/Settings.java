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

package ro.niconeko.astralbooks.values;

import ro.niconeko.astralbooks.storage.settings.StorageSettings;
import ro.niconeko.astralbooks.utils.settings.*;

import java.util.List;

public class Settings {
    public static final BooleanSetting METRICS_ENABLED = new BooleanSetting("metrics", true, List.of(
            "Metrics can be viewed at https://bstats.org/plugin/bukkit/AstralBooks/18026",
            "It requires server restart to take effect!"
    ));
    public static final BooleanSetting UPDATE_CHECK = new BooleanSetting("update_check", true);
    public static final BooleanSetting JOIN_BOOK_ENABLED = new BooleanSetting("join_book_enabled", false);
    public static final BooleanSetting JOIN_BOOK_ALWAYS_SHOW = new BooleanSetting("join_book_always_show", false);
    public static final BooleanSetting JOIN_BOOK_DELAY_ENABLED = new BooleanSetting("join_book_delay_enabled", false);
    public static final IntegerSetting JOIN_BOOK_DELAY = new IntegerSetting("join_book_delay", 0);
    public static final BooleanSetting SIGN_BOOK_SECURITY_ENABLED = new BooleanSetting("sign_book_security_enabled", false);
    public static final SectionSetting<StorageSettings> STORAGE = new SectionSetting<>(new StorageSettings());
}