package ro.niconeko.astralbooks.storage.settings;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.settings.Settings;

public class StorageJsonSettings extends Settings {
    @Getter private int saveInterval = 60;

    public StorageJsonSettings(AstralBooksPlugin plugin) {
        super(plugin);
    }

    @Override
    public void load(ConfigurationSection section) {
        this.saveInterval = super.getOrSetIntFunction(section, "save_interval", this.saveInterval);
    }
}
