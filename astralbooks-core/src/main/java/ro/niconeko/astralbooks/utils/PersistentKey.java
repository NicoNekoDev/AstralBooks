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
            ITEM_RIGHT_KEY = NamespacedKey.fromString("RightBookValue", plugin);
            ITEM_LEFT_KEY = NamespacedKey.fromString("LeftBookValue", plugin);
            BOOK_PASSWORD = NamespacedKey.fromString("BookPassword", plugin);
            BOOK_PASSWORD_FAILS = NamespacedKey.fromString("BookPasswordFails", plugin);
            CHUNK_TAG = NamespacedKey.fromString("BooksOnChunk", plugin);
            ENTITY_LEFT_BOOK = NamespacedKey.fromString("EntityLeftBook", plugin);
            ENTITY_RIGHT_BOOK = NamespacedKey.fromString("EntityRightBook", plugin);
            BLOCK_LOCATION_X = NamespacedKey.fromString("BookBlockXCoord", plugin);
            BLOCK_LOCATION_Y = NamespacedKey.fromString("BookBlockYCoord", plugin);
            BLOCK_LOCATION_Z = NamespacedKey.fromString("BookBlockZCoord", plugin);
            BLOCK_LEFT_BOOK = NamespacedKey.fromString("BookBlockLeft", plugin);
            BLOCK_RIGHT_BOOK = NamespacedKey.fromString("BookBlockRight", plugin);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
