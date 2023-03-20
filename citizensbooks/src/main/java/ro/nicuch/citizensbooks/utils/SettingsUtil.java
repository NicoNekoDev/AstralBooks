package ro.nicuch.citizensbooks.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") //Piss off java
public class SettingsUtil {

    public static String parseMessage(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static ConfigurationSection getOrCreateSection(ConfigurationSection config, String path) {
        return getOrCreateSection(config, path, Optional.empty());
    }

    public static ConfigurationSection getOrCreateSection(ConfigurationSection config, String path, Optional<List<String>> comments) {
        ConfigurationSection value = config.isConfigurationSection(path) ? config.getConfigurationSection(path) : config.createSection(path);
        comments.ifPresent(strings -> config.setComments(path, strings));
        return value;
    }

    public static String getOrSetStringFunction(ConfigurationSection config, String path, String defaultValue) {
        return getOrSetStringFunction(config, path, defaultValue, Optional.empty());
    }

    public static String getOrSetStringFunction(ConfigurationSection config, String path, String defaultValue, Optional<List<String>> comments) {
        String value = defaultValue;
        if (config.isString(path)) {
            value = config.getString(path);
        } else {
            config.set(path, value);
            comments.ifPresent(strings -> config.setComments(path, strings));
        }
        return value;
    }

    public static int getOrSetIntFunction(ConfigurationSection config, String path, int defaultValue) {
        return getOrSetIntFunction(config, path, defaultValue, Optional.empty());
    }

    public static int getOrSetIntFunction(ConfigurationSection config, String path, int defaultValue, Optional<List<String>> comments) {
        int value = defaultValue;
        if (config.isInt(path)) {
            value = config.getInt(path);
        } else {
            config.set(path, value);
            comments.ifPresent(strings -> config.setComments(path, strings));
        }
        return value;
    }

    public static long getOrSetLongFunction(ConfigurationSection config, String path, long defaultValue) {
        return getOrSetLongFunction(config, path, defaultValue, Optional.empty());
    }

    public static long getOrSetLongFunction(ConfigurationSection config, String path, long defaultValue, Optional<List<String>> comments) {
        long value = defaultValue;
        if (config.isLong(path)) {
            value = config.getLong(path);
        } else {
            config.set(path, value);
            comments.ifPresent(strings -> config.setComments(path, strings));
        }
        return value;
    }

    public static ConfigurationSection getOrSetSectionFunction(ConfigurationSection config, String path, ConfigurationSection defaultValue) {
        return getOrSetSectionFunction(config, path, defaultValue, Optional.empty());
    }

    public static ConfigurationSection getOrSetSectionFunction(ConfigurationSection config, String path, ConfigurationSection defaultValue, Optional<List<String>> comments) {
        ConfigurationSection value = defaultValue;
        if (config.isConfigurationSection(path)) {
            value = config.getConfigurationSection(path);
        } else {
            config.set(path, value);
            comments.ifPresent(strings -> config.setComments(path, strings));
        }
        return value;
    }

    public static boolean getOrSetBooleanFunction(ConfigurationSection config, String path, boolean defaultValue) {
        return getOrSetBooleanFunction(config, path, defaultValue, Optional.empty());
    }

    public static boolean getOrSetBooleanFunction(ConfigurationSection config, String path, boolean defaultValue, Optional<List<String>> comments) {
        boolean value = defaultValue;
        if (config.isBoolean(path)) {
            value = config.getBoolean(path);
        } else {
            config.set(path, value);
            comments.ifPresent(strings -> config.setComments(path, strings));
        }
        return value;
    }

    public static List<String> getOrSetStringCollectionFunction(ConfigurationSection config, String path, Collection<String> defaultValue) {
        return getOrSetStringCollectionFunction(config, path, defaultValue, Optional.empty());
    }

    public static List<String> getOrSetStringCollectionFunction(ConfigurationSection config, String path, Collection<String> defaultValue, Optional<List<String>> comments) {
        List<String> value = new ArrayList<>(defaultValue);
        if (config.isList(path)) {
            value = config.getStringList(path);
        } else {
            config.set(path, value);
            comments.ifPresent(strings -> config.setComments(path, strings));
        }
        defaultValue.clear();
        return value;
    }
}
