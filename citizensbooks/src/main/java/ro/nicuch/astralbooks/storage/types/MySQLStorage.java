package ro.nicuch.astralbooks.storage.types;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import org.bukkit.inventory.ItemStack;
import ro.nicuch.astralbooks.CitizensBooksPlugin;
import ro.nicuch.astralbooks.storage.AbstractStorage;
import ro.nicuch.astralbooks.storage.settings.StorageMySQLSettings;
import ro.nicuch.astralbooks.storage.settings.StorageSettings;
import ro.nicuch.astralbooks.utils.Side;

import java.sql.*;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class MySQLStorage extends AbstractStorage {
    private Connection connection;
    private String tablePrefix = "";
    private String serverName = "";

    public MySQLStorage(CitizensBooksPlugin plugin) {
        super(plugin);
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
        try (PreparedStatement statement = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS ? (filter_name VARCHAR(255) PRIMARY KEY, filter_book TEXT);")) {
            statement.setString(1, this.tablePrefix + "filters");
            statement.execute();
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "(MYSQL) Failed to create 'filters' table!", ex);
            return false;
        }
        try (PreparedStatement statement = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS ? (command_name VARCHAR(255) PRIMARY KEY, filter_name VARCHAR(255), permission VARCHAR(255));")) {
            statement.setString(1, this.tablePrefix + "commands");
            statement.execute();
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "(MYSQL) Failed to create 'commands' table!", ex);
            return false;
        }
        if (this.plugin.isCitizensEnabled()) {
            try (PreparedStatement statement = this.connection.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS ? (
                    npc_id INT NOT NULL,
                    side VARCHAR(32) NOT NULL DEFAULT 'right_side',
                    server VARCHAR(255) DEFAULT 'default',
                    npc_book TEXT,
                    CONSTRAINT npc_id_side PRIMARY KEY (npc_id, side)
                    );
                    """)) {
                statement.setString(1, this.tablePrefix + "npc_books");
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.SEVERE, "(MYSQL) Failed to create 'npcbooks' table!", ex);
                return false;
            }
        }
        try (PreparedStatement statement = this.connection.prepareStatement("SELECT filter_name FROM ?;")) {
            statement.setString(1, this.tablePrefix + "filters");
            try (ResultSet preload = statement.executeQuery()) {
                while (preload.next()) {
                    super.cache.filters.add(preload.getString("filter_name"));
                }
            }
        }
        try (PreparedStatement statement = this.connection.prepareStatement("SELECT command_name FROM ?;")) {
            statement.setString(1, this.tablePrefix + "commands");
            try (ResultSet preload = statement.executeQuery()) {
                while (preload.next()) {
                    super.cache.commands.add(preload.getString("command_name"));
                }
            }
        }
        if (this.plugin.isCitizensEnabled()) {
            try (PreparedStatement statement = this.connection.prepareStatement("SELECT npc_id, side FROM ?;")) {
                statement.setString(1, this.tablePrefix + "npc_books");
                try (ResultSet preload = statement.executeQuery()) {
                    while (preload.next()) {
                        super.cache.npcs.add(Pair.of(preload.getInt("npc_id"), Side.fromString(preload.getString("side"))));
                    }
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
            try (PreparedStatement statement = this.connection.prepareStatement("SELECT filter_book FROM ? WHERE filter_name=?;")) {
                statement.setString(1, this.tablePrefix + "filters");
                statement.setString(2, filterName);
                try (ResultSet result = statement.executeQuery()) {
                    return this.plugin.getAPI().decodeItemStack(result.getString("filter_book"));
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to retrieve book data!", ex);
                return null;
            }
        });
    }

    @Override
    protected Future<ItemStack> getNPCBookStack(int npcId, Side side) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("SELECT npc_book FROM ? WHERE npc_id=? AND side=? AND server=?;")) {
                statement.setString(1, this.tablePrefix + "npc_books");
                statement.setInt(2, npcId);
                statement.setString(3, side.toString());
                statement.setString(4, this.serverName);
                try (ResultSet result = statement.executeQuery()) {
                    return this.plugin.getAPI().decodeItemStack(result.getString("npc_book"));
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
            try (PreparedStatement statement = this.connection.prepareStatement("SELECT filter_name, permission FROM ? WHERE command_name=?;")) {
                statement.setString(1, this.tablePrefix + "commands");
                statement.setString(2, cmd);
                try (ResultSet result = statement.executeQuery()) {
                    return Pair.of(result.getString("filter_name"), result.getString("permission"));
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to retrieve command data!", ex);
                return null;
            }
        });
    }

    @Override
    protected void removeNPCBookStack(int npcId, Side side) {
        if (!this.plugin.isCitizensEnabled())
            throw new IllegalStateException("Citizens is not enabled!");
        super.cache.npcs.remove(Pair.of(npcId, side));
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("DELETE FROM ? WHERE npc_id=? AND side=? AND server=?;")) {
                statement.setString(1, this.tablePrefix + "npc_books");
                statement.setInt(2, npcId);
                statement.setString(3, side.toString());
                statement.setString(4, this.serverName);
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
            try (PreparedStatement statement = this.connection.prepareStatement("DELETE FROM ? WHERE filter_name=?;")) {
                statement.setString(1, this.tablePrefix + "filters");
                statement.setString(2, filterName);
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
            try (PreparedStatement statement = this.connection.prepareStatement("DELETE FROM ? WHERE command_name=?;")) {
                statement.setString(1, this.tablePrefix + "commands");
                statement.setString(2, cmd);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to remove command data!", ex);
            }
        });
    }

    @Override
    protected void putNPCBookStack(int npcId, Side side, ItemStack book) {
        if (!this.plugin.isCitizensEnabled())
            throw new IllegalStateException("Citizens is not enabled!");
        Pair<Integer, Side> pairKey = Pair.of(npcId, side);
        super.cache.npcs.add(pairKey);
        super.cache.npcBooks.put(pairKey, book);
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("INSERT INTO ? (npc_id, side, server, npc_book) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE npc_book=?;")) {
                String encoded = this.plugin.getAPI().encodeItemStack(book);
                statement.setString(1, this.tablePrefix + "npc_books");
                statement.setInt(2, npcId);
                statement.setString(3, side.toString());
                statement.setString(4, this.serverName);
                statement.setString(5, encoded);
                statement.setString(6, encoded);
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
            try (PreparedStatement statement = this.connection.prepareStatement("INSERT INTO ? (filter_name, filter_book) VALUES(?, ?) ON DUPLICATE KEY UPDATE filter_book=?;")) {
                String encoded = this.plugin.getAPI().encodeItemStack(book);
                statement.setString(1, this.tablePrefix + "filters");
                statement.setString(2, filterName);
                statement.setString(3, encoded);
                statement.setString(4, encoded);
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
            try (PreparedStatement statement = this.connection.prepareStatement("INSERT INTO ? (command_name, filter_name, permission) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE filter_name=?, permission=?;")) {
                statement.setString(1, this.tablePrefix + "commands");
                statement.setString(2, cmd);
                statement.setString(3, filterName);
                statement.setString(4, permission);
                statement.setString(5, filterName);
                statement.setString(6, permission);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(MYSQL) Failed to save command data!", ex);
            }
        });
    }
}
