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

package ro.nicuch.citizensbooks.item;

import org.bukkit.inventory.ItemStack;
import ro.nicuch.citizensbooks.utils.PersistentKey;

public interface ItemData {

    boolean hasStringKey(PersistentKey key);

    void putString(PersistentKey key, String value);

    String getString(PersistentKey key);

    boolean hasIntKey(PersistentKey key);

    void putInt(PersistentKey key, int value);

    int getInt(PersistentKey key);

    void removeKey(PersistentKey key);

    ItemStack build();

    ItemStack copyDataToStack(ItemStack stack);
}
