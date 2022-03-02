/*

    CitizensBooks
    Copyright (c) 2022 @ Drăghiciu 'NicoNekoDev' Nicolae

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */

package ro.nicuch.citizensbooks;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CitizensBooksDatabase {
    private final CitizensBooksPlugin plugin;
    private Connection connection;
    private ExecutorService poolExecutor;
    private String table_prefix;
    private LoadingCache<String, ItemStack> filterBooks;
    private LoadingCache<Pair<Integer, String>, ItemStack> npcBooks;
    private final Set<String> filters = new HashSet<>();
    private final Set<Pair<Integer, String>> savedBooks = new HashSet<>();
    private boolean isMySQL;
    private String serverName;

    public CitizensBooksDatabase(CitizensBooksPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean enableDatabase(Logger logger) {
        try {
            YamlConfiguration settings = this.plugin.getSettings();
            if ("mysql".equalsIgnoreCase(settings.getString("database.type", "json"))) {
                logger.info("Loading MySQL database...");
                String user = settings.getString("database.mysql.user", "user");
                String pass = settings.getString("database.mysql.password", "password");
                String ip = settings.getString("database.mysql.ip", "localhost");
                String port = "3306";
                if (settings.isString("database.mysql.port")) {
                    port = settings.getString("database.mysql.port", "3306");
                } else if (settings.isInt("database.mysql.port")) {
                    port = String.valueOf(settings.getInt("database.mysql.port", 3306));
                }
                String database = settings.getString("database.mysql.name", "database");
                boolean sslEnabled = settings.getBoolean("database.mysql.enable_ssl", false);
                this.table_prefix = settings.getString("database.mysql.table_prefix", "cbooks_");
                this.serverName = settings.getString("database.mysql.server_name", "default");
                this.connection = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + database + "?user=" + user + "&password=" + pass + "&useSSL=" + sslEnabled + "&autoReconnect=true");
                this.isMySQL = true;
                logger.info("Connected to MySQL database!");
            } else if ("sqlite".equalsIgnoreCase(settings.getString("database.type", "json"))) {
                logger.info("Loading SQLite database...");
                String file = settings.getString("database.sqlite.file_name", "storage.db");
                this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.plugin.getDataFolder() + File.separator + file);
                this.isMySQL = false;
                logger.info("Connected to SQLite database!");
            } else
                return false;
            this.connection.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS " + this.table_prefix + "filters (" +
                            "filter_name VARCHAR(255) PRIMARY KEY," +
                            "filter_book TEXT" +
                            ");");
            if (this.plugin.isCitizensEnabled()) {
                if (this.isMySQL) {
                    this.connection.createStatement().executeUpdate(
                            "CREATE TABLE IF NOT EXISTS " + this.table_prefix + "npc_books (" +
                                    "npc_id INT NOT NULL," +
                                    "side VARCHAR(32) NOT NULL DEFAULT 'right_side'," +
                                    "server VARCHAR(255) DEFAULT 'default'," +
                                    "npc_book TEXT," +
                                    "CONSTRAINT npc_id_side PRIMARY KEY (npc_id, side)" +
                                    ");");
                } else {
                    this.connection.createStatement().executeUpdate(
                            "CREATE TABLE IF NOT EXISTS " + this.table_prefix + "npc_books (" +
                                    "npc_id INT NOT NULL," +
                                    "side VARCHAR(32) NOT NULL DEFAULT 'right_side'," +
                                    "server VARCHAR(255) DEFAULT 'default'," +
                                    "npc_book TEXT," +
                                    "PRIMARY KEY (npc_id, side)" +
                                    ");");
                }
            }
            try (ResultSet preload = this.connection.createStatement().executeQuery("SELECT filter_name FROM " + this.table_prefix + "filters;")) {
                while (preload.next()) {
                    this.filters.add(preload.getString("filter_name"));
                }
            }
            if (this.plugin.isCitizensEnabled()) {
                try (ResultSet preload = this.connection.createStatement().executeQuery("SELECT npc_id, side FROM " + this.table_prefix + "npc_books;")) {
                    while (preload.next()) {
                        this.savedBooks.add(Pair.of(preload.getInt("npc_id"), preload.getString("side")));
                    }
                }
            }
            this.poolExecutor = Executors.newFixedThreadPool(this.plugin.getSettings().getInt("database.threads", 2));
            this.filterBooks = CacheBuilder.newBuilder()
                    .expireAfterAccess(5, TimeUnit.MINUTES)
                    .build(new CacheLoader<>() {
                        @Override
                        public @NotNull ItemStack load(@NotNull String key) throws Exception {
                            return CitizensBooksDatabase.this.getFilterBookStack(key).get();
                        }
                    });
            if (this.plugin.isCitizensEnabled()) {
                this.npcBooks = CacheBuilder.newBuilder()
                        .expireAfterAccess(5, TimeUnit.MINUTES)
                        .build(new CacheLoader<>() {
                            @Override
                            public @NotNull ItemStack load(@NotNull Pair<Integer, String> key) throws Exception {
                                return CitizensBooksDatabase.this.getNPCBookStack(key).get();
                            }
                        });
            }
            if (this.filters.isEmpty())
                logger.info("No filter was loaded!");
            else
                logger.info("Loaded " + this.filters.size() + " filters!");
            return true;
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to connect to database!", ex);
            return false;
        }
    }

    public void disableDatabase(Logger logger) {
        logger.info("Disabling database...");
        try {
            this.filters.clear();
            if (this.filterBooks != null)
                this.filterBooks.invalidateAll();
            this.filterBooks = null;
            this.poolExecutor.shutdown();
            this.poolExecutor.awaitTermination(5, TimeUnit.SECONDS);
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (SQLException | InterruptedException ignored) {
        }
    }

    public ItemStack getNPCBook(int npcId, String side, ItemStack def) {
        if (!this.plugin.isCitizensEnabled())
            throw new IllegalStateException("Citizens is not enabled!");
        try {
            return this.npcBooks.get(Pair.of(npcId, side));
        } catch (Exception e) {
            return def;
        }
    }

    public void removeNPCBook(int npcId, String side) {
        if (!this.plugin.isCitizensEnabled())
            throw new IllegalStateException("Citizens is not enabled!");
        this.savedBooks.remove(Pair.of(npcId, side));
        this.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("DELETE FROM " + this.table_prefix + "npc_books WHERE npc_id=? AND side=? AND server=?;")) {
                statement.setInt(1, npcId);
                statement.setString(2, side);
                statement.setString(3, this.serverName);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to remove book data!", ex);
            }
        });
    }


    protected Future<ItemStack> getNPCBookStack(Pair<Integer, String> key) {
        return this.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("SELECT npc_book FROM " + this.table_prefix + "npc_books WHERE npc_id=? AND side=? AND server=?;")) {
                statement.setInt(1, key.getFirstValue());
                statement.setString(2, key.getSecondValue());
                statement.setString(3, this.serverName);
                try (ResultSet result = statement.executeQuery()) {
                    return this.plugin.getAPI().decodeItemStack(result.getString("npc_book"));
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to retrieve book data!", ex);
                return null;
            }
        });
    }

    public void putNPCBook(int npcId, String side, ItemStack book) {
        if (!this.plugin.isCitizensEnabled())
            throw new IllegalStateException("Citizens is not enabled!");
        Pair<Integer, String> pairKey = Pair.of(npcId, side);
        this.savedBooks.add(pairKey);
        this.npcBooks.put(pairKey, book);
        this.poolExecutor.submit(() -> {
            String query = this.isMySQL ?
                    "INSERT INTO " + this.table_prefix + "npc_books (npc_id, side, server, npc_book) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE npc_book=?;" :
                    "INSERT INTO " + this.table_prefix + "npc_books (npc_id, side, server, npc_book) VALUES(?, ?, ?, ?) ON CONFLICT(npc_id, side) DO UPDATE SET npc_book=?;";
            try (PreparedStatement statement = this.connection.prepareStatement(query)) {
                String encoded = this.plugin.getAPI().encodeItemStack(book);
                statement.setInt(1, npcId);
                statement.setString(2, side);
                statement.setString(3, this.serverName);
                statement.setString(4, encoded);
                statement.setString(5, encoded);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to save book data!", ex);
            }
        });
    }

    public boolean hasNPCBook(int npcId, String side) {
        return this.savedBooks.contains(Pair.of(npcId, side));
    }

    public boolean hasFilterBook(String filterName) {
        return this.filters.contains(filterName);
    }

    public ItemStack getFilterBook(String filterName, ItemStack def) {
        try {
            return this.filterBooks.get(filterName);
        } catch (Exception e) {
            return def;
        }
    }

    protected Future<ItemStack> getFilterBookStack(String filterName) {
        return this.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("SELECT filter_book FROM " + this.table_prefix + "filters WHERE filter_name=?;")) {
                statement.setString(1, filterName);
                try (ResultSet result = statement.executeQuery()) {
                    return this.plugin.getAPI().decodeItemStack(result.getString("filter_book"));
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to retrieve book data!", ex);
                return null;
            }
        });
    }

    public void putFilterBook(String filterName, ItemStack book) {
        this.filters.add(filterName);
        this.filterBooks.put(filterName, book);
        this.poolExecutor.submit(() -> {
            String query = this.isMySQL ?
                    "INSERT INTO " + this.table_prefix + "filters (filter_name, filter_book) VALUES(?, ?) ON DUPLICATE KEY UPDATE filter_book=?;" :
                    "INSERT INTO " + this.table_prefix + "filters (filter_name, filter_book) VALUES(?, ?) ON CONFLICT(filter_name) DO UPDATE SET filter_book=?;";
            try (PreparedStatement statement = this.connection.prepareStatement(query)) {
                String encoded = this.plugin.getAPI().encodeItemStack(book);
                statement.setString(1, filterName);
                statement.setString(2, encoded);
                statement.setString(3, encoded);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to save book data!", ex);
            }
        });
    }

    public void removeFilterBook(String filterName) {
        this.filters.remove(filterName);
        this.filterBooks.invalidate(filterName);
        this.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("DELETE FROM " + this.table_prefix + "filters WHERE filter_name=?;")) {
                statement.setString(1, filterName);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to remove book data!", ex);
            }
        });
    }

    public Set<String> getFilterNames() {
        return this.filters;
    }
}
