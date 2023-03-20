package ro.nicuch.citizensbooks.settings;

import org.bukkit.configuration.ConfigurationSection;

public interface SettingsSerializer {
    void load(ConfigurationSection section);
}
