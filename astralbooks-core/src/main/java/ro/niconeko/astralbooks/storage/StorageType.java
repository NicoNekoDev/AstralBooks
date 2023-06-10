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

package ro.niconeko.astralbooks.storage;

public enum StorageType {
    JSON("json", "Json"),
    MYSQL("mysql", "MySQL"),
    SQLITE("sqlite", "SQLite"),
    H2("h2", "H2"),
    MARIADB("mariadb", "MariaDB");

    private final String type;
    private final String formattedName;

    StorageType(String type, String formattedName) {
        this.type = type;
        this.formattedName = formattedName;
    }

    @Override
    public String toString() {
        return this.type;
    }

    public String getFormattedName() {
        return formattedName;
    }

    public static StorageType fromString(String type) {
        if (type.equalsIgnoreCase("mysql"))
            return MYSQL;
        else if (type.equalsIgnoreCase("sqlite"))
            return SQLITE;
        else if (type.equalsIgnoreCase("json"))
            return JSON;
        else if (type.equalsIgnoreCase("mariadb"))
            return MARIADB;
        return H2;
    }
}
