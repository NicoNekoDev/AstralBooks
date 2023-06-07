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

package ro.niconeko.astralbooks.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import ro.niconeko.astralbooks.AstralBooksPlugin;

import java.lang.reflect.InvocationTargetException;

public class PersistentKey {
    public static PersistentKey ITEM_RIGHT_KEY = null;
    public static PersistentKey ITEM_LEFT_KEY = null;
    //
    public static PersistentKey BOOK_PASSWORD = null;
    public static PersistentKey BOOK_PASSWORD_FAILS = null;
    //
    public static PersistentKey CHUNK_TAG = null;
    //
    public static PersistentKey ENTITY_LEFT_BOOK = null;
    public static PersistentKey ENTITY_RIGHT_BOOK = null;
    //
    public static PersistentKey BLOCK_LOCATION_X = null;
    public static PersistentKey BLOCK_LOCATION_Y = null;
    public static PersistentKey BLOCK_LOCATION_Z = null;
    //
    public static PersistentKey BLOCK_LEFT_BOOK = null;
    public static PersistentKey BLOCK_RIGHT_BOOK = null;
    public static Class<?> NAMESPACE_KEY = null;

    public static boolean init(AstralBooksPlugin plugin) {
        try {
            try {
                NAMESPACE_KEY = Class.forName("org.bukkit.NamespacedKey");
            } catch (ClassNotFoundException ignore) {}
            ITEM_RIGHT_KEY = new PersistentKey("RightBookValue", plugin);
            ITEM_LEFT_KEY = new PersistentKey("LeftBookValue", plugin);
            BOOK_PASSWORD = new PersistentKey("BookPassword", plugin);
            BOOK_PASSWORD_FAILS = new PersistentKey("BookPasswordFails", plugin);
            CHUNK_TAG = new PersistentKey("BooksOnChunk", plugin);
            ENTITY_LEFT_BOOK = new PersistentKey("EntityLeftBook", plugin);
            ENTITY_RIGHT_BOOK = new PersistentKey("EntityRightBook", plugin);
            BLOCK_LOCATION_X = new PersistentKey("BookBlockXCoord", plugin);
            BLOCK_LOCATION_Y = new PersistentKey("BookBlockYCoord", plugin);
            BLOCK_LOCATION_Z = new PersistentKey("BookBlockZCoord", plugin);
            BLOCK_LEFT_BOOK = new PersistentKey("BookBlockLeft", plugin);
            BLOCK_RIGHT_BOOK = new PersistentKey("BookBlockRight", plugin);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    Object key;
    final String value;

    private PersistentKey(String value, AstralBooksPlugin plugin) {
        this.value = value;
        try {
            this.key = NAMESPACE_KEY != null ? NAMESPACE_KEY.getConstructor(Plugin.class, String.class).newInstance(plugin, this.value) : null;
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException |
                 InstantiationException ignore) {
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
