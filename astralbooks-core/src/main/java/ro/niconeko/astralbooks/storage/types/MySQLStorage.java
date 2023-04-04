package ro.niconeko.astralbooks.storage.types;

import com.google.common.hash.Hashing;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import org.bukkit.inventory.ItemStack;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.Storage;
import ro.niconeko.astralbooks.storage.StorageType;
import ro.niconeko.astralbooks.storage.settings.StorageMySQLSettings;
import ro.niconeko.astralbooks.storage.settings.StorageSettings;
import ro.niconeko.astralbooks.utils.Side;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class MySQLStorage extends Storage {
    private Connection connection;
    private String tablePrefix = "";
    private String serverName = "";

    public MySQLStorage(AstralBooksPlugin plugin) {
        super(plugin, StorageType.MYSQL);
    }

    protected boolean load(StorageSettings settings) throws SQLException {
        StorageMySQLSettings mySQLSettings = settings.getMySQLSettings();
        String user = mySQLSettings.getUsername();
        String pass = mySQLSettings.getPassword();
        String host = mySQLSettings.getHost();
        int port = mySQLSettings.getPort();
        String database = mySQLSettings.getDatabase();
        boolean sslEnabled = mySQLSettings.isSSLEnabled();
        this.tablePrefix = mySQLSettings.getTablePrefix();
        this.serverName = mySQLSettings.getServerName();
        this.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?user=" + user + "&password=" + pass + "&useSSL=" + sslEnabled + "&autoReconnect=true");
        this.connection.setAutoCommit(true);
        try (PreparedStatement statement = this.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS %sfilters (
                filter_name VARCHAR(256),
                filter_book TEXT,
                PRIMARY KEY (filter_name)
                );
                """.formatted(this.tablePrefix)
        )) {
            statement.execute();
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "(MYSQL) Failed to create 'filters' table!", ex);
            return false;
        }
        try (PreparedStatement statement = this.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS %scommands (
                command_name VARCHAR(256),
                filter_name VARCHAR(256),
                permission VARCHAR(255),
                PRIMARY KEY (command_name)
                );
                """.formatted(this.tablePrefix)
        )) {
            statement.execute();
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "(MYSQL) Failed to create 'commands' table!", ex);
            return false;
        }
        try (PreparedStatement statement = this.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS %snpc_books (
                npc_id INT NOT NULL,
                side VARCHAR(32) NOT NULL DEFAULT 'right_side',
                server VARCHAR(256) DEFAULT 'default',
                npc_book TEXT,
                CONSTRAINT npc_id_side PRIMARY KEY (npc_id, side)
                );
                """.formatted(this.tablePrefix)
        )) {
            statement.execute();
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "(MYSQL) Failed to create 'npcbooks' table!", ex);
            return false;
        }
        try (PreparedStatement statement = this.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS %ssecurity_books (
                book_hash VARCHAR(256),
                book TEXT,
                PRIMARY KEY (book_hash)
                );
                """.formatted(this.tablePrefix)
        )) {
            statement.execute();
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "(MYSQL) Failed to create 'security_books' table!", ex);
            return false;
        }
        try (PreparedStatement statement = this.connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS %ssecurity_players (
                player VARCHAR(48) NOT NULL,
                timestamp BIGINT NOT NULL,
                book_hash VARCHAR(256) NOT NULL,
                CONSTRAINT player_date PRIMARY KEY (player, timestamp)
                );
                """.formatted(this.tablePrefix)
        )) {
            statement.execute();
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "(MYSQL) Failed to create 'security_players' table!", ex);
            return false;
        }
        try (PreparedStatement statement = this.connection.prepareStatement(
                "SELECT filter_name FROM %sfilters;".formatted(this.tablePrefix)
        )) {
            try (ResultSet preload = statement.executeQuery()) {
                while (preload.next()) {
                    super.cache.filters.add(preload.getString("filter_name"));
                }
            }
        }
        try (PreparedStatement statement = this.connection.prepareStatement(
                "SELECT command_name FROM %scommands;".formatted(this.tablePrefix)
        )) {
            try (ResultSet preload = statement.executeQuery()) {
                while (preload.next()) {
                    super.cache.commands.add(preload.getString("command_name"));
                }
            }
        }
        try (PreparedStatement statement = this.connection.prepareStatement(
                "SELECT npc_id, side FROM %snpc_books;".formatted(this.tablePrefix)
        )) {
            try (ResultSet preload = statement.executeQuery()) {
                while (preload.next()) {
                    super.cache.npcs.add(Pair.of(preload.getInt("npc_id"), Side.fromString(preload.getString("side"))));
                }
            }
        }
        return true;
    }

    @Override
    protected void unload() {
        try {
            this.connection.close();
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "(MYSQL) Failed to unload database!", ex);
        }
    }

    @Override
    protected Future<ItemStack> getFilterBookStack(String filterName) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "SELECT filter_book FROM %sfilters WHERE filter_name=?;".formatted(this.tablePrefix)
            )) {
                statement.setString(1, filterName);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next())
                        return this.plugin.getAPI().decodeItemStack(result.getString("filter_book"));
                    return null;
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to retrieve filter book data!", ex);
                return null;
            }
        });
    }

    @Override
    protected Future<ItemStack> getNPCBookStack(int npcId, Side side) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "SELECT npc_book FROM %snpc_books WHERE npc_id=? AND side=? AND server=?;".formatted(this.tablePrefix)
            )) {
                statement.setInt(1, npcId);
                statement.setString(2, side.toString());
                statement.setString(3, this.serverName);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next())
                        return this.plugin.getAPI().decodeItemStack(result.getString("npc_book"));
                    return null;
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to retrieve book data!", ex);
                return null;
            }
        });
    }

    @Override
    protected Future<Pair<String, String>> getCommandFilterStack(String cmd) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "SELECT filter_name, permission FROM %scommands WHERE command_name=?;".formatted(this.tablePrefix)
            )) {
                statement.setString(1, cmd);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next())
                        return Pair.of(result.getString("filter_name"), result.getString("permission"));
                    return null;
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to retrieve command data!", ex);
                return null;
            }
        });
    }

    @Override
    protected void removeNPCBookStack(int npcId, Side side) {
        super.cache.npcs.remove(Pair.of(npcId, side));
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "DELETE FROM %snpc_books WHERE npc_id=? AND side=? AND server=?;".formatted(this.tablePrefix)
            )) {
                statement.setInt(1, npcId);
                statement.setString(2, side.toString());
                statement.setString(3, this.serverName);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to remove book data!", ex);
            }
        });
    }

    @Override
    protected void removeFilterBookStack(String filterName) {
        super.cache.filters.remove(filterName);
        super.cache.filterBooks.invalidate(filterName);
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "DELETE FROM %sfilters WHERE filter_name=?;".formatted(this.tablePrefix)
            )) {
                statement.setString(1, filterName);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to remove book data!", ex);
            }
        });
    }

    @Override
    protected void removeCommandFilterStack(String cmd) {
        super.cache.commands.remove(cmd);
        super.cache.commandFilters.invalidate(cmd);
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "DELETE FROM %scommands WHERE command_name=?;".formatted(this.tablePrefix)
            )) {
                statement.setString(1, cmd);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to remove command data!", ex);
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
                    "INSERT INTO %snpc_books (npc_id, side, server, npc_book) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE npc_book=?;".formatted(this.tablePrefix)
            )) {
                String encoded = this.plugin.getAPI().encodeItemStack(book);
                statement.setInt(1, npcId);
                statement.setString(2, side.toString());
                statement.setString(3, this.serverName);
                statement.setString(4, encoded);
                statement.setString(5, encoded);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to save book data!", ex);
            }
        });
    }

    @Override
    protected void putFilterBookStack(String filterName, ItemStack book) {
        super.cache.filters.add(filterName);
        super.cache.filterBooks.put(filterName, book);
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "INSERT INTO %sfilters (filter_name, filter_book) VALUES(?, ?) ON DUPLICATE KEY UPDATE filter_book=?;".formatted(this.tablePrefix)
            )) {
                String encoded = this.plugin.getAPI().encodeItemStack(book);
                statement.setString(1, filterName);
                statement.setString(2, encoded);
                statement.setString(3, encoded);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to save book data!", ex);
            }
        });
    }

    @Override
    protected void putCommandFilterStack(String cmd, String filterName, String permission) {
        super.cache.commands.add(cmd);
        super.cache.commandFilters.put(cmd, Pair.of(filterName, permission));
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "INSERT INTO %scommands (command_name, filter_name, permission) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE filter_name=?, permission=?;".formatted(this.tablePrefix)
            )) {
                statement.setString(1, cmd);
                statement.setString(2, filterName);
                statement.setString(3, permission);
                statement.setString(4, filterName);
                statement.setString(5, permission);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to save command data!", ex);
            }
        });
    }

    @Override
    protected Future<LinkedList<Pair<Date, ItemStack>>> getAllBookSecurityStack(UUID uuid, int page, int amount) {
        return super.cache.poolExecutor.submit(() -> {
            LinkedList<Pair<Date, ItemStack>> list = new LinkedList<>();
            String query = page > -1 ? """
                    SELECT book, timestamp
                    FROM %1$ssecurity_books
                    INNER JOIN %1$ssecurity_players USING(book_hash)
                    WHERE player = ?
                    ORDER BY timestamp DESC LIMIT ? OFFSET ?;
                    """ : """
                    SELECT book, timestamp
                    FROM %1$ssecurity_books
                    INNER JOIN %1$ssecurity_players USING(book_hash)
                    WHERE player = ?
                    ORDER BY timestamp DESC;
                    """;
            try (PreparedStatement statement = this.connection.prepareStatement(query.formatted(this.tablePrefix))) {
                statement.setString(1, uuid.toString());
                if (page > -1) {
                    statement.setInt(2, amount);
                    statement.setInt(3, page * amount);
                }
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        Date date = new Date(result.getLong("timestamp"));
                        ItemStack book = this.plugin.getAPI().decodeItemStack(result.getString("book"));
                        list.add(Pair.of(date, book));
                    }
                    return list;
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to retrieve book security data!", ex);
                return list;
            }
        });
    }

    @Override
    protected Future<LinkedList<Triplet<UUID, Date, ItemStack>>> getAllBookSecurityStack(int page, int amount) {
        return super.cache.poolExecutor.submit(() -> {
            LinkedList<Triplet<UUID, Date, ItemStack>> list = new LinkedList<>();
            String query = page > -1 ? """
                    SELECT book, timestamp, player
                    FROM %1$ssecurity_books
                    INNER JOIN %1$ssecurity_players USING(book_hash)
                    ORDER BY timestamp DESC LIMIT ? OFFSET ?;
                    """ : """
                    SELECT book, timestamp, player
                    FROM %1$ssecurity_books
                    INNER JOIN %1$ssecurity_players USING(book_hash)
                    ORDER BY timestamp DESC;
                    """;
            try (PreparedStatement statement = this.connection.prepareStatement(query.formatted(this.tablePrefix))) {
                if (page > -1) {
                    statement.setInt(1, amount);
                    statement.setInt(2, page * amount);
                }
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        UUID uuid = UUID.fromString(result.getString("player"));
                        Date date = new Date(result.getLong("timestamp"));
                        ItemStack book = this.plugin.getAPI().decodeItemStack(result.getString("book"));
                        list.add(Triplet.of(uuid, date, book));
                    }
                    return list;
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to retrieve book security data!", ex);
                return list;
            }
        });
    }

    @Override
    protected void putBookSecurityStack(UUID uuid, Date date, ItemStack book) {
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statementPlayers = this.connection.prepareStatement(
                    "INSERT INTO %ssecurity_players (player, timestamp, book_hash) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE book_hash=?;".formatted(this.tablePrefix));
                 PreparedStatement statementBooks = this.connection.prepareStatement(
                         "INSERT INTO %ssecurity_books (book_hash, book) VALUES(?, ?) ON DUPLICATE KEY UPDATE book=?;".formatted(this.tablePrefix))
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
                this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to save security player data!", ex);
            }
        });
    }

    @Override
    protected Future<ItemStack> getSecurityBookStack(UUID uuid, Date date) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("""
                    SELECT book
                    FROM %1$ssecurity_books
                    INNER JOIN %1$ssecurity_players USING(book_hash)
                    WHERE player=?
                    AND timestamp=?;
                    """.formatted(this.tablePrefix)
            )) {
                statement.setString(1, uuid.toString());
                statement.setLong(2, date.getTime());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next())
                        return this.plugin.getAPI().decodeItemStack(result.getString("book"));
                    return null;
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to retrieve command data!", ex);
                return null;
            }
        });
    }

    @Override
    protected Queue<Triplet<Integer, Side, ItemStack>> getAllNPCBookStacks(AtomicBoolean failed) {
        Queue<Triplet<Integer, Side, ItemStack>> queue = new LinkedList<>();
        try (PreparedStatement statement = this.connection.prepareStatement(
                "SELECT npc_id, side, npc_book FROM %snpc_books WHERE server=?;".formatted(this.tablePrefix)
        )) {
            statement.setString(1, this.serverName);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int npcId = result.getInt("npc_id");
                    Side side = Side.fromString(result.getString("side"));
                    ItemStack book = this.plugin.getAPI().decodeItemStack(result.getString("npc_book"));
                    queue.add(Triplet.of(npcId, side, book));
                }
            }
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to retrieve all book data!", ex);
            failed.set(true);
            return queue;
        }
        return queue;
    }

    @Override
    protected Queue<Pair<String, ItemStack>> getAllFilterBookStacks(AtomicBoolean failed) {
        Queue<Pair<String, ItemStack>> queue = new LinkedList<>();
        try (PreparedStatement statement = this.connection.prepareStatement(
                "SELECT filter_name, filter_book FROM %sfilters;".formatted(this.tablePrefix)
        )) {
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String filterName = result.getString("filter_name");
                    ItemStack book = this.plugin.getAPI().decodeItemStack(result.getString("filter_book"));
                    queue.add(Pair.of(filterName, book));
                }
            }
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to retrieve all filter book data!", ex);
            failed.set(true);
            return queue;
        }
        return queue;
    }

    @Override
    protected Queue<Triplet<String, String, String>> getAllCommandFilterStacks(AtomicBoolean failed) {
        Queue<Triplet<String, String, String>> queue = new LinkedList<>();
        try (PreparedStatement statement = this.connection.prepareStatement(
                "SELECT command_name, filter_name, permission FROM %scommands;".formatted(this.tablePrefix)
        )) {
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String cmd = result.getString("command_name");
                    String filterName = result.getString("filter_name");
                    String permission = result.getString("permission");
                    queue.add(Triplet.of(cmd, filterName, permission));
                }
            }
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to retrieve all command data!", ex);
            failed.set(true);
            return queue;
        }
        return queue;
    }

    @Override
    protected Queue<Triplet<UUID, Date, ItemStack>> getAllBookSecurityStacks(AtomicBoolean failed) {
        Queue<Triplet<UUID, Date, ItemStack>> queue = new LinkedList<>();
        try (PreparedStatement statement = this.connection.prepareStatement(
                "SELECT book, timestamp, player FROM %1$ssecurity_books INNER JOIN %1$ssecurity_players USING(book_hash);".formatted(this.tablePrefix)
        )) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    UUID uuid = UUID.fromString(result.getString("player"));
                    Date date = new Date(result.getLong("timestamp"));
                    ItemStack book = this.plugin.getAPI().decodeItemStack(result.getString("book"));
                    queue.add(Triplet.of(uuid, date, book));
                }
            }
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to retrieve all book security data!", ex);
            failed.set(true);
            return queue;
        }
        return queue;
    }

    @Override
    protected void setAllNPCBookStacks(Queue<Triplet<Integer, Side, ItemStack>> queue, AtomicBoolean failed) {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO %snpc_books (npc_id, side, server, npc_book) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE npc_book=?;".formatted(this.tablePrefix)
        )) {
            Triplet<Integer, Side, ItemStack> triplet;
            while ((triplet = queue.poll()) != null) {
                String encoded = this.plugin.getAPI().encodeItemStack(triplet.getThirdValue());
                statement.setInt(1, triplet.getFirstValue());
                statement.setString(2, triplet.getSecondValue().toString());
                statement.setString(3, this.serverName);
                statement.setString(4, encoded);
                statement.setString(5, encoded);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            failed.set(true);
            this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to save all npc book data!", ex);
        }
    }

    @Override
    protected void setAllFilterBookStacks(Queue<Pair<String, ItemStack>> queue, AtomicBoolean failed) {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO %sfilters (filter_name, filter_book) VALUES(?, ?) ON DUPLICATE KEY UPDATE filter_book=?;".formatted(this.tablePrefix)
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
            this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to save all filter book data!", ex);
        }
    }

    @Override
    protected void setAllCommandFilterStacks(Queue<Triplet<String, String, String>> queue, AtomicBoolean failed) {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "INSERT INTO %scommands (command_name, filter_name, permission) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE filter_name=?, permission=?;".formatted(this.tablePrefix)
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
            this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to save all command data!", ex);
        }
    }

    @Override
    protected void setAllBookSecurityStacks(Queue<Triplet<UUID, Date, ItemStack>> queue, AtomicBoolean failed) {
        try (PreparedStatement statementPlayers = this.connection.prepareStatement(
                "INSERT INTO %ssecurity_players (player, timestamp, book_hash) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE book_hash=?;".formatted(this.tablePrefix));
             PreparedStatement statementBooks = this.connection.prepareStatement(
                     "INSERT INTO %ssecurity_books (book_hash, book) VALUES(?, ?) ON DUPLICATE KEY UPDATE book=?;".formatted(this.tablePrefix))
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
            this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to save all security player data!", ex);
        }
    }
}
