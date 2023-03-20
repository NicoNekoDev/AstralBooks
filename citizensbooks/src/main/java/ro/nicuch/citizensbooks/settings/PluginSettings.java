package ro.nicuch.citizensbooks.settings;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ro.nicuch.citizensbooks.storage.settings.StorageSettings;
import ro.nicuch.citizensbooks.utils.SettingsUtil;

import java.util.List;
import java.util.Optional;

public class PluginSettings implements SettingsSerializer {
    @Getter private boolean metricsEnabled = true;
    @Getter private boolean updateCheck = true;
    @Getter private boolean joinBookEnabled = false;
    @Getter @Setter private long joinBookLastChange = 0;
    @Getter private boolean joinBookAlwaysShow = false;
    @Getter private boolean joinBookEnableDelay = false;
    @Getter private int joinBookDelay = 0;
    @NonNull @Getter private ConfigurationSection joinBookLastSeenByPlayers = new YamlConfiguration();

    @Override
    public void load(ConfigurationSection section) {
        this.metricsEnabled = SettingsUtil.getOrSetBooleanFunction(section, "metrics", this.metricsEnabled, Optional.of(List.of(
                "Metrics can be viewed at https://bstats.org/plugin/bukkit/CitizensBooks/2454",
                "It requires server restart to take effect!"
        )));
        this.updateCheck = SettingsUtil.getOrSetBooleanFunction(section, "update_check", this.updateCheck);
        this.joinBookEnabled = SettingsUtil.getOrSetBooleanFunction(section, "join_book_enabled", this.joinBookEnabled);
        this.joinBookLastChange = SettingsUtil.getOrSetLongFunction(section, "join_book_last_change", this.joinBookLastChange, Optional.of(List.of(
                "In ticks (20 ticks = 1 second)",
                "If AuthMe is enabled, this will be ignored!"
        )));
        this.joinBookAlwaysShow = SettingsUtil.getOrSetBooleanFunction(section, "join_book_always_show", this.joinBookAlwaysShow);
        this.joinBookEnableDelay = SettingsUtil.getOrSetBooleanFunction(section, "join_book_enable_delay", this.joinBookEnableDelay);
        this.joinBookDelay = SettingsUtil.getOrSetIntFunction(section, "join_book_delay", this.joinBookDelay);
        this.joinBookLastSeenByPlayers = SettingsUtil.getOrSetSectionFunction(section, "join_book_last_seen_by_players", this.joinBookLastSeenByPlayers);
        this.storageSettings.load(SettingsUtil.getOrCreateSection(section, "storage"));
        this.messageSettings.load(SettingsUtil.getOrCreateSection(section, "messages"));
    }

    @NonNull
    @Getter
    private final StorageSettings storageSettings = new StorageSettings();

    @NonNull
    @Getter
    private final MessageSettings messageSettings = new MessageSettings();
}