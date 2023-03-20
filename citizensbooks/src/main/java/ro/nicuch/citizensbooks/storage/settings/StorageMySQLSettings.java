package ro.nicuch.citizensbooks.storage.settings;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import ro.nicuch.citizensbooks.settings.SettingsSerializer;
import ro.nicuch.citizensbooks.utils.SettingsUtil;

import java.util.List;
import java.util.Optional;

public class StorageMySQLSettings implements SettingsSerializer {
    @Getter private String host = "localhost";
    @Getter private int port = 3306;
    @Getter private String database = "citizensbooks";
    @Getter private String username = "root";
    @Getter private String password = "";
    @Getter private boolean SSLEnabled = false;
    @Getter private String tablePrefix = "cbooks_";
    @Getter private String serverName = "default";

    @Override
    public void load(ConfigurationSection section) {
        this.host = SettingsUtil.getOrSetStringFunction(section, "host", this.host);
        this.port = SettingsUtil.getOrSetIntFunction(section, "port", this.port);
        this.database = SettingsUtil.getOrSetStringFunction(section, "database", this.database);
        this.username = SettingsUtil.getOrSetStringFunction(section, "username", this.username);
        this.password = SettingsUtil.getOrSetStringFunction(section, "password", this.password);
        this.SSLEnabled = SettingsUtil.getOrSetBooleanFunction(section, "ssl_enabled", this.SSLEnabled, Optional.of(List.of("If SSL encryption is enabled.")));
        this.tablePrefix = SettingsUtil.getOrSetStringFunction(section, "table_prefix", this.tablePrefix, Optional.of(List.of("The prefix for the table name.")));
        this.serverName = SettingsUtil.getOrSetStringFunction(section, "server_name", this.serverName, Optional.of(List.of("Use this if you have multiple servers using the same database.")));
    }
}
