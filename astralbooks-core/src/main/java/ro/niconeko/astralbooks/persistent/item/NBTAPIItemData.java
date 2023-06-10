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

package ro.niconeko.astralbooks.persistent.item;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;
import ro.niconeko.astralbooks.utils.PersistentKey;

public class NBTAPIItemData implements ItemData {
    private final ItemStack itemStack;
    private final NBTItem item;

    public NBTAPIItemData(ItemStack item) {
        this.itemStack = item;
        this.item = new NBTItem(item);
    }

    @Override
    public boolean hasStringKey(PersistentKey key) {
        return this.item.hasKey(key.getValue());
    }

    @Override
    public void putString(PersistentKey key, String value) {
        this.item.setString(key.getValue(), value);
    }

    @Override
    public String getString(PersistentKey key) {
        return this.item.getString(key.getValue());
    }

    @Override
    public boolean hasIntKey(PersistentKey key) {
        return this.item.hasKey(key.getValue());
    }

    @Override
    public void putInt(PersistentKey key, int value) {
        this.item.setInteger(key.getValue(), value);
    }

    @Override
    public int getInt(PersistentKey key) {
        return this.item.getInteger(key.getValue());
    }

    @Override
    public void removeKey(PersistentKey key) {
        this.item.removeKey(key.getValue());
    }

    @Override
    public ItemStack build() {
        this.item.applyNBT(this.itemStack);
        return this.itemStack;
    }

    @Override
    public ItemStack copyDataToStack(ItemStack stack) {
        this.item.applyNBT(stack);
        return stack;
    }
}
