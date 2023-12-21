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

public class RemoteStorageSettings extends Section {
    public final StringSetting HOST = new StringSetting("host", "localhost");
    public final PortSetting PORT = new PortSetting("port", 3306);
    public final StringSetting DATABASE = new StringSetting("database", "astralbooks");
    public final StringSetting USERNAME = new StringSetting("username", "root");
    public final StringSetting PASSWORD = new StringSetting("password", "");
    public final BooleanSetting SSL_ENABLED = new BooleanSetting("ssl_enabled", false, List.of("If SSL encryption is enabled"));
    public final StringSetting TABLE_PREFIX = new StringSetting("table_prefix", "abooks_", List.of("The prefix for the table name"));
    public final StringSetting SERVER_NAME = new StringSetting("server_name", "default", List.of("Use this if you have multiple servers using the same database"));

    public RemoteStorageSettings() {
        super("remote");
    }
}
