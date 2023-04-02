package ro.niconeko.astralbooks.settings;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.utils.Message;

public class MessageSettings extends Settings {
    private ConfigurationSection section = new YamlConfiguration();

    public MessageSettings(AstralBooksPlugin plugin) {
        super(plugin);
    }

    @Override
    public void load(ConfigurationSection section) {
        this.section = section;
        for (Message msg : Message.values())
            super.getOrSetStringFunction(section, msg.getPath(), msg.getDefault(), msg.getComments());
    }

    public String getMessage(Message msg) {
        return super.parseMessage(this.section.getString(Message.HEADER.getPath(), Message.HEADER.getDefault()))
                + super.parseMessage(this.section.getString(msg.getPath(), msg.getDefault()));
    }

    public String getMessageNoHeader(Message msg) {
        return super.parseMessage(this.section.getString(msg.getPath(), msg.getPath()));
    }
}
