package ro.niconeko.astralbooks.storage.types.impl;

import com.google.common.hash.Hashing;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import org.bukkit.inventory.ItemStack;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.StorageType;
import ro.niconeko.astralbooks.storage.types.EmbedStorage;
import ro.niconeko.astralbooks.utils.Side;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class SQLiteStorage extends EmbedStorage {

    public SQLiteStorage(AstralBooksPlugin plugin) {
        super(plugin, StorageType.SQLITE);
    }

    @Override
    protected String getDriver() {
        return "org.sqlite.JDBC";
    }

    @Override
    protected String getURL() {
        return "jdbc:sqlite:" + super.plugin.getDataFolder() + File.separator + super.fileName;
    }

    @Override
    protected boolean createTables() {
        try (PreparedStatement statement = super.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS 'filters' (
                filter_name VARCHAR(255) PRIMARY KEY,
                filter_book TEXT
                );
                """
        )) {
            statement.execute();
        } catch (SQLException ex) {
            super.plugin.getLogger().log(Level.SEVERE, "(" + super.storageType.getFormattedName() + ") Failed to create 'filters' table!", ex);
            return false;
        }
        try (PreparedStatement statement = super.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS 'commands' (
                command_name VARCHAR(255) PRIMARY KEY,
                filter_name VARCHAR(255),
                permission VARCHAR(255)
                );
                """
        )) {
            statement.execute();
        } catch (SQLException ex) {
            super.plugin.getLogger().log(Level.SEVERE, "(" + super.storageType.getFormattedName() + ") Failed to create 'commands' table!", ex);
            return false;
        }
        try (PreparedStatement statement = super.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS 'npc_books' (
                npc_id INT NOT NULL,
                side VARCHAR(32) NOT NULL DEFAULT 'right_side',
                npc_book TEXT,
                PRIMARY KEY (npc_id, side)
                );
                """
        )) {
            statement.execute();
        } catch (SQLException ex) {
            super.plugin.getLogger().log(Level.SEVERE, "(" + super.storageType.getFormattedName() + ") Failed to create 'npcbooks' table!", ex);
            return false;
        }
        try (PreparedStatement statement = super.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS 'security_books' (
                book_hash VARCHAR(256) PRIMARY KEY,
                book TEXT
                );
                """
        )) {
            statement.execute();
        } catch (SQLException ex) {
            super.plugin.getLogger().log(Level.SEVERE, "(" + super.storageType.getFormattedName() + ") Failed to create 'security_books' table!", ex);
            return false;
        }
        try (PreparedStatement statement = super.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS 'security_players' (
                player VARCHAR(48) NOT NULL,
                timestamp TIMESTAMP NOT NULL,
                book_hash VARCHAR(256) NOT NULL,
                PRIMARY KEY (player, timestamp)
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

    @Override
    protected Future<ItemStack> getFilterBookStack(String filterName) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = super.connection.prepareStatement(
                    "SELECT filter_book FROM 'filters' WHERE filter_name=?;"
            )) {
                statement.setString(1, filterName);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next())
                        return super.plugin.getAPI().decodeItemStack(result.getString("filter_book"));
                    return null;
                }
            } catch (SQLException ex) {
                super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to retrieve book data!", ex);
                return null;
            }
        });
    }

    @Override
    protected Future<ItemStack> getNPCBookStack(int npcId, Side side) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = super.connection.prepareStatement(
                    "SELECT npc_book FROM 'npc_books' WHERE npc_id=? AND side=?;"
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
    protected Future<Pair<String, String>> getCommandFilterStack(String cmd) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = super.connection.prepareStatement(
                    "SELECT filter_name, permission FROM 'commands' WHERE command_name=?;"
            )) {
                statement.setString(1, cmd);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next())
                        return Pair.of(result.getString("filter_name"), result.getString("permission"));
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
        super.cache.npcs.remove(Pair.of(npcId, side));
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = super.connection.prepareStatement(
                    "DELETE FROM 'npc_books' WHERE npc_id=? AND side=?;"
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
                    "DELETE FROM 'filters' WHERE filter_name=?;"
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
                    "DELETE FROM 'commands' WHERE command_name=?;"
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
        Pair<Integer, Side> pairKey = Pair.of(npcId, side);
        super.cache.npcs.add(pairKey);
        super.cache.npcBooks.put(pairKey, book);
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = super.connection.prepareStatement(
                    "INSERT INTO 'npc_books' (npc_id, side, npc_book) VALUES(?, ?, ?) ON CONFLICT(npc_id, side) DO UPDATE SET npc_book=?;"
            )) {
                String encoded = super.plugin.getAPI().encodeItemStack(book);
                statement.setInt(1, npcId);
                statement.setString(2, side.toString());
                statement.setString(3, encoded);
                statement.setString(4, encoded);
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
                    "INSERT INTO 'filters' (filter_name, filter_book) VALUES(?, ?) ON CONFLICT(filter_name) DO UPDATE SET filter_book=?;"
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
        super.cache.commandFilters.put(cmd, Pair.of(filterName, permission));
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = super.connection.prepareStatement(
                    "INSERT INTO 'commands' (command_name, filter_name, permission) VALUES(?, ?, ?) ON CONFLICT(command_name) DO UPDATE SET filter_name=?, permission=?;"
            )) {
                statement.setString(1, cmd);
                statement.setString(2, filterName);
                statement.setString(3, permission);
                statement.setString(4, filterName);
                statement.setString(5, permission);
                statement.executeUpdate();
            } catch (SQLException ex) {
                super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to save book data!", ex);
            }
        });
    }

    @Override
    protected Future<LinkedList<Pair<Date, ItemStack>>> getAllBookSecurityStack(UUID uuid, int page, int amount) {
        return super.cache.poolExecutor.submit(() -> {
            LinkedList<Pair<Date, ItemStack>> list = new LinkedList<>();
            String query = page > -1 ? """
                    SELECT
                    'security_players'.timestamp,
                    'security_books'.book
                    FROM 'security_books', 'security_players'
                    WHERE 'security_books'.book_hash = 'security_players'.book_hash
                    AND 'security_players'.player = ?
                    ORDER BY 'security_players'.timestamp DESC LIMIT ? OFFSET ?;
                    """ : """
                    SELECT
                    'security_players'.timestamp,
                    'security_books'.book
                    FROM 'security_books', 'security_players'
                    WHERE 'security_books'.book_hash = 'security_players'.book_hash
                    AND 'security_players'.player = ?
                    ORDER BY 'security_players'.timestamp DESC;
                    """;
            try (PreparedStatement statement = super.connection.prepareStatement(query)) {
                statement.setString(1, uuid.toString());
                if (page > -1) {
                    statement.setInt(2, amount);
                    statement.setInt(3, page * amount);
                }
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        ItemStack book = super.plugin.getAPI().decodeItemStack(result.getString(1));
                        Date date = new Date(result.getTimestamp(2).getTime());
                        list.add(Pair.of(date, book));
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
    protected Future<LinkedList<Triplet<UUID, Date, ItemStack>>> getAllBookSecurityStack(int page, int amount) {
        return super.cache.poolExecutor.submit(() -> {
            LinkedList<Triplet<UUID, Date, ItemStack>> list = new LinkedList<>();
            String query = page > -1 ? """
                    SELECT
                    'security_players'.player,
                    'security_players'.timestamp,
                    'security_books'.book
                    FROM 'security_books', 'security_players'
                    WHERE 'security_books'.book_hash = 'security_players'.book_hash
                    ORDER BY 'security_players'.timestamp DESC LIMIT ? OFFSET ?;
                    """ : """
                    SELECT
                    'security_players'.player,
                    'security_players'.timestamp,
                    'security_books'.book
                    FROM 'security_books', 'security_players'
                    WHERE 'security_books'.book_hash = 'security_players'.book_hash
                    ORDER BY 'security_players'.timestamp DESC;
                    """;
            try (PreparedStatement statement = super.connection.prepareStatement(query)) {
                if (page > -1) {
                    statement.setInt(1, amount);
                    statement.setInt(2, page * amount);
                }
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        UUID uuid = UUID.fromString(result.getString(1));
                        Date date = new Date(result.getTimestamp(2).getTime());
                        ItemStack book = super.plugin.getAPI().decodeItemStack(result.getString(3));
                        list.add(Triplet.of(uuid, date, book));
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
        super.cache.playerTimestamps.getUnchecked(uuid).add(date);
        super.cache.poolExecutor.submit(() -> {
            Timestamp timestamp = new Timestamp(date.getTime());
            String encodedBook = super.plugin.getAPI().encodeItemStack(book);
            String hashBook = Hashing.sha256().hashString(encodedBook, StandardCharsets.UTF_8).toString();
            try (PreparedStatement statementPlayers = super.connection.prepareStatement(
                    "INSERT INTO 'security_players' (player, timestamp, book_hash) VALUES(?, ?, ?) ON CONFLICT(player, timestamp) DO UPDATE SET book_hash=?;");
                 PreparedStatement statementBooks = super.connection.prepareStatement(
                         "INSERT INTO 'security_books' (book_hash, book) VALUES(?, ?) ON CONFLICT(book_hash) DO UPDATE SET book=?;")
            ) {
                statementPlayers.setString(1, uuid.toString());
                statementPlayers.setTimestamp(2, timestamp);
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
                    'security_books'.book
                    FROM 'security_books', 'security_players'
                    WHERE 'security_books'.book_hash = 'security_players'.book_hash
                    AND 'security_players'.player=?
                    AND 'security_players'.timestamp=?;
                    """
            )) {
                statement.setString(1, uuid.toString());
                statement.setTimestamp(2, new Timestamp(date.getTime()));
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
    protected Queue<Triplet<Integer, Side, ItemStack>> getAllNPCBookStacks(AtomicBoolean failed) {
        Queue<Triplet<Integer, Side, ItemStack>> queue = new LinkedList<>();
        try (PreparedStatement statement = super.connection.prepareStatement(
                "SELECT npc_book, npc_id, side FROM 'npc_books';"
        )) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int npcId = result.getInt("npc_id");
                    Side side = Side.fromString(result.getString("side"));
                    ItemStack book = super.plugin.getAPI().decodeItemStack(result.getString("npc_book"));
                    queue.add(Triplet.of(npcId, side, book));
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
    protected Queue<Pair<String, ItemStack>> getAllFilterBookStacks(AtomicBoolean failed) {
        Queue<Pair<String, ItemStack>> queue = new LinkedList<>();
        try (PreparedStatement statement = super.connection.prepareStatement(
                "SELECT filter_name, filter_book FROM 'filters';"
        )) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String filterName = result.getString("filter_name");
                    ItemStack book = super.plugin.getAPI().decodeItemStack(result.getString("filter_book"));
                    queue.add(Pair.of(filterName, book));
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
    protected Queue<Triplet<String, String, String>> getAllCommandFilterStacks(AtomicBoolean failed) {
        Queue<Triplet<String, String, String>> queue = new LinkedList<>();
        try (PreparedStatement statement = super.connection.prepareStatement(
                "SELECT command_name, filter_name, permission FROM 'commands';"
        )) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String cmd = result.getString("command_name");
                    String filterName = result.getString("filter_name");
                    String permission = result.getString("permission");
                    queue.add(Triplet.of(cmd, filterName, permission));
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
    protected Queue<Triplet<UUID, Date, ItemStack>> getAllBookSecurityStacks(AtomicBoolean failed) {
        Queue<Triplet<UUID, Date, ItemStack>> queue = new LinkedList<>();
        try (PreparedStatement statement = super.connection.prepareStatement("""
                SELECT
                'security_players'.player,
                'security_players'.timestamp,
                'security_books'.book
                FROM 'security_books', 'security_players'
                WHERE 'security_books'.book_hash = 'security_players'.book_hash;
                """
        )) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    UUID uuid = UUID.fromString(result.getString(1));
                    Date date = new Date(result.getLong(2));
                    ItemStack book = super.plugin.getAPI().decodeItemStack(result.getString(3));
                    queue.add(Triplet.of(uuid, date, book));
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
    protected void setAllNPCBookStacks(Queue<Triplet<Integer, Side, ItemStack>> queue, AtomicBoolean failed) {
        try (PreparedStatement statement = super.connection.prepareStatement(
                "INSERT INTO 'npc_books' (npc_id, side, npc_book) VALUES(?, ?, ?) ON CONFLICT(npc_id, side) DO UPDATE SET npc_book=?;"
        )) {
            Triplet<Integer, Side, ItemStack> triplet;
            while ((triplet = queue.poll()) != null) {
                String encoded = super.plugin.getAPI().encodeItemStack(triplet.getThirdValue());
                statement.setInt(1, triplet.getFirstValue());
                statement.setString(2, triplet.getSecondValue().toString());
                statement.setString(3, encoded);
                statement.setString(4, encoded);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            failed.set(true);
            super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to save all npc book data!", ex);
        }
    }

    @Override
    protected void setAllFilterBookStacks(Queue<Pair<String, ItemStack>> queue, AtomicBoolean failed) {
        try (PreparedStatement statement = super.connection.prepareStatement(
                "INSERT INTO 'filters' (filter_name, filter_book) VALUES(?, ?) ON CONFLICT(filter_name) DO UPDATE SET filter_book=?;"
        )) {
            Pair<String, ItemStack> pair;
            while ((pair = queue.poll()) != null) {
                String encoded = super.plugin.getAPI().encodeItemStack(pair.getSecondValue());
                statement.setString(1, pair.getFirstValue());
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
    protected void setAllCommandFilterStacks(Queue<Triplet<String, String, String>> queue, AtomicBoolean failed) {
        try (PreparedStatement statement = super.connection.prepareStatement(
                "INSERT INTO 'commands' (command_name, filter_name, permission) VALUES(?, ?, ?) ON CONFLICT(command_name) DO UPDATE SET filter_name=?, permission=?;"
        )) {
            Triplet<String, String, String> triplet;
            while ((triplet = queue.poll()) != null) {
                statement.setString(1, triplet.getFirstValue());
                statement.setString(2, triplet.getSecondValue());
                statement.setString(3, triplet.getThirdValue());
                statement.setString(4, triplet.getSecondValue());
                statement.setString(5, triplet.getThirdValue());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            failed.set(true);
            super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to save all command data!", ex);
        }
    }

    @Override
    protected void setAllBookSecurityStacks(Queue<Triplet<UUID, Date, ItemStack>> queue, AtomicBoolean failed) {
        try (PreparedStatement statementPlayers = super.connection.prepareStatement(
                "INSERT INTO 'security_players' (player, timestamp, book_hash) VALUES(?, ?, ?) ON CONFLICT(player, timestamp) DO UPDATE SET book_hash=?;");
             PreparedStatement statementBooks = super.connection.prepareStatement(
                     "INSERT INTO 'security_books' (book_hash, book) VALUES(?, ?) ON CONFLICT(book_hash) DO UPDATE SET book=?;")
        ) {
            Triplet<UUID, Date, ItemStack> triplet;
            while ((triplet = queue.poll()) != null) {
                String encodedBook = super.plugin.getAPI().encodeItemStack(triplet.getThirdValue());
                String hashBook = Hashing.sha256().hashString(encodedBook, StandardCharsets.UTF_8).toString();
                statementPlayers.setString(1, triplet.getFirstValue().toString());
                statementPlayers.setLong(2, triplet.getSecondValue().getTime());
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
            super.plugin.getLogger().log(Level.WARNING, "(" + super.storageType.getFormattedName() + ") Failed to save all security player data!", ex);
        }
    }

    @Override
    protected Map<UUID, Set<Date>> cleanOldSecurityBookStacks() {
        try {
            super.lock.lock();
            Map<UUID, Set<Date>> map = new HashMap<>();
            long interval = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(super.purgeSecurityBooksOlderThan);
            try (PreparedStatement statementSelectPlayers = super.connection.prepareStatement(
                    "SELECT player, timestamp FROM 'security_players' WHERE timestamp < " + interval + ";");
                 PreparedStatement statementDeletePlayers = super.connection.prepareStatement(
                         "DELETE FROM 'security_players' WHERE timestamp < " + interval + ";");
                 PreparedStatement statementBooks = super.connection.prepareStatement("""
                         DELETE FROM 'security_books' WHERE book_hash IN (
                         SELECT
                         'security_books'.book_hash
                         FROM 'security_books', 'security_players'
                         WHERE 'security_books'.book_hash = 'security_players'.book_hash
                         AND 'security_players'.book_hash IS NULL
                         );
                          """)
            ) {
                try (ResultSet result = statementSelectPlayers.executeQuery()) {
                    while (result.next()) {
                        UUID uuid = UUID.fromString(result.getString(1));
                        Date timestamp = new Date(result.getTimestamp(2).getTime());
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
