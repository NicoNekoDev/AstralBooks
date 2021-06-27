package ro.nicuch.citizensbooks.utils;

import ro.nicuch.citizensbooks.CitizensBooksPlugin;

import java.util.HashMap;
import java.util.Map;

public class ConfigUpdater {

    public static boolean updateConfig(CitizensBooksPlugin plugin, int oldVersion) {
        if (oldVersion == 7) {
            Map<String, String> commandsMap = new HashMap<>();
            plugin.getSettings().getConfigurationSection("commands").getKeys(false).forEach(str -> commandsMap.put(str, plugin.getSettings().getString("commands." + str)));
            plugin.getSettings().set("commands", null);
            commandsMap.forEach((key, value) -> {
                plugin.getSettings().set("commands." + key + ".filter_name", value);
                plugin.getSettings().set("commands." + key + ".permission", "none"); //default permission is to none
            });
            plugin.getSettings().set("lang.player_not_found", Message.PLAYER_NOT_FOUND.getDefault());
            plugin.getSettings().set("lang.filter_not_found", Message.FILTER_NOT_FOUND.getDefault());
            plugin.getSettings().set("lang.usage.forceopen", Message.USAGE_FORCEOPEN.getDefault());
            plugin.getSettings().set("lang.help.forceopen", Message.HELP_FORCEOPEN.getDefault());
            plugin.getSettings().set("version", plugin.configVersion); //update the version
            plugin.saveSettings(); //save settings
            return true;
        }
        return false;
    }
}
