package ro.nicuch.citizensbooks.persistent;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import ro.nicuch.citizensbooks.utils.PersistentKey;

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
            newContainer.set(PersistentKey.BLOCK_LOCATION_X.getKey(), PersistentDataType.INTEGER, complexPart.x());
            newContainer.set(PersistentKey.BLOCK_LOCATION_Y.getKey(), PersistentDataType.INTEGER, complexPart.y());
            newContainer.set(PersistentKey.BLOCK_LOCATION_Z.getKey(), PersistentDataType.INTEGER, complexPart.z());
            newContainer.set(PersistentKey.BLOCK_LEFT_BOOK.getKey(), PersistentDataType.STRING, complexPart.leftBook() != null ? complexPart.leftBook() : "");
            newContainer.set(PersistentKey.BLOCK_RIGHT_BOOK.getKey(), PersistentDataType.STRING, complexPart.rightBook() != null ? complexPart.rightBook() : "");
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
            int blockX = 0, blockY = 0, blockZ = 0;
            String leftBook = null;
            String rightBook = null;
            if (!primitivePart.has(PersistentKey.BLOCK_LOCATION_X.getKey(), PersistentDataType.INTEGER))
                throw new IllegalStateException();
            if (!primitivePart.has(PersistentKey.BLOCK_LOCATION_Y.getKey(), PersistentDataType.INTEGER))
                throw new IllegalStateException();
            if (!primitivePart.has(PersistentKey.BLOCK_LOCATION_Z.getKey(), PersistentDataType.INTEGER))
                throw new IllegalStateException();
            blockX = primitivePart.get(PersistentKey.BLOCK_LOCATION_X.getKey(), PersistentDataType.INTEGER);
            blockY = primitivePart.get(PersistentKey.BLOCK_LOCATION_Y.getKey(), PersistentDataType.INTEGER);
            blockZ = primitivePart.get(PersistentKey.BLOCK_LOCATION_Z.getKey(), PersistentDataType.INTEGER);
            if (primitivePart.has(PersistentKey.BLOCK_LEFT_BOOK.getKey(), PersistentDataType.STRING))
                leftBook = primitivePart.get(PersistentKey.ENTITY_LEFT_BOOK.getKey(), PersistentDataType.STRING);
            if (primitivePart.has(PersistentKey.BLOCK_RIGHT_BOOK.getKey(), PersistentDataType.STRING))
                rightBook = primitivePart.get(PersistentKey.BLOCK_RIGHT_BOOK.getKey(), PersistentDataType.STRING);
            blocksToBooksPairs.add(new BlockToBookPair(blockX, blockY, blockZ, leftBook, rightBook));
        }
        return blocksToBooksPairs;
    }
}
