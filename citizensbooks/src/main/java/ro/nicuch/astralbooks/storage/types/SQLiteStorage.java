package ro.nicuch.astralbooks.storage.types;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import org.bukkit.inventory.ItemStack;
import ro.nicuch.astralbooks.CitizensBooksPlugin;
import ro.nicuch.astralbooks.storage.AbstractStorage;
import ro.nicuch.astralbooks.storage.settings.StorageSettings;
import ro.nicuch.astralbooks.utils.Side;

import java.io.File;
import java.sql.*;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class SQLiteStorage extends AbstractStorage {
    private Connection connection;

    public SQLiteStorage(CitizensBooksPlugin plugin) {
        super(plugin);
    }

    protected boolean load(StorageSettings settings) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + super.plugin.getDataFolder() + File.separator + settings.getSQLiteSettings().getFileName());
        try (PreparedStatement statement = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS 'filters' (filter_name VARCHAR(255) PRIMARY KEY, filter_book TEXT);")) {
            statement.execute();
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "(SQLite) Failed to create 'filters' table!", ex);
            return false;
        }
        try (PreparedStatement statement = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS 'commands' (command_name VARCHAR(255) PRIMARY KEY, filter_name VARCHAR(255), permission VARCHAR(255));")) {
            statement.execute();
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "(SQLite) Failed to create 'commands' table!", ex);
            return false;
        }
        if (this.plugin.isCitizensEnabled()) {
            try (PreparedStatement statement = this.connection.prepareStatement("""
                    CREATE TABLE IF NOT EXISTS 'npc_books' (
                    npc_id INT NOT NULL,
                    side VARCHAR(32) NOT NULL DEFAULT 'right_side',
                    npc_book TEXT,
                    PRIMARY KEY (npc_id, side)
                    );
                    """)) {
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.SEVERE, "(SQLite) Failed to create 'npcbooks' table!", ex);
                return false;
            }
        }
        try (PreparedStatement statement = this.connection.prepareStatement("SELECT filter_name FROM 'filters';")) {
            try (ResultSet preload = statement.executeQuery()) {
                while (preload.next()) {
                    super.cache.filters.add(preload.getString("filter_name"));
                }
            }
        }
        try (PreparedStatement statement = this.connection.prepareStatement("SELECT command_name FROM 'commands';")) {
            try (ResultSet preload = statement.executeQuery()) {
                while (preload.next()) {
                    super.cache.commands.add(preload.getString("command_name"));
                }
            }
        }
        if (this.plugin.isCitizensEnabled()) {
            try (PreparedStatement statement = this.connection.prepareStatement("SELECT npc_id, side FROM 'npc_books';")) {
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
            this.plugin.getLogger().log(Level.SEVERE, "(SQLite) Failed to unload database!", ex);
        }
    }

    @Override
    protected Future<ItemStack> getFilterBookStack(String filterName) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("SELECT filter_book FROM 'filters' WHERE filter_name=?;")) {
                statement.setString(1, filterName);
                try (ResultSet result = statement.executeQuery()) {
                    return this.plugin.getAPI().decodeItemStack(result.getString("filter_book"));
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(SQLite) Failed to retrieve book data!", ex);
                return null;
            }
        });
    }

    @Override
    protected Future<ItemStack> getNPCBookStack(int npcId, Side side) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("SELECT npc_book FROM 'npc_books' WHERE npc_id=? AND side=?;")) {
                statement.setInt(1, npcId);
                statement.setString(2, side.toString());
                try (ResultSet result = statement.executeQuery()) {
                    return this.plugin.getAPI().decodeItemStack(result.getString("npc_book"));
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(SQLite) Failed to retrieve book data!", ex);
                return null;
            }
        });
    }

    @Override
    protected Future<Pair<String, String>> getCommandFilterStack(String cmd) {
        return super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("SELECT filter_name, permission FROM 'commands' WHERE command_name=?;")) {
                statement.setString(1, cmd);
                try (ResultSet result = statement.executeQuery()) {
                    return Pair.of(result.getString("filter_name"), result.getString("permission"));
                }
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(SQLite) Failed to retrieve command data!", ex);
                return null;
            }
        });
    }

    @Override
    protected void removeNPCBookStack(int npcId, Side side) {
        if (!this.plugin.isCitizensEnabled()) throw new IllegalStateException("Citizens is not enabled!");
        super.cache.npcs.remove(Pair.of(npcId, side));
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("DELETE FROM 'npc_books' WHERE npc_id=? AND side=?;")) {
                statement.setInt(1, npcId);
                statement.setString(2, side.toString());
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(SQLite) Failed to remove book data!", ex);
            }
        });
    }

    @Override
    protected void removeFilterBookStack(String filterName) {
        super.cache.filters.remove(filterName);
        super.cache.filterBooks.invalidate(filterName);
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("DELETE FROM 'filters' WHERE filter_name=?;")) {
                statement.setString(1, filterName);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(SQLite) Failed to remove book data!", ex);
            }
        });
    }

    @Override
    protected void removeCommandFilterStack(String cmd) {
        super.cache.commands.remove(cmd);
        super.cache.commandFilters.invalidate(cmd);
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("DELETE FROM 'commands' WHERE command_name=?;")) {
                statement.setString(1, cmd);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(SQLite) Failed to remove command data!", ex);
            }
        });
    }

    @Override
    protected void putNPCBookStack(int npcId, Side side, ItemStack book) {
        if (!this.plugin.isCitizensEnabled()) throw new IllegalStateException("Citizens is not enabled!");
        Pair<Integer, Side> pairKey = Pair.of(npcId, side);
        super.cache.npcs.add(pairKey);
        super.cache.npcBooks.put(pairKey, book);
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("INSERT INTO 'npc_books' (npc_id, side, npc_book) VALUES(?, ?, ?) ON CONFLICT(npc_id, side) DO UPDATE SET npc_book=?;")) {
                String encoded = this.plugin.getAPI().encodeItemStack(book);
                statement.setInt(1, npcId);
                statement.setString(2, side.toString());
                statement.setString(3, encoded);
                statement.setString(4, encoded);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(SQLite) Failed to save book data!", ex);
            }
        });
    }

    @Override
    protected void putFilterBookStack(String filterName, ItemStack book) {
        super.cache.filters.add(filterName);
        super.cache.filterBooks.put(filterName, book);
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("INSERT INTO 'filters' (filter_name, filter_book) VALUES(?, ?) ON CONFLICT(filter_name) DO UPDATE SET filter_book=?;")) {
                String encoded = this.plugin.getAPI().encodeItemStack(book);
                statement.setString(1, filterName);
                statement.setString(2, encoded);
                statement.setString(3, encoded);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(SQLite) Failed to save book data!", ex);
            }
        });
    }

    @Override
    protected void putCommandFilterStack(String cmd, String filterName, String permission) {
        super.cache.commands.add(cmd);
        super.cache.commandFilters.put(cmd, Pair.of(filterName, permission));
        super.cache.poolExecutor.submit(() -> {
            try (PreparedStatement statement = this.connection.prepareStatement("INSERT INTO 'commands' (command_name, filter_name, permission) VALUES(?, ?, ?) ON CONFLICT(command_name) DO UPDATE SET filter_name=?, permission=?;")) {
                statement.setString(1, cmd);
                statement.setString(2, filterName);
                statement.setString(3, permission);
                statement.setString(4, filterName);
                statement.setString(5, permission);
                statement.executeUpdate();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.WARNING, "(SQLite) Failed to save book data!", ex);
            }
        });
    }
}
