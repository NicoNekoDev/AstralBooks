package ro.niconeko.astralbooks.settings;

import org.bukkit.configuration.ConfigurationSection;

public interface SettingsSerializer {
    void load(ConfigurationSection section);
}
