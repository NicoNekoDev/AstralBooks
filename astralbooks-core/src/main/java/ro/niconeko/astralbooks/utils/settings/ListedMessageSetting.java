package ro.niconeko.astralbooks.utils.settings;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import ro.niconeko.astralbooks.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ListedMessageSetting extends Setting<List<List<String>>> {

    public ListedMessageSetting(String key, List<List<String>> defaultValue, List<String> comments, List<String> inlineComments) {
        super(key, defaultValue, comments, inlineComments);
    }

    public ListedMessageSetting(String key, List<List<String>> defaultValue, List<String> comments) {
        super(key, defaultValue, comments, List.of());
    }

    public ListedMessageSetting(String key, List<List<String>> defaultValue) {
        super(key, defaultValue, List.of(), List.of());
    }

    @Override
    public final ListedMessageSetting load(ConfigurationSection section) {
        if (section.isList(super.key)) {
            List<List<String>> result = new ArrayList<>();
            int index = 0;
            for (String string : section.getStringList(super.key)) {
                if (string.equals("[separator]"))
                    index++;
                else {
                    if (index >= result.size())
                        result.add(new ArrayList<>());
                    result.get(index).add(string);
                }
            }
            this.value = result;
        } else {
            List<String> result = new ArrayList<>();
            for (List<String> list : super.defaultValue) {
                result.addAll(list);
                result.add("[separator]");
            }
            section.set(super.key, result);
            if (!this.comments.isEmpty()) section.setComments(this.key, this.comments);
            if (!this.inlineComments.isEmpty()) section.setInlineComments(this.key, this.inlineComments);
            this.value = this.defaultValue;
        }
        return this;
    }

    public int send(CommandSender sender, int page) {
        return this.send(sender, page, Function.identity());
    }

    public int send(CommandSender sender, int page, Function<String, String> replacer) {
        if (page < 0)
            page = 0;
        if (page >= super.value.size())
            page = super.value.size() - 1;
        for (String message : super.value.get(page)) {
            message = replacer.apply(message);
            if (message != null && !message.isEmpty())
                MessageUtils.sendMessage(sender, message);
        }
        return super.value.size();
    }
}
