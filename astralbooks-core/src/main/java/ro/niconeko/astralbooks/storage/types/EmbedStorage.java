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

package ro.niconeko.astralbooks.storage.types;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.Storage;
import ro.niconeko.astralbooks.storage.StorageType;
import ro.niconeko.astralbooks.storage.settings.StorageEmbedSettings;
import ro.niconeko.astralbooks.storage.settings.StorageSettings;
import ro.niconeko.astralbooks.utils.Side;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public abstract class EmbedStorage extends Storage {
    protected String fileName;
    protected boolean encryptionEnabled;
    protected int autoSaveInterval;

    protected EmbedStorage(AstralBooksPlugin plugin, StorageType storageType) {
        super(plugin, storageType);
    }

    @Override
    protected void loadSettings(StorageSettings settings) {
        StorageEmbedSettings embedSettings = settings.getEmbedSettings();
        this.fileName = embedSettings.getFileName();
        this.encryptionEnabled = embedSettings.isEncryptionEnabled();
        this.autoSaveInterval = embedSettings.getSaveInterval();
        super.purgeSecurityBooksOlderThan = settings.getSecurityBookPurgeOlderThan();
    }

    @Override
    protected boolean preloadCache() {
        try (PreparedStatement statement = super.connection.prepareStatement(
                "SELECT filter_name FROM 'filters';"
        )) {
            try (ResultSet preload = statement.executeQuery()) {
                while (preload.next()) {
                    super.cache.filters.add(preload.getString("filter_name"));
                }
            }
        } catch (SQLException ex) {
            super.plugin.getLogger().log(Level.SEVERE, "(" + super.storageType.getFormattedName() + ") Failed to preload 'filters' table!", ex);
            return false;
        }
        try (PreparedStatement statement = super.connection.prepareStatement(
                "SELECT command_name FROM 'commands';"
        )) {
            try (ResultSet preload = statement.executeQuery()) {
                while (preload.next()) {
                    super.cache.commands.add(preload.getString("command_name"));
                }
            }
        } catch (SQLException ex) {
            super.plugin.getLogger().log(Level.SEVERE, "(" + super.storageType.getFormattedName() + ") Failed to preload 'commands' table!", ex);
            return false;
        }
        try (PreparedStatement statement = super.connection.prepareStatement(
                "SELECT npc_id, side FROM 'npc_books';"
        )) {
            try (ResultSet preload = statement.executeQuery()) {
                while (preload.next()) {
                    super.cache.npcs.add(Pair.of(preload.getInt("npc_id"), Side.fromString(preload.getString("side"))));
                }
            }
        } catch (SQLException ex) {
            super.plugin.getLogger().log(Level.SEVERE, "(" + super.storageType.getFormattedName() + ") Failed to preload 'npc_books' table!", ex);
            return false;
        }
        return true;
    }

    @Override
    protected boolean createTables() {
        try (PreparedStatement statement = super.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS filters (
                filter_name VARCHAR(256),
                filter_book TEXT,
                PRIMARY KEY (filter_name)
                );
                """
        )) {
            statement.execute();
        } catch (SQLException ex) {
            super.plugin.getLogger().log(Level.SEVERE, "(" + super.storageType.getFormattedName() + ") Failed to create 'filters' table!", ex);
            return false;
        }
        try (PreparedStatement statement = super.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS commands (
                command_name VARCHAR(256),
                filter_name VARCHAR(256),
                permission VARCHAR(255),
                PRIMARY KEY (command_name)
                );
                """
        )) {
            statement.execute();
        } catch (SQLException ex) {
            super.plugin.getLogger().log(Level.SEVERE, "(" + super.storageType.getFormattedName() + ") Failed to create 'commands' table!", ex);
            return false;
        }
        try (PreparedStatement statement = super.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS npc_books (
                npc_id INT NOT NULL,
                side VARCHAR(32) NOT NULL DEFAULT 'right_side',
                npc_book TEXT,
                CONSTRAINT npc_id_side PRIMARY KEY (npc_id, side)
                );
                """
        )) {
            statement.execute();
        } catch (SQLException ex) {
            super.plugin.getLogger().log(Level.SEVERE, "(" + super.storageType.getFormattedName() + ") Failed to create 'npcbooks' table!", ex);
            return false;
        }
        try (PreparedStatement statement = super.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS security_books (
                book_hash VARCHAR(256),
                book TEXT,
                PRIMARY KEY (book_hash)
                );
                """
        )) {
            statement.execute();
        } catch (SQLException ex) {
            super.plugin.getLogger().log(Level.SEVERE, "(" + super.storageType.getFormattedName() + ") Failed to create 'security_books' table!", ex);
            return false;
        }
        try (PreparedStatement statement = super.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS security_players (
                player VARCHAR(48) NOT NULL,
                timestamp BIGINT NOT NULL,
                book_hash VARCHAR(256) NOT NULL,
                CONSTRAINT player_date PRIMARY KEY (player, timestamp)
                );
                """
        )) {
            statement.execute();
        } catch (SQLException ex) {
            super.plugin.getLogger().log(Level.SEVERE, "(" + super.storageType.getFormattedName() + ") Failed to create 'security_players' table!", ex);
            return false;
        }
        return true;
    }
}
