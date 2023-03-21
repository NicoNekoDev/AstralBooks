package ro.niconeko.astralbooks.storage.settings;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import ro.niconeko.astralbooks.settings.SettingsSerializer;
import ro.niconeko.astralbooks.storage.StorageType;
import ro.niconeko.astralbooks.utils.SettingsUtil;

import java.util.List;
import java.util.Optional;

public class StorageSettings implements SettingsSerializer {
    @Getter
    private StorageType databaseType = StorageType.JSON;

    @Getter
    private int databaseThreads = 2;

    @Getter
    private final StorageMySQLSettings MySQLSettings = new StorageMySQLSettings();

    @Getter
    private final StorageSQLiteSettings SQLiteSettings = new StorageSQLiteSettings();

    @Getter
    private final StorageJsonSettings JsonSettings = new StorageJsonSettings();

    @Override
    public void load(ConfigurationSection section) {
        this.databaseType = StorageType.fromString(SettingsUtil.getOrSetStringFunction(section, "type", this.databaseType.toString(), Optional.of(List.of("Options: json, sqlite, mysql"))));
        this.databaseThreads = SettingsUtil.getOrSetIntFunction(section, "threads", this.databaseThreads, Optional.of(List.of("Number of threads the cache will use")));
        this.JsonSettings.load(SettingsUtil.getOrCreateSection(section, "json"));
        this.SQLiteSettings.load(SettingsUtil.getOrCreateSection(section, "sqlite"));
        this.MySQLSettings.load(SettingsUtil.getOrCreateSection(section, "mysql"));
    }
}
