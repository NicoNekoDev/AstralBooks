package ro.nicuch.citizensbooks;

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
            plugin.getSettings().set("lang.player_not_found", ConfigDefaults.player_not_found);
            plugin.getSettings().set("lang.filter_not_found", ConfigDefaults.filter_not_found);
            plugin.getSettings().set("lang.usage.forceopen", ConfigDefaults.usage_forceopen);
            plugin.getSettings().set("lang.help.forceopen", ConfigDefaults.help_forceopen);
            plugin.getSettings().set("version", plugin.configVersion); //update the version
            plugin.saveSettings(); //save settings
            return true;
        }
        return false;
    }
}
