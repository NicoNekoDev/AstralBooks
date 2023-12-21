/*
 *     CitizensBooks
 *     Copyright (c) 2023 @ Drăghiciu 'NicoNekoDev' Nicolae
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package ro.niconeko.astralbooks.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.utils.settings.Setting;
import ro.niconeko.astralbooks.values.Messages;
import ro.niconeko.astralbooks.values.Settings;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public class SettingsManager {

    public static boolean loadAndSave(AstralBooksPlugin plugin) {
        try {
            final File settingsFile = new File(plugin.getDataFolder(), "settings.yml");
            final YamlConfiguration settings = YamlConfiguration.loadConfiguration(settingsFile);
            loadValues(settings, Settings.class);
            settings.options().setHeader(List.of("""
                                     _             _ ____              _       \s
                           /\\       | |           | |  _ \\            | |      \s
                          /  \\   ___| |_ _ __ __ _| | |_) | ___   ___ | | _____\s
                         / /\\ \\ / __| __| '__/ _` | |  _ < / _ \\ / _ \\| |/ / __|
                        / ____ \\\\__ \\ |_| | | (_| | | |_) | (_) | (_) |   <\\__ \\
                       /_/    \\_\\___/\\__|_|  \\__,_|_|____/ \\___/ \\___/|_|\\_\\___/
                                                                               \s
                                                                               \s
                    """.split("\n")));
            settings.save(settingsFile);
            //
            final File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
            final YamlConfiguration messages = YamlConfiguration.loadConfiguration(messagesFile);
            loadValues(messages, Messages.class);
            messages.save(messagesFile);

            return true;
        } catch (IOException | IllegalAccessException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static void loadValues(ConfigurationSection section, Class<?> clazz) throws IllegalAccessException {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.get(null) instanceof Setting<?> setting)
                setting.load(section);
        }
    }
}
