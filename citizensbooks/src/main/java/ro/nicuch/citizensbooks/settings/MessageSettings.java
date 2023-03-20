package ro.nicuch.citizensbooks.settings;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ro.nicuch.citizensbooks.utils.Message;
import ro.nicuch.citizensbooks.utils.SettingsUtil;

public class MessageSettings implements SettingsSerializer {
    private ConfigurationSection section = new YamlConfiguration();

    @Override
    public void load(ConfigurationSection section) {
        this.section = section;
        for (Message msg : Message.values()) {
            SettingsUtil.getOrSetStringFunction(section, msg.getPath(), msg.getDefault());
        }
    }

    public String getMessage(Message msg) {
        return SettingsUtil.parseMessage(this.section.getString(Message.HEADER.getPath(), Message.HEADER.getDefault()))
                + SettingsUtil.parseMessage(this.section.getString(msg.getPath(), msg.getDefault()));
    }

    public String getMessageNoHeader(Message msg) {
        return SettingsUtil.parseMessage(this.section.getString(msg.getPath(), msg.getPath()));
    }
}
