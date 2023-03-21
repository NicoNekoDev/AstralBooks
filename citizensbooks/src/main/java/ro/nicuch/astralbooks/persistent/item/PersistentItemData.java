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

package ro.nicuch.astralbooks.persistent.item;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ro.nicuch.astralbooks.persistent.NBTDataType;
import ro.nicuch.astralbooks.utils.PersistentKey;

public class PersistentItemData implements ItemData {
    private final ItemStack itemStack;
    private final ItemMeta meta;

    public PersistentItemData(ItemStack item) {
        this.itemStack = item;
        this.meta = item.getItemMeta();
    }

    @Override
    public boolean hasStringKey(PersistentKey key) {
        return this.meta.getPersistentDataContainer().has(key.getKey(), PersistentDataType.STRING);
    }

    @Override
    public void putString(PersistentKey key, String value) {
        this.meta.getPersistentDataContainer().set(key.getKey(), PersistentDataType.STRING, value);
    }

    @Override
    public String getString(PersistentKey key) {
        return this.meta.getPersistentDataContainer().get(key.getKey(), PersistentDataType.STRING);
    }

    @Override
    public boolean hasIntKey(PersistentKey key) {
        return this.meta.getPersistentDataContainer().has(key.getKey(), PersistentDataType.INTEGER);
    }

    @Override
    public void putInt(PersistentKey key, int value) {
        this.meta.getPersistentDataContainer().set(key.getKey(), PersistentDataType.INTEGER, value);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int getInt(PersistentKey key) {
        return this.meta.getPersistentDataContainer().get(key.getKey(), PersistentDataType.INTEGER);
    }

    @Override
    public void removeKey(PersistentKey key) {
        this.meta.getPersistentDataContainer().remove(key.getKey());
    }

    @Override
    public ItemStack build() {
        this.itemStack.setItemMeta(this.meta);
        return this.itemStack;
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    @Override
    public ItemStack copyDataToStack(ItemStack stack) {
        if (this.meta == null)
            return stack;
        ItemMeta stackMeta = stack.getItemMeta();
        if (stackMeta == null)
            return stack;
        PersistentDataContainer stackContainer = stackMeta.getPersistentDataContainer();
        PersistentDataContainer container = this.meta.getPersistentDataContainer();
        try {
            for (NamespacedKey key : container.getKeys()) {
                for (NBTDataType type : NBTDataType.values()) {
                    if (container.has(key, type.getType())) {
                        stackContainer.set(key, type.getType(), container.get(key, type.getType()));
                        break;
                    }
                }
            }
        } catch (Exception ignore) {
            // if something failed, it's all because of spigot and java generify >.<
        }
        stack.setItemMeta(stackMeta);
        return stack;
    }
}
