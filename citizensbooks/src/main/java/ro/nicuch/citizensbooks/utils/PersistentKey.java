/*

    CitizensBooks
    Copyright (c) 2022 @ Drăghiciu 'NicoNekoDev' Nicolae

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 */

package ro.nicuch.citizensbooks.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import ro.nicuch.citizensbooks.CitizensBooksPlugin;

import java.lang.reflect.InvocationTargetException;

public class PersistentKey {
    public static PersistentKey ITEM_RIGHT_KEY = null;
    public static PersistentKey ITEM_LEFT_KEY = null;
    public static PersistentKey BOOK_PASSWORD = null;
    public static PersistentKey BOOK_PASSWORD_FAILS = null;

    public static boolean init(CitizensBooksPlugin plugin) {
        try {
            ITEM_RIGHT_KEY = new PersistentKey("RightBookValue", plugin);
            ITEM_LEFT_KEY = new PersistentKey("LeftBookValue", plugin);
            BOOK_PASSWORD = new PersistentKey("BookPassword", plugin);
            BOOK_PASSWORD_FAILS = new PersistentKey("BookPasswordFails", plugin);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    Object key;
    final String value;

    private PersistentKey(String value, CitizensBooksPlugin plugin) {
        this.value = value;
        try {
            this.key = Class.forName("org.bukkit.NamespacedKey").getConstructor(Plugin.class, String.class).newInstance(plugin, this.value);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException | ClassNotFoundException | InstantiationException ignore) {
            this.key = null;
        }
    }

    public final NamespacedKey getKey() {
        return (NamespacedKey) this.key;
    }

    public final String getValue() {
        return this.value;
    }
}