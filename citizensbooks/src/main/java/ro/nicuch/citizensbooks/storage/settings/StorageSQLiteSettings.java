package ro.nicuch.citizensbooks.storage.settings;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import ro.nicuch.citizensbooks.settings.SettingsSerializer;
import ro.nicuch.citizensbooks.utils.SettingsUtil;

public class StorageSQLiteSettings implements SettingsSerializer {
    @Getter private String fileName = "database.db";

    @Override
    public void load(ConfigurationSection section) {
        this.fileName = SettingsUtil.getOrSetStringFunction(section, "file_name", this.fileName);
    }
}
