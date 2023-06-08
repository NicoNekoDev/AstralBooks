package ro.niconeko.astralbooks.storage.types;

import com.google.common.hash.Hashing;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import org.bukkit.inventory.ItemStack;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.Storage;
import ro.niconeko.astralbooks.storage.StorageType;
import ro.niconeko.astralbooks.storage.settings.StorageSettings;
import ro.niconeko.astralbooks.utils.Side;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class H2Storage extends Storage {
    private Connection connection;
    private int purgeSecurityBooksOlderThan = 30;

    public H2Storage(AstralBooksPlugin plugin) {
        super(plugin, StorageType.H2);
    }

    protected boolean load(StorageSettings settings) throws SQLException {
        try {
            super.lock.lock();
            this.plugin.getLogger().info("Loading H2 database...");
            this.purgeSecurityBooksOlderThan = settings.getSecurityBookPurgeOlderThan();
            Class.forName("org.h2.Driver");
            this.connection = DriverManager.getConnection(
                    "jdbc:h2:./"
                            + super.plugin.getDataFolder() + File.separator + settings.getH2Settings().getFileName()
                            + (settings.getH2Settings().isEncryptionEnabled() ? ";CIPHER=AES" : "") + ";mode=MySQL"
            );
            try (PreparedStatement statement = this.connection.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS filters (
                    filter_name VARCHAR(256),
                    filter_book TEXT,
                    PRIMARY KEY (filter_name)
                    );
                    """
            )) {
                statement.execute();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.SEVERE, "(H2) Failed to create 'filters' table!", ex);
                return false;
            }
            try (PreparedStatement statement = this.connection.prepareStatement("""
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
                this.plugin.getLogger().log(Level.SEVERE, "(H2) Failed to create 'commands' table!", ex);
                return false;
            }
            try (PreparedStatement statement = this.connection.prepareStatement("""
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
                this.plugin.getLogger().log(Level.SEVERE, "(H2) Failed to create 'npcbooks' table!", ex);
                return false;
            }
            try (PreparedStatement statement = this.connection.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS security_books (
                    book_hash VARCHAR(256),
                    book TEXT,
                    PRIMARY KEY (book_hash)
                    );
                    """
            )) {
                statement.execute();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.SEVERE, "(H2) Failed to create 'security_books' table!", ex);
                return false;
            }
            try (PreparedStatement statement = this.connection.prepareStatement("""
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
                this.plugin.getLogger().log(Level.SEVERE, "(H2) Failed to create 'security_players' table!", ex);
                return false;
            }
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "SELECT filter_name FROM filters;"
            )) {
                try (ResultSet preload = statement.executeQuery()) {
                    while (preload.next()) {
                        super.cache.filters.add(preload.getString("filter_name"));
                    }
                }
            }
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "SELECT command_name FROM commands;"
            )) {
                try (ResultSet preload = statement.executeQuery()) {
                    while (preload.next()) {
                        super.cache.commands.add(preload.getString("command_name"));
                    }
                }
            }
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "SELECT npc_id, side FROM npc_books;"
            )) {
                try (ResultSet preload = statement.executeQuery()) {
                    while (preload.next()) {
                        super.cache.npcs.add(Pair.of(preload.getInt("npc_id"), Side.fromString(preload.getString("side"))));
                    }
                }
            }
            super.loaded = true;
            return true;
        } catch (ClassNotFoundException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "(H2) Failed to find MySQL driver!", ex);
            return false;
        } finally {
            super.lock.unlock();
        }
    }

    @Override
    protected void unload() {
        try {
            super.lock.lock();
            this.plugin.getLogger().info("Unloading MySQL database...");
            super.loaded = false;
            try {
                this.connection.close();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.SEVERE, "(H2) Failed to unload database!", ex);
            }
        } finally {
            super.lock.unlock();
        }
    }

    @Override
    protected Future<ItemStack> getFilterBookStack(String filterName) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "SELECT filter_book FROM filters WHERE filter_name=?;"
            )) {
                statement.setString(1, filterName);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next())
                        return this.plugin.getAPI().decodeItemStack(result.getString("filter_book"));
                    return null;
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to retrieve filter book data!", ex);
                return null;
            }
        });
    }

    @Override
    protected Future<ItemStack> getNPCBookStack(int npcId, Side side) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "SELECT npc_book FROM npc_books WHERE npc_id=? AND side=?;"
            )) {
                statement.setInt(1, npcId);
                statement.setString(2, side.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next())
                        return this.plugin.getAPI().decodeItemStack(result.getString("npc_book"));
                    return null;
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to retrieve book data!", ex);
                return null;
            }
        });
    }

    @Override
    protected Future<Pair<String, String>> getCommandFilterStack(String cmd) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "SELECT filter_name, permission FROM commands WHERE command_name=?;"
            )) {
                statement.setString(1, cmd);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next())
                        return Pair.of(result.getString("filter_name"), result.getString("permission"));
                    return null;
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to retrieve command data!", ex);
                return null;
            }
        });
    }

    @Override
    protected void removeNPCBookStack(int npcId, Side side) {
        super.cache.npcs.remove(Pair.of(npcId, side));
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "DELETE FROM npc_books WHERE npc_id=? AND side=?;"
            )) {
                statement.setInt(1, npcId);
                statement.setString(2, side.toString());
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to remove book data!", ex);
            }
        });
    }

    @Override
    protected void removeFilterBookStack(String filterName) {
        super.cache.filters.remove(filterName);
        super.cache.filterBooks.invalidate(filterName);
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "DELETE FROM filters WHERE filter_name=?;"
            )) {
                statement.setString(1, filterName);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to remove book data!", ex);
            }
        });
    }

    @Override
    protected void removeCommandFilterStack(String cmd) {
        super.cache.commands.remove(cmd);
        super.cache.commandFilters.invalidate(cmd);
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "DELETE FROM commands WHERE command_name=?;"
            )) {
                statement.setString(1, cmd);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to remove command data!", ex);
            }
        });
    }

    @Override
    protected void putNPCBookStack(int npcId, Side side, ItemStack book) {
        Pair<Integer, Side> pairKey = Pair.of(npcId, side);
        super.cache.npcs.add(pairKey);
        super.cache.npcBooks.put(pairKey, book);
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "INSERT INTO npc_books (npc_id, side, npc_book) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE npc_book=?;"
            )) {
                String encoded = this.plugin.getAPI().encodeItemStack(book);
                statement.setInt(1, npcId);
                statement.setString(2, side.toString());
                statement.setString(4, encoded);
                statement.setString(5, encoded);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to save book data!", ex);
            }
        });
    }

    @Override
    protected void putFilterBookStack(String filterName, ItemStack book) {
        super.cache.filters.add(filterName);
        super.cache.filterBooks.put(filterName, book);
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "INSERT INTO filters (filter_name, filter_book) VALUES(?, ?) ON DUPLICATE KEY UPDATE filter_book=?;"
            )) {
                String encoded = this.plugin.getAPI().encodeItemStack(book);
                statement.setString(1, filterName);
                statement.setString(2, encoded);
                statement.setString(3, encoded);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to save book data!", ex);
            }
        });
    }

    @Override
    protected void putCommandFilterStack(String cmd, String filterName, String permission) {
        super.cache.commands.add(cmd);
        super.cache.commandFilters.put(cmd, Pair.of(filterName, permission));
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "INSERT INTO commands (command_name, filter_name, permission) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE filter_name=?, permission=?;"
            )) {
                statement.setString(1, cmd);
                statement.setString(2, filterName);
                statement.setString(3, permission);
                statement.setString(4, filterName);
                statement.setString(5, permission);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to save command data!", ex);
            }
        });
    }

    @Override
    protected Future<LinkedList<Pair<Date, ItemStack>>> getAllBookSecurityStack(UUID uuid, int page, int amount) {
        return super.cache.poolExecutor.submit(() -> {
            LinkedList<Pair<Date, ItemStack>> list = new LinkedList<>();
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
            try (PreparedStatement statement = this.connection.prepareStatement(query)) {
                statement.setString(1, uuid.toString());
                if (page > -1) {
                    statement.setInt(2, amount);
                    statement.setInt(3, page * amount);
                }
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        Date date = new Date(result.getLong(1));
                        ItemStack book = this.plugin.getAPI().decodeItemStack(result.getString(2));
                        list.add(Pair.of(date, book));
                    }
                    return list;
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to retrieve book security data!", ex);
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
            try (PreparedStatement statement = this.connection.prepareStatement(query)) {
                if (page > -1) {
                    statement.setInt(1, amount);
                    statement.setInt(2, page * amount);
                }
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        UUID uuid = UUID.fromString(result.getString(1));
                        Date date = new Date(result.getLong(2));
                        ItemStack book = this.plugin.getAPI().decodeItemStack(result.getString(3));
                        list.add(Triplet.of(uuid, date, book));
                    }
                    return list;
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to retrieve book security data!", ex);
                return list;
            }
        });
    }

    @Override
    protected void putBookSecurityStack(UUID uuid, Date date, ItemStack book) {
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statementPlayers = this.connection.prepareStatement(
                    "INSERT INTO security_players (player, timestamp, book_hash) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE book_hash=?;");
                 PreparedStatement statementBooks = this.connection.prepareStatement(
                         "INSERT INTO security_books (book_hash, book) VALUES(?, ?) ON DUPLICATE KEY UPDATE book=?;")
            ) {
                String encodedBook = this.plugin.getAPI().encodeItemStack(book);
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
                this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to save security player data!", ex);
            }
        });
    }

    @Override
    protected Future<ItemStack> getSecurityBookStack(UUID uuid, Date date) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("""
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
                        return this.plugin.getAPI().decodeItemStack(result.getString(1));
                    return null;
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to retrieve command data!", ex);
                return null;
            }
        });
    }

    @Override
    protected Queue<Triplet<Integer, Side, ItemStack>> getAllNPCBookStacks(AtomicBoolean failed) {
        Queue<Triplet<Integer, Side, ItemStack>> queue = new LinkedList<>();
        try (PreparedStatement statement = this.connection.prepareStatement(
                "SELECT npc_id, side, npc_book FROM npc_books;"
        )) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int npcId = result.getInt("npc_id");
                    Side side = Side.fromString(result.getString("side"));
                    ItemStack book = this.plugin.getAPI().decodeItemStack(result.getString("npc_book"));
                    queue.add(Triplet.of(npcId, side, book));
                }
            }
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to retrieve all book data!", ex);
            failed.set(true);
            return queue;
        }
        return queue;
    }

    @Override
    protected Queue<Pair<String, ItemStack>> getAllFilterBookStacks(AtomicBoolean failed) {
        Queue<Pair<String, ItemStack>> queue = new LinkedList<>();
        try (PreparedStatement statement = this.connection.prepareStatement(
                "SELECT filter_name, filter_book FROM filters;"
        )) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String filterName = result.getString("filter_name");
                    ItemStack book = this.plugin.getAPI().decodeItemStack(result.getString("filter_book"));
                    queue.add(Pair.of(filterName, book));
                }
            }
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to retrieve all filter book data!", ex);
            failed.set(true);
            return queue;
        }
        return queue;
    }

    @Override
    protected Queue<Triplet<String, String, String>> getAllCommandFilterStacks(AtomicBoolean failed) {
        Queue<Triplet<String, String, String>> queue = new LinkedList<>();
        try (PreparedStatement statement = this.connection.prepareStatement(
                "SELECT command_name, filter_name, permission FROM commands;"
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
            this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to retrieve all command data!", ex);
            failed.set(true);
            return queue;
        }
        return queue;
    }

    @Override
    protected Queue<Triplet<UUID, Date, ItemStack>> getAllBookSecurityStacks(AtomicBoolean failed) {
        Queue<Triplet<UUID, Date, ItemStack>> queue = new LinkedList<>();
        try (PreparedStatement statement = this.connection.prepareStatement("""
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
                    ItemStack book = this.plugin.getAPI().decodeItemStack(result.getString(3));
                    queue.add(Triplet.of(uuid, date, book));
                }
            }
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to retrieve all book security data!", ex);
            failed.set(true);
            return queue;
        }
        return queue;
    }

    @Override
    protected void setAllNPCBookStacks(Queue<Triplet<Integer, Side, ItemStack>> queue, AtomicBoolean failed) {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO npc_books (npc_id, side, npc_book) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE npc_book=?;"
        )) {
            Triplet<Integer, Side, ItemStack> triplet;
            while ((triplet = queue.poll()) != null) {
                String encoded = this.plugin.getAPI().encodeItemStack(triplet.getThirdValue());
                statement.setInt(1, triplet.getFirstValue());
                statement.setString(2, triplet.getSecondValue().toString());
                statement.setString(4, encoded);
                statement.setString(5, encoded);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            failed.set(true);
            this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to save all npc book data!", ex);
        }
    }

    @Override
    protected void setAllFilterBookStacks(Queue<Pair<String, ItemStack>> queue, AtomicBoolean failed) {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO filters (filter_name, filter_book) VALUES(?, ?) ON DUPLICATE KEY UPDATE filter_book=?;"
        )) {
            Pair<String, ItemStack> pair;
            while ((pair = queue.poll()) != null) {
                String encoded = this.plugin.getAPI().encodeItemStack(pair.getSecondValue());
                statement.setString(1, pair.getFirstValue());
                statement.setString(2, encoded);
                statement.setString(3, encoded);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            failed.set(true);
            this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to save all filter book data!", ex);
        }
    }

    @Override
    protected void setAllCommandFilterStacks(Queue<Triplet<String, String, String>> queue, AtomicBoolean failed) {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO commands (command_name, filter_name, permission) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE filter_name=?, permission=?;"
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
            this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to save all command data!", ex);
        }
    }

    @Override
    protected void setAllBookSecurityStacks(Queue<Triplet<UUID, Date, ItemStack>> queue, AtomicBoolean failed) {
        try (PreparedStatement statementPlayers = this.connection.prepareStatement(
                "INSERT INTO security_players (player, timestamp, book_hash) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE book_hash=?;");
             PreparedStatement statementBooks = this.connection.prepareStatement(
                     "INSERT INTO security_books (book_hash, book) VALUES(?, ?) ON DUPLICATE KEY UPDATE book=?;")
        ) {
            Triplet<UUID, Date, ItemStack> triplet;
            while ((triplet = queue.poll()) != null) {
                String encodedBook = this.plugin.getAPI().encodeItemStack(triplet.getThirdValue());
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
            this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to save all security books data!", ex);
        }
    }

    @Override
    protected Map<UUID, Set<Date>> cleanOldSecurityBookStacks() {
        try {
            super.lock.lock();
            Map<UUID, Set<Date>> map = new HashMap<>();
            long interval = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(this.purgeSecurityBooksOlderThan);
            try (PreparedStatement statementSelectPlayers = this.connection.prepareStatement(
                    ("SELECT player, timestamp FROM security_players WHERE timestamp < " + interval + ";"));
                 PreparedStatement statementDeletePlayers = this.connection.prepareStatement(
                         ("DELETE FROM security_players WHERE timestamp < " + interval + ";"));

                 PreparedStatement statementBooks = this.connection.prepareStatement("""
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
                this.plugin.getLogger().log(Level.WARNING, "(H2) Failed to clean old security books data!", ex);
            }
            return map;
        } finally {
            super.lock.unlock();
        }
    }
}
