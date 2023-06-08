package ro.niconeko.astralbooks.storage.settings;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.settings.Settings;

import java.util.List;
import java.util.Optional;

public class StorageH2Settings extends Settings {
    @Getter private String fileName = "database-h2";
    @Getter private boolean EncryptionEnabled = false;

    public StorageH2Settings(AstralBooksPlugin plugin) {
        super(plugin);
    }

    @Override
    public void load(ConfigurationSection section) {
        this.fileName = super.getOrSetStringFunction(section, "file_name", this.fileName);
        this.EncryptionEnabled = super.getOrSetBooleanFunction(section, "encryption_enabled", this.EncryptionEnabled, Optional.of(List.of("Enable AES encryption.", "Don't change if you don't know what you're doing!")));
    }
}
