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

import com.google.common.hash.Hashing;
import org.bukkit.inventory.ItemStack;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.StorageType;
import ro.niconeko.astralbooks.utils.tuples.PairTuple;
import ro.niconeko.astralbooks.utils.tuples.TripletTuple;
import ro.niconeko.astralbooks.storage.types.EmbedStorage;
import ro.niconeko.astralbooks.utils.Side;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class H2Storage extends EmbedStorage {

    public H2Storage(AstralBooksPlugin plugin) {
        super(plugin, StorageType.H2);
    }

    @Override
    protected String getDriver() {
        return "org.h2.Driver";
    }

    @Override
    protected String getURL() {
        return "jdbc:h2:./"
                + super.plugin.getDataFolder() + File.separator + super.fileName
                + (super.encryptionEnabled ? ";CIPHER=AES" : "") + ";mode=MySQL";
    }

    @Override
    protected boolean preloadCache() {
        try (PreparedStatement statement = super.connection.prepareStatement(
                "SELECT filter_name FROM filters;"
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
                "SELECT command_name FROM commands;"
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
                "SELECT npc_id, side FROM npc_books;"
        )) {
            try (ResultSet preload = statement.executeQuery()) {
                while (preload.next()) {
                    super.cache.npcs.add(new PairTuple<>(preload.getInt("npc_id"), Side.fromString(preload.getString("side"))));
                }
            }
        } catch (SQLException ex) {
            super.plugin.getLogger().log(Level.SEVERE, "(" + super.storageType.getFormattedName() + ") Failed to preload 'npc_books' table!", ex);
            return false;
        }
        return true;
    }

    @Override
    protected Future<ItemStack> getFilterBookStack(String filterName) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = super.connection.prepareStatement(
                    "SELECT filter_book FROM filters WHERE filter_name=?;"
            )) {
                statement.setString(1, filterName);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next())
                        return super.plugin.getAPI().decodeItemStack(result.getString("filter_book"));
                    return null;
                }
            } catch (SQLException ex) {
                super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to retrieve filter book data!", ex);
                return null;
            }
        });
    }

    @Override
    protected Future<ItemStack> getNPCBookStack(int npcId, Side side) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = super.connection.prepareStatement(
                    "SELECT npc_book FROM npc_books WHERE npc_id=? AND side=?;"
            )) {
                statement.setInt(1, npcId);
                statement.setString(2, side.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next())
                        return super.plugin.getAPI().decodeItemStack(result.getString("npc_book"));
                    return null;
                }
            } catch (SQLException ex) {
                super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to retrieve book data!", ex);
                return null;
            }
        });
    }

    @Override
    protected Future<PairTuple<String, String>> getCommandFilterStack(String cmd) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = super.connection.prepareStatement(
                    "SELECT filter_name, permission FROM commands WHERE command_name=?;"
            )) {
                statement.setString(1, cmd);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next())
                        return new PairTuple<>(result.getString("filter_name"), result.getString("permission"));
                    return null;
                }
            } catch (SQLException ex) {
                super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to retrieve command data!", ex);
                return null;
            }
        });
    }

    @Override
    protected void removeNPCBookStack(int npcId, Side side) {
        super.cache.npcs.remove(new PairTuple<>(npcId, side));
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = super.connection.prepareStatement(
                    "DELETE FROM npc_books WHERE npc_id=? AND side=?;"
            )) {
                statement.setInt(1, npcId);
                statement.setString(2, side.toString());
                statement.executeUpdate();
            } catch (SQLException ex) {
                super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to remove book data!", ex);
            }
        });
    }

    @Override
    protected void removeFilterBookStack(String filterName) {
        super.cache.filters.remove(filterName);
        super.cache.filterBooks.invalidate(filterName);
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = super.connection.prepareStatement(
                    "DELETE FROM filters WHERE filter_name=?;"
            )) {
                statement.setString(1, filterName);
                statement.executeUpdate();
            } catch (SQLException ex) {
                super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to remove book data!", ex);
            }
        });
    }

    @Override
    protected void removeCommandFilterStack(String cmd) {
        super.cache.commands.remove(cmd);
        super.cache.commandFilters.invalidate(cmd);
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = super.connection.prepareStatement(
                    "DELETE FROM commands WHERE command_name=?;"
            )) {
                statement.setString(1, cmd);
                statement.executeUpdate();
            } catch (SQLException ex) {
                super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to remove command data!", ex);
            }
        });
    }

    @Override
    protected void putNPCBookStack(int npcId, Side side, ItemStack book) {
        PairTuple<Integer, Side> pairKey = new PairTuple<>(npcId, side);
        super.cache.npcs.add(pairKey);
        super.cache.npcBooks.put(pairKey, book);
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = super.connection.prepareStatement(
                    "INSERT INTO npc_books (npc_id, side, npc_book) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE npc_book=?;"
            )) {
                String encoded = super.plugin.getAPI().encodeItemStack(book);
                statement.setInt(1, npcId);
                statement.setString(2, side.toString());
                statement.setString(4, encoded);
                statement.setString(5, encoded);
                statement.executeUpdate();
            } catch (SQLException ex) {
                super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to save book data!", ex);
            }
        });
    }

    @Override
    protected void putFilterBookStack(String filterName, ItemStack book) {
        super.cache.filters.add(filterName);
        super.cache.filterBooks.put(filterName, book);
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = super.connection.prepareStatement(
                    "INSERT INTO filters (filter_name, filter_book) VALUES(?, ?) ON DUPLICATE KEY UPDATE filter_book=?;"
            )) {
                String encoded = super.plugin.getAPI().encodeItemStack(book);
                statement.setString(1, filterName);
                statement.setString(2, encoded);
                statement.setString(3, encoded);
                statement.executeUpdate();
            } catch (SQLException ex) {
                super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to save book data!", ex);
            }
        });
    }

    @Override
    protected void putCommandFilterStack(String cmd, String filterName, String permission) {
        super.cache.commands.add(cmd);
        super.cache.commandFilters.put(cmd, new PairTuple<>(filterName, permission));
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = super.connection.prepareStatement(
                    "INSERT INTO commands (command_name, filter_name, permission) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE filter_name=?, permission=?;"
            )) {
                statement.setString(1, cmd);
                statement.setString(2, filterName);
                statement.setString(3, permission);
                statement.setString(4, filterName);
                statement.setString(5, permission);
                statement.executeUpdate();
            } catch (SQLException ex) {
                super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to save command data!", ex);
            }
        });
    }

    @Override
    protected Future<LinkedList<PairTuple<Date, ItemStack>>> getAllBookSecurityStack(UUID uuid, int page, int amount) {
        return super.cache.poolExecutor.submit(() -> {
            LinkedList<PairTuple<Date, ItemStack>> list = new LinkedList<>();
            String query = page > -1 ? """
                    SELECT
                    security_players.timestamp,
                    security_books.book
                    FROM security_books, security_players
                    WHERE security_books.book_hash = security_players.book_hash
                    AND security_players.player = ?
                    ORDER BY security_players.timestamp DESC LIMIT ? OFFSET ?;
                    """ : """
                    SELECT
                    security_players.timestamp,
                    security_books.book
                    FROM security_books, security_players
                    WHERE security_books.book_hash = security_players.book_hash
                    AND security_players.player = ?
                    ORDER BY security_players.timestamp DESC;
                    """;
            try (PreparedStatement statement = super.connection.prepareStatement(query)) {
                statement.setString(1, uuid.toString());
                if (page > -1) {
                    statement.setInt(2, amount);
                    statement.setInt(3, page * amount);
                }
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        Date date = new Date(result.getLong(1));
                        ItemStack book = super.plugin.getAPI().decodeItemStack(result.getString(2));
                        list.add(new PairTuple<>(date, book));
                    }
                    return list;
                }
            } catch (SQLException ex) {
                super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to retrieve book security data!", ex);
                return list;
            }
        });
    }

    @Override
    protected Future<LinkedList<TripletTuple<UUID, Date, ItemStack>>> getAllBookSecurityStack(int page, int amount) {
        return super.cache.poolExecutor.submit(() -> {
            LinkedList<TripletTuple<UUID, Date, ItemStack>> list = new LinkedList<>();
            String query = page > -1 ? """
                    SELECT
                    security_players.player,
                    security_players.timestamp,
                    security_books.book
                    FROM security_books, security_players
                    WHERE security_books.book_hash = security_players.book_hash
                    ORDER BY security_players.timestamp DESC LIMIT ? OFFSET ?;
                    """ : """
                    SELECT
                    security_players.player,
                    security_players.timestamp,
                    security_books.book
                    FROM security_books, security_players
                    WHERE security_books.book_hash = security_players.book_hash
                    ORDER BY security_players.timestamp DESC;
                    """;
            try (PreparedStatement statement = super.connection.prepareStatement(query)) {
                if (page > -1) {
                    statement.setInt(1, amount);
                    statement.setInt(2, page * amount);
                }
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        UUID uuid = UUID.fromString(result.getString(1));
                        Date date = new Date(result.getLong(2));
                        ItemStack book = super.plugin.getAPI().decodeItemStack(result.getString(3));
                        list.add(new TripletTuple<>(uuid, date, book));
                    }
                    return list;
                }
            } catch (SQLException ex) {
                super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to retrieve book security data!", ex);
                return list;
            }
        });
    }

    @Override
    protected void putBookSecurityStack(UUID uuid, Date date, ItemStack book) {
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statementPlayers = super.connection.prepareStatement(
                    "INSERT INTO security_players (player, timestamp, book_hash) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE book_hash=?;");
                 PreparedStatement statementBooks = super.connection.prepareStatement(
                         "INSERT INTO security_books (book_hash, book) VALUES(?, ?) ON DUPLICATE KEY UPDATE book=?;")
            ) {
                String encodedBook = super.plugin.getAPI().encodeItemStack(book);
                String hashBook = Hashing.sha256().hashString(encodedBook, StandardCharsets.UTF_8).toString();
                statementPlayers.setString(1, uuid.toString());
                statementPlayers.setLong(2, date.getTime());
                statementPlayers.setString(3, hashBook);
                statementPlayers.setString(4, hashBook);
                statementPlayers.executeUpdate();
                statementBooks.setString(1, hashBook);
                statementBooks.setString(2, encodedBook);
                statementBooks.setString(3, encodedBook);
                statementBooks.executeUpdate();
            } catch (SQLException ex) {
                super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to save security player data!", ex);
            }
        });
    }

    @Override
    protected Future<ItemStack> getSecurityBookStack(UUID uuid, Date date) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = super.connection.prepareStatement("""
                    SELECT
                    security_books.book
                    FROM security_books, security_players
                    WHERE security_books.book_hash = security_players.book_hash
                    AND security_players.player=?
                    AND security_players.timestamp=?;
                    """
            )) {
                statement.setString(1, uuid.toString());
                statement.setLong(2, date.getTime());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next())
                        return super.plugin.getAPI().decodeItemStack(result.getString(1));
                    return null;
                }
            } catch (SQLException ex) {
                super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to retrieve command data!", ex);
                return null;
            }
        });
    }

    @Override
    protected Queue<TripletTuple<Integer, Side, ItemStack>> getAllNPCBookStacks(AtomicBoolean failed) {
        Queue<TripletTuple<Integer, Side, ItemStack>> queue = new LinkedList<>();
        try (PreparedStatement statement = super.connection.prepareStatement(
                "SELECT npc_id, side, npc_book FROM npc_books;"
        )) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int npcId = result.getInt("npc_id");
                    Side side = Side.fromString(result.getString("side"));
                    ItemStack book = super.plugin.getAPI().decodeItemStack(result.getString("npc_book"));
                    queue.add(new TripletTuple<>(npcId, side, book));
                }
            }
        } catch (SQLException ex) {
            super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to retrieve all book data!", ex);
            failed.set(true);
            return queue;
        }
        return queue;
    }

    @Override
    protected Queue<PairTuple<String, ItemStack>> getAllFilterBookStacks(AtomicBoolean failed) {
        Queue<PairTuple<String, ItemStack>> queue = new LinkedList<>();
        try (PreparedStatement statement = super.connection.prepareStatement(
                "SELECT filter_name, filter_book FROM filters;"
        )) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String filterName = result.getString("filter_name");
                    ItemStack book = super.plugin.getAPI().decodeItemStack(result.getString("filter_book"));
                    queue.add(new PairTuple<>(filterName, book));
                }
            }
        } catch (SQLException ex) {
            super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to retrieve all filter book data!", ex);
            failed.set(true);
            return queue;
        }
        return queue;
    }

    @Override
    protected Queue<TripletTuple<String, String, String>> getAllCommandFilterStacks(AtomicBoolean failed) {
        Queue<TripletTuple<String, String, String>> queue = new LinkedList<>();
        try (PreparedStatement statement = super.connection.prepareStatement(
                "SELECT command_name, filter_name, permission FROM commands;"
        )) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String cmd = result.getString("command_name");
                    String filterName = result.getString("filter_name");
                    String permission = result.getString("permission");
                    queue.add(new TripletTuple<>(cmd, filterName, permission));
                }
            }
        } catch (SQLException ex) {
            super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to retrieve all command data!", ex);
            failed.set(true);
            return queue;
        }
        return queue;
    }

    @Override
    protected Queue<TripletTuple<UUID, Date, ItemStack>> getAllBookSecurityStacks(AtomicBoolean failed) {
        Queue<TripletTuple<UUID, Date, ItemStack>> queue = new LinkedList<>();
        try (PreparedStatement statement = super.connection.prepareStatement("""
                SELECT
                security_players.player,
                security_players.timestamp,
                security_books.book
                FROM security_books, security_players
                WHERE security_books.book_hash = security_players.book_hash;
                """
        )) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    UUID uuid = UUID.fromString(result.getString(1));
                    Date date = new Date(result.getLong(2));
                    ItemStack book = super.plugin.getAPI().decodeItemStack(result.getString(3));
                    queue.add(new TripletTuple<>(uuid, date, book));
                }
            }
        } catch (SQLException ex) {
            super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to retrieve all book security data!", ex);
            failed.set(true);
            return queue;
        }
        return queue;
    }

    @Override
    protected void setAllNPCBookStacks(Queue<TripletTuple<Integer, Side, ItemStack>> queue, AtomicBoolean failed) {
        try (PreparedStatement statement = super.connection.prepareStatement(
                "INSERT INTO npc_books (npc_id, side, npc_book) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE npc_book=?;"
        )) {
            TripletTuple<Integer, Side, ItemStack> triplet;
            while ((triplet = queue.poll()) != null) {
                String encoded = super.plugin.getAPI().encodeItemStack(triplet.thirdValue());
                statement.setInt(1, triplet.firstValue());
                statement.setString(2, triplet.secondValue().toString());
                statement.setString(4, encoded);
                statement.setString(5, encoded);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            failed.set(true);
            super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to save all npc book data!", ex);
        }
    }

    @Override
    protected void setAllFilterBookStacks(Queue<PairTuple<String, ItemStack>> queue, AtomicBoolean failed) {
        try (PreparedStatement statement = super.connection.prepareStatement(
                "INSERT INTO filters (filter_name, filter_book) VALUES(?, ?) ON DUPLICATE KEY UPDATE filter_book=?;"
        )) {
            PairTuple<String, ItemStack> pair;
            while ((pair = queue.poll()) != null) {
                String encoded = super.plugin.getAPI().encodeItemStack(pair.secondValue());
                statement.setString(1, pair.firstValue());
                statement.setString(2, encoded);
                statement.setString(3, encoded);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            failed.set(true);
            super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to save all filter book data!", ex);
        }
    }

    @Override
    protected void setAllCommandFilterStacks(Queue<TripletTuple<String, String, String>> queue, AtomicBoolean failed) {
        try (PreparedStatement statement = super.connection.prepareStatement(
                "INSERT INTO commands (command_name, filter_name, permission) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE filter_name=?, permission=?;"
        )) {
            TripletTuple<String, String, String> triplet;
            while ((triplet = queue.poll()) != null) {
                statement.setString(1, triplet.firstValue());
                statement.setString(2, triplet.secondValue());
                statement.setString(3, triplet.thirdValue());
                statement.setString(4, triplet.secondValue());
                statement.setString(5, triplet.thirdValue());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            failed.set(true);
            super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to save all command data!", ex);
        }
    }

    @Override
    protected void setAllBookSecurityStacks(Queue<TripletTuple<UUID, Date, ItemStack>> queue, AtomicBoolean failed) {
        try (PreparedStatement statementPlayers = super.connection.prepareStatement(
                "INSERT INTO security_players (player, timestamp, book_hash) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE book_hash=?;");
             PreparedStatement statementBooks = super.connection.prepareStatement(
                     "INSERT INTO security_books (book_hash, book) VALUES(?, ?) ON DUPLICATE KEY UPDATE book=?;")
        ) {
            TripletTuple<UUID, Date, ItemStack> triplet;
            while ((triplet = queue.poll()) != null) {
                String encodedBook = super.plugin.getAPI().encodeItemStack(triplet.thirdValue());
                String hashBook = Hashing.sha256().hashString(encodedBook, StandardCharsets.UTF_8).toString();
                statementPlayers.setString(1, triplet.firstValue().toString());
                statementPlayers.setLong(2, triplet.secondValue().getTime());
                statementPlayers.setString(3, hashBook);
                statementPlayers.setString(4, hashBook);
                statementPlayers.addBatch();
                statementBooks.setString(1, hashBook);
                statementBooks.setString(2, encodedBook);
                statementBooks.setString(3, encodedBook);
                statementBooks.addBatch();
            }
            statementPlayers.executeBatch();
            statementBooks.executeBatch();
        } catch (SQLException ex) {
            failed.set(true);
            super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to save all security books data!", ex);
        }
    }

    @Override
    protected Map<UUID, Set<Date>> cleanOldSecurityBookStacks() {
        try {
            super.lock.lock();
            Map<UUID, Set<Date>> map = new HashMap<>();
            long interval = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(super.purgeSecurityBooksOlderThan);
            try (PreparedStatement statementSelectPlayers = super.connection.prepareStatement(
                    ("SELECT player, timestamp FROM security_players WHERE timestamp < " + interval + ";"));
                 PreparedStatement statementDeletePlayers = super.connection.prepareStatement(
                         ("DELETE FROM security_players WHERE timestamp < " + interval + ";"));

                 PreparedStatement statementBooks = super.connection.prepareStatement("""
                          DELETE FROM security_books WHERE book_hash IN (
                          SELECT
                          security_books.book_hash
                          FROM security_books, security_players
                          WHERE security_books.book_hash = security_players.book_hash
                          AND security_players.book_hash IS NULL
                          );
                         """
                 )) {
                try (ResultSet result = statementSelectPlayers.executeQuery()) {
                    while (result.next()) {
                        UUID uuid = UUID.fromString(result.getString("player"));
                        Date timestamp = new Date(result.getLong("timestamp"));
                        map.computeIfAbsent(uuid, key -> new HashSet<>()).add(timestamp);
                    }
                }
                statementDeletePlayers.executeUpdate();
                statementBooks.executeUpdate();
            } catch (SQLException ex) {
                super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to clean old security books data!", ex);
            }
            return map;
        } finally {
            super.lock.unlock();
        }
    }
}
