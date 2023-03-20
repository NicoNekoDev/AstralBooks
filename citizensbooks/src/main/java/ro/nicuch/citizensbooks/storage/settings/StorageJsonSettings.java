package ro.nicuch.citizensbooks.storage.settings;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import ro.nicuch.citizensbooks.settings.SettingsSerializer;
import ro.nicuch.citizensbooks.utils.SettingsUtil;

public class StorageJsonSettings implements SettingsSerializer {
    @Getter private int saveInterval = 60;

    @Override
    public void load(ConfigurationSection section) {
        this.saveInterval = SettingsUtil.getOrSetIntFunction(section, "save_interval", this.saveInterval);
    }
}
