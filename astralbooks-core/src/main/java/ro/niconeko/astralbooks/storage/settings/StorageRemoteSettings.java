package ro.niconeko.astralbooks.storage.settings;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.settings.Settings;

import java.util.List;
import java.util.Optional;

public class StorageRemoteSettings extends Settings {
    @Getter private String host = "localhost";
    @Getter private int port = 3306;
    @Getter private String database = "astralbooks";
    @Getter private String username = "root";
    @Getter private String password = "";
    @Getter private boolean SSLEnabled = false;
    @Getter private String tablePrefix = "abooks_";
    @Getter private String serverName = "default";

    public StorageRemoteSettings(AstralBooksPlugin plugin) {
        super(plugin);
    }

    @Override
    public void load(ConfigurationSection section) {
        this.host = super.getOrSetStringFunction(section, "host", this.host);
        this.port = super.getOrSetIntFunction(section, "port", this.port);
        this.database = super.getOrSetStringFunction(section, "database", this.database);
        this.username = super.getOrSetStringFunction(section, "username", this.username);
        this.password = super.getOrSetStringFunction(section, "password", this.password);
        this.SSLEnabled = super.getOrSetBooleanFunction(section, "ssl_enabled", this.SSLEnabled, Optional.of(List.of("If SSL encryption is enabled.")));
        this.tablePrefix = super.getOrSetStringFunction(section, "table_prefix", this.tablePrefix, Optional.of(List.of("The prefix for the table name.")));
        this.serverName = super.getOrSetStringFunction(section, "server_name", this.serverName, Optional.of(List.of("Use this if you have multiple servers using the same database.")));
    }
}
