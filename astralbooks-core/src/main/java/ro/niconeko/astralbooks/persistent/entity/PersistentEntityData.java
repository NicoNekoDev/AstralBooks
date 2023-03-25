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

package ro.niconeko.astralbooks.persistent.entity;

import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import ro.niconeko.astralbooks.utils.PersistentKey;

public class PersistentEntityData implements EntityData {
    private final Entity entity;

    public PersistentEntityData(Entity entity) {
        this.entity = entity;
    }

    @Override
    public boolean hasStringKey(PersistentKey key) {
        return this.entity.getPersistentDataContainer().has(key.getKey(), PersistentDataType.STRING);
    }

    @Override
    public void putString(PersistentKey key, String value) {
        this.entity.getPersistentDataContainer().set(key.getKey(), PersistentDataType.STRING, value);
    }

    @Override
    public String getString(PersistentKey key) {
        return this.entity.getPersistentDataContainer().get(key.getKey(), PersistentDataType.STRING);
    }

    @Override
    public boolean hasIntKey(PersistentKey key) {
        return this.entity.getPersistentDataContainer().has(key.getKey(), PersistentDataType.INTEGER);
    }

    @Override
    public void putInt(PersistentKey key, int value) {
        this.entity.getPersistentDataContainer().set(key.getKey(), PersistentDataType.INTEGER, value);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int getInt(PersistentKey key) {
        return this.entity.getPersistentDataContainer().get(key.getKey(), PersistentDataType.INTEGER);
    }

    @Override
    public void removeKey(PersistentKey key) {
        this.entity.getPersistentDataContainer().remove(key.getKey());
    }
}
