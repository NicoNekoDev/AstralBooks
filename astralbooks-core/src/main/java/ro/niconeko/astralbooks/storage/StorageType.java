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

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public enum StorageType {
    JSON("Json"),
    MYSQL("MySQL"),
    SQLITE("SQLite"),
    H2("H2"),
    MARIADB("MariaDB");

    private final String formattedName;

    StorageType(String formattedName) {
        this.formattedName = formattedName;
    }

    @NotNull
    @Override
    public String toString() {
        return switch (this) {
            case H2 -> "h2";
            case MYSQL -> "mysql";
            case JSON -> "json";
            case SQLITE -> "sqlite";
            case MARIADB -> "mariadb";
        };
    }

    public static StorageType fromString(String type) {
        return switch (type.toUpperCase()) {
            case "MARIADB" -> MARIADB;
            case "MYSQL" -> MYSQL;
            case "SQLITE" -> SQLITE;
            case "JSON" -> JSON;
            default -> H2;
        };
    }
}
