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

package ro.niconeko.astralbooks.persistent.item;

import org.bukkit.inventory.ItemStack;
import ro.niconeko.astralbooks.utils.PersistentKey;

public class EmptyItemData implements ItemData {
    private final ItemStack item;

    public EmptyItemData(ItemStack item) {
        this.item = item;
    }

    @Override
    public boolean hasStringKey(PersistentKey key) {
        throw new UnsupportedOperationException("PersistentDataContainer or NBTAPI is not enabled! This is not an issue with CitizensBooks!");
    }

    @Override
    public void putString(PersistentKey key, String value) {
        throw new UnsupportedOperationException("PersistentDataContainer or NBTAPI is not enabled! This is not an issue with CitizensBooks!");
    }

    @Override
    public String getString(PersistentKey key) {
        throw new UnsupportedOperationException("PersistentDataContainer or NBTAPI is not enabled! This is not an issue with CitizensBooks!");
    }

    @Override
    public boolean hasIntKey(PersistentKey key) {
        throw new UnsupportedOperationException("PersistentDataContainer or NBTAPI is not enabled! This is not an issue with CitizensBooks!");
    }

    @Override
    public void putInt(PersistentKey key, int value) {
        throw new UnsupportedOperationException("PersistentDataContainer or NBTAPI is not enabled! This is not an issue with CitizensBooks!");
    }

    @Override
    public int getInt(PersistentKey key) {
        throw new UnsupportedOperationException("PersistentDataContainer or NBTAPI is not enabled! This is not an issue with CitizensBooks!");
    }

    @Override
    public void removeKey(PersistentKey key) {
        throw new UnsupportedOperationException("PersistentDataContainer or NBTAPI is not enabled! This is not an issue with CitizensBooks!");
    }

    @Override
    public ItemStack build() {
        return this.item;
    }

    @Override
    public ItemStack copyDataToStack(ItemStack stack) {
        return stack;
    }
}
