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

package ro.niconeko.astralbooks.utils;

import org.bukkit.NamespacedKey;
import ro.niconeko.astralbooks.AstralBooksPlugin;

import java.util.logging.Level;

public class PersistentKey {
    public static NamespacedKey ITEM_RIGHT_KEY = null;
    public static NamespacedKey ITEM_LEFT_KEY = null;
    //
    public static NamespacedKey BOOK_PASSWORD = null;
    public static NamespacedKey BOOK_PASSWORD_FAILS = null;
    //
    public static NamespacedKey CHUNK_TAG = null;
    //
    public static NamespacedKey ENTITY_LEFT_BOOK = null;
    public static NamespacedKey ENTITY_RIGHT_BOOK = null;
    //
    public static NamespacedKey BLOCK_LOCATION_X = null;
    public static NamespacedKey BLOCK_LOCATION_Y = null;
    public static NamespacedKey BLOCK_LOCATION_Z = null;
    //
    public static NamespacedKey BLOCK_LEFT_BOOK = null;
    public static NamespacedKey BLOCK_RIGHT_BOOK = null;

    public static boolean init(AstralBooksPlugin plugin) {
        try {
            ITEM_RIGHT_KEY = new NamespacedKey(plugin, "RightBookValue");
            ITEM_LEFT_KEY = new NamespacedKey(plugin, "LeftBookValue");
            BOOK_PASSWORD = new NamespacedKey(plugin, "BookPassword");
            BOOK_PASSWORD_FAILS = new NamespacedKey(plugin, "BookPasswordFails");
            CHUNK_TAG = new NamespacedKey(plugin, "BooksOnChunk");
            ENTITY_LEFT_BOOK = new NamespacedKey(plugin, "EntityLeftBook");
            ENTITY_RIGHT_BOOK = new NamespacedKey(plugin, "EntityRightBook");
            BLOCK_LOCATION_X = new NamespacedKey(plugin, "BookBlockXCoord");
            BLOCK_LOCATION_Y = new NamespacedKey(plugin, "BookBlockYCoord");
            BLOCK_LOCATION_Z = new NamespacedKey(plugin, "BookBlockZCoord");
            BLOCK_LEFT_BOOK = new NamespacedKey(plugin, "BookBlockLeft");
            BLOCK_RIGHT_BOOK = new NamespacedKey(plugin, "BookBlockRight");
            return true;
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load NamespacedKeys", ex);
            return false;
        }
    }
}
