package ro.niconeko.astralbooks.storage.settings;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.settings.Settings;

public class StorageSQLiteSettings extends Settings {
    @Getter private String fileName = "database-sql.db";

    public StorageSQLiteSettings(AstralBooksPlugin plugin) {
        super(plugin);
    }

    @Override
    public void load(ConfigurationSection section) {
        this.fileName = super.getOrSetStringFunction(section, "file_name", this.fileName);
    }
}
