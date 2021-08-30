package ro.nicuch.citizensbooks.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import ro.nicuch.citizensbooks.CitizensBooksPlugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CitizensBooksDatabase {
    private final CitizensBooksPlugin plugin;
    private Connection connection;
    private ExecutorService poolExecutor;
    private String table_prefix;
    private LoadingCache<String, ItemStack> filterBooks;
    private final Set<String> filters = new HashSet<>();
    private boolean isMySQL;

    public CitizensBooksDatabase(CitizensBooksPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean enableDatabase(Logger logger) {
        try {
            YamlConfiguration settings = this.plugin.getSettings();
            if ("mysql".equalsIgnoreCase(settings.getString("database.type", "yaml"))) {
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
                this.connection = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + database + "?user=" + user + "&password=" + pass + "&useSSL=" + sslEnabled + "&autoReconnect=true");
                this.isMySQL = true;
                logger.info("Connected to MySQL database!");
            } else if ("sqlite".equalsIgnoreCase(settings.getString("database.type", "yaml"))) {
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
            try (ResultSet preload = this.connection.createStatement().executeQuery("SELECT filter_name FROM " + this.table_prefix + "filters;")) {
                while (preload.next()) {
                    this.filters.add(preload.getString("filter_name"));
                }
            }
            this.poolExecutor = Executors.newFixedThreadPool(this.plugin.getSettings().getInt("database.threads", 2));
            this.filterBooks = CacheBuilder.newBuilder()
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .expireAfterAccess(5, TimeUnit.MINUTES)
                    .build(new CacheLoader<>() {
                        @Override
                        public ItemStack load(@NotNull String key) throws Exception {
                            return CitizensBooksDatabase.this.getFilterBookStack(key).get();
                        }
                    });
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
                    return this.decodeItemStack(result.getString("filter_book"));
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
                String encoded = this.encodeItemStack(book);
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

    protected String encodeItemStack(ItemStack item) {
        if (item != null && item.getType() != Material.AIR)
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
                dataOutput.writeObject(item.serialize());
                return Base64Coder.encodeLines(outputStream.toByteArray());
            } catch (IOException ex) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to encode item!", ex);
            }
        return "";
    }

    @SuppressWarnings("unchecked")
    protected ItemStack decodeItemStack(String data) {
        if (data != null && !data.isEmpty())
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
                BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
                return ItemStack.deserialize((Map<String, Object>) dataInput.readObject());
            } catch (IOException | ClassNotFoundException ex) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to decode item!", ex);
            }
        return null;
    }
}
