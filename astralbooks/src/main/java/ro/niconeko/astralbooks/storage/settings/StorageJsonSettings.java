package ro.niconeko.astralbooks.storage.settings;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import ro.niconeko.astralbooks.settings.SettingsSerializer;
import ro.niconeko.astralbooks.utils.SettingsUtil;

public class StorageJsonSettings implements SettingsSerializer {
    @Getter private int saveInterval = 60;

    @Override
    public void load(ConfigurationSection section) {
        this.saveInterval = SettingsUtil.getOrSetIntFunction(section, "save_interval", this.saveInterval);
    }
}
