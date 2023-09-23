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

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import ro.niconeko.astralbooks.utils.PersistentKey;

import java.util.ArrayList;
import java.util.List;

public class BlocksToBooksPairsDataType implements PersistentDataType<PersistentDataContainer[], BlocksToBooksPairs> {
    @NotNull
    @Override
    public Class<PersistentDataContainer[]> getPrimitiveType() {
        return PersistentDataContainer[].class;
    }

    @NotNull
    @Override
    public Class<BlocksToBooksPairs> getComplexType() {
        return BlocksToBooksPairs.class;
    }

    @NotNull
    @Override
    public PersistentDataContainer @NotNull [] toPrimitive(@NotNull BlocksToBooksPairs complex, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        List<PersistentDataContainer> list = new ArrayList<>();
        for (BlockToBookPair complexPart : complex.getList()) {
            PersistentDataContainer newContainer = persistentDataAdapterContext.newPersistentDataContainer();
            newContainer.set(PersistentKey.BLOCK_LOCATION_X, PersistentDataType.INTEGER, complexPart.x());
            newContainer.set(PersistentKey.BLOCK_LOCATION_Y, PersistentDataType.INTEGER, complexPart.y());
            newContainer.set(PersistentKey.BLOCK_LOCATION_Z, PersistentDataType.INTEGER, complexPart.z());
            newContainer.set(PersistentKey.BLOCK_LEFT_BOOK, PersistentDataType.STRING, complexPart.leftBook() != null ? complexPart.leftBook() : "");
            newContainer.set(PersistentKey.BLOCK_RIGHT_BOOK, PersistentDataType.STRING, complexPart.rightBook() != null ? complexPart.rightBook() : "");
            list.add(newContainer);
        }
        return list.toArray(new PersistentDataContainer[0]);
    }

    @SuppressWarnings("DataFlowIssue")
    @NotNull
    @Override
    public BlocksToBooksPairs fromPrimitive(@NotNull PersistentDataContainer @NotNull [] primitive, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        BlocksToBooksPairs blocksToBooksPairs = new BlocksToBooksPairs();
        for (PersistentDataContainer primitivePart : primitive) {
            int blockX, blockY, blockZ;
            String leftBook = null;
            String rightBook = null;
            if (!primitivePart.has(PersistentKey.BLOCK_LOCATION_X, PersistentDataType.INTEGER))
                throw new IllegalStateException();
            if (!primitivePart.has(PersistentKey.BLOCK_LOCATION_Y, PersistentDataType.INTEGER))
                throw new IllegalStateException();
            if (!primitivePart.has(PersistentKey.BLOCK_LOCATION_Z, PersistentDataType.INTEGER))
                throw new IllegalStateException();
            blockX = primitivePart.get(PersistentKey.BLOCK_LOCATION_X, PersistentDataType.INTEGER);
            blockY = primitivePart.get(PersistentKey.BLOCK_LOCATION_Y, PersistentDataType.INTEGER);
            blockZ = primitivePart.get(PersistentKey.BLOCK_LOCATION_Z, PersistentDataType.INTEGER);
            if (primitivePart.has(PersistentKey.BLOCK_LEFT_BOOK, PersistentDataType.STRING))
                leftBook = primitivePart.get(PersistentKey.BLOCK_LEFT_BOOK, PersistentDataType.STRING);
            if (primitivePart.has(PersistentKey.BLOCK_RIGHT_BOOK, PersistentDataType.STRING))
                rightBook = primitivePart.get(PersistentKey.BLOCK_RIGHT_BOOK, PersistentDataType.STRING);
            blocksToBooksPairs.add(new BlockToBookPair(blockX, blockY, blockZ, leftBook, rightBook));
        }
        return blocksToBooksPairs;
    }
}
