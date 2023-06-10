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

package ro.niconeko.astralbooks.persistent.entity;

import de.tr7zw.nbtapi.NBTEntity;
import org.bukkit.entity.Entity;
import ro.niconeko.astralbooks.utils.PersistentKey;

public class NBTAPIEntityData implements EntityData {
    private final NBTEntity nbtEntity;

    public NBTAPIEntityData(Entity entity) {
        this.nbtEntity = new NBTEntity(entity);
    }

    @Override
    public boolean hasStringKey(PersistentKey key) {
        return this.nbtEntity.getPersistentDataContainer().hasKey(key.getValue());
    }

    @Override
    public void putString(PersistentKey key, String value) {
        this.nbtEntity.setString(key.getValue(), value);
    }

    @Override
    public String getString(PersistentKey key) {
        return this.nbtEntity.getString(key.getValue());
    }

    @Override
    public boolean hasIntKey(PersistentKey key) {
        return this.nbtEntity.hasKey(key.getValue());
    }

    @Override
    public void putInt(PersistentKey key, int value) {
        this.nbtEntity.setInteger(key.getValue(), value);
    }

    @Override
    public int getInt(PersistentKey key) {
        return this.nbtEntity.getInteger(key.getValue());
    }

    @Override
    public void removeKey(PersistentKey key) {
        this.nbtEntity.removeKey(key.getValue());
    }
}
