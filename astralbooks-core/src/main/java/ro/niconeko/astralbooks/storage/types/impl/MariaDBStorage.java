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

package ro.niconeko.astralbooks.storage.types.impl;

import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.StorageType;
import ro.niconeko.astralbooks.storage.types.RemoteStorage;

public class MariaDBStorage extends RemoteStorage {
    public MariaDBStorage(AstralBooksPlugin plugin) {
        super(plugin, StorageType.MARIADB);
    }

    @Override
    protected String getDriver() {
        return "org.mariadb.jdbc.Driver";
    }

    @Override
    protected String getURL() {
        return "jdbc:mariadb://"
                + super.host
                + ":" + super.port
                + "/" + super.database
                + "?user=" + super.user
                + "&password=" + super.pass
                + "&useSSL=" + super.sslEnabled
                + "&autoReconnect=true";
    }
}
