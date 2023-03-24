package ro.niconeko.astralbooks.settings;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.settings.StorageSettings;

import java.util.List;
import java.util.Optional;

public class PluginSettings extends Settings {
    @Getter private boolean metricsEnabled = true;
    @Getter private boolean updateCheck = true;
    @Getter @Setter private boolean joinBookEnabled = false;
    @Getter private boolean joinBookAlwaysShow = false;
    @Getter private boolean joinBookEnableDelay = false;
    @Getter private int joinBookDelay = 0;

    public PluginSettings(AstralBooksPlugin plugin) {
        super(plugin);
    }

    @Override
    public void load(ConfigurationSection section) {
        this.metricsEnabled = super.getOrSetBooleanFunction(section, "metrics", this.metricsEnabled, Optional.of(List.of(
                "Metrics can be viewed at https://bstats.org/plugin/bukkit/AstralBooks/18026",
                "It requires server restart to take effect!"
        )));
        this.updateCheck = super.getOrSetBooleanFunction(section, "update_check", this.updateCheck);
        this.joinBookEnabled = super.getOrSetBooleanFunction(section, "join_book_enabled", this.joinBookEnabled);
        this.joinBookAlwaysShow = super.getOrSetBooleanFunction(section, "join_book_always_show", this.joinBookAlwaysShow);
        this.joinBookEnableDelay = super.getOrSetBooleanFunction(section, "join_book_enable_delay", this.joinBookEnableDelay);
        this.joinBookDelay = super.getOrSetIntFunction(section, "join_book_delay", this.joinBookDelay);
        this.storageSettings.load(super.getOrCreateSection(section, "storage"));
        this.messageSettings.load(super.getOrCreateSection(section, "messages"));
    }

    @NonNull
    @Getter
    private final StorageSettings storageSettings = new StorageSettings(super.plugin);

    @NonNull
    @Getter
    private final MessageSettings messageSettings = new MessageSettings(super.plugin);
}