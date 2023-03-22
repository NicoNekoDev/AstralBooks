package ro.niconeko.astralbooks.storage.settings;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.settings.Settings;
import ro.niconeko.astralbooks.storage.StorageType;

import java.util.List;
import java.util.Optional;

public class StorageSettings extends Settings {
    @Getter private StorageType databaseType = StorageType.JSON;
    @Getter private int databaseThreads = 2;
    @Getter private final StorageMySQLSettings MySQLSettings = new StorageMySQLSettings(super.plugin);
    @Getter private final StorageSQLiteSettings SQLiteSettings = new StorageSQLiteSettings(super.plugin);
    @Getter private final StorageJsonSettings JsonSettings = new StorageJsonSettings(super.plugin);

    public StorageSettings(AstralBooksPlugin plugin) {
        super(plugin);
    }

    @Override
    public void load(ConfigurationSection section) {
        this.databaseType = StorageType.fromString(super.getOrSetStringFunction(section, "type", this.databaseType.toString(), Optional.of(List.of("Options: json, sqlite, mysql"))));
        this.databaseThreads = super.getOrSetIntFunction(section, "threads", this.databaseThreads, Optional.of(List.of("Number of threads the cache will use")));
        this.JsonSettings.load(super.getOrCreateSection(section, "json"));
        this.SQLiteSettings.load(super.getOrCreateSection(section, "sqlite"));
        this.MySQLSettings.load(super.getOrCreateSection(section, "mysql"));
    }
}
