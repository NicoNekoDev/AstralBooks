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

package ro.niconeko.astralbooks.persistent;

import lombok.Getter;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("rawtypes")
@Getter
public enum NBTDataType {
    BYTE(PersistentDataType.BYTE),
    BYTE_ARRAY(PersistentDataType.BYTE_ARRAY),
    INTEGER(PersistentDataType.INTEGER),
    INTEGER_ARRAY(PersistentDataType.INTEGER_ARRAY),
    LONG(PersistentDataType.LONG),
    LONG_ARRAY(PersistentDataType.LONG_ARRAY),
    DOUBLE(PersistentDataType.DOUBLE),
    FLOAT(PersistentDataType.FLOAT),
    SHORT(PersistentDataType.SHORT),
    TAG_CONTAINER(PersistentDataType.TAG_CONTAINER),
    TAG_CONTAINER_ARRAY(PersistentDataType.TAG_CONTAINER_ARRAY),
    STRING(PersistentDataType.STRING);

    final PersistentDataType type;

    NBTDataType(PersistentDataType type) {
        this.type = type;
    }

}
