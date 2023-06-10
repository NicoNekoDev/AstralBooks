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

package ro.niconeko.astralbooks.persistent.chunk;

import com.google.gson.JsonObject;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ro.niconeko.astralbooks.AstralBooksCore;
import ro.niconeko.astralbooks.dist.Distribution;
import ro.niconeko.astralbooks.persistent.BlockToBookPair;
import ro.niconeko.astralbooks.persistent.BlocksToBooksPairs;
import ro.niconeko.astralbooks.persistent.BlocksToBooksPairsDataType;
import ro.niconeko.astralbooks.utils.PersistentKey;

import java.util.HashMap;
import java.util.Map;

public class PersistentChunkData implements ChunkData {
    private final Chunk chunk;

    public PersistentChunkData(Chunk chunk) {
        this.chunk = chunk;
    }


    @SuppressWarnings("DataFlowIssue")
    @Override
    public Map<Block, Pair<ItemStack, ItemStack>> loadChunk(Distribution distribution) throws IllegalAccessException {
        Map<Block, Pair<ItemStack, ItemStack>> chunkBlocks = new HashMap<>();
        PersistentDataContainer chunkPersistentDataContainer = distribution.getChunkDataContainer(chunk);
        if (chunkPersistentDataContainer.has(PersistentKey.CHUNK_TAG.getKey(), PersistentDataType.TAG_CONTAINER_ARRAY)) {
            BlocksToBooksPairs blocksDataContainers = chunkPersistentDataContainer.get(PersistentKey.CHUNK_TAG.getKey(), new BlocksToBooksPairsDataType());
            for (BlockToBookPair blockDataContainer : blocksDataContainers.getList()) {
                String leftBookJson = blockDataContainer.leftBook();
                ItemStack leftBook = (leftBookJson == null || leftBookJson.isEmpty()) ?
                        null :
                        distribution.convertJsonToBook(AstralBooksCore.GSON.fromJson(leftBookJson, JsonObject.class));
                String rightBookJson = blockDataContainer.rightBook();
                ItemStack rightBook = (rightBookJson == null || rightBookJson.isEmpty()) ?
                        null :
                        distribution.convertJsonToBook(AstralBooksCore.GSON.fromJson(rightBookJson, JsonObject.class));
                Block block = chunk.getWorld().getBlockAt(blockDataContainer.x(), blockDataContainer.y(), blockDataContainer.z());
                chunkBlocks.put(block, Pair.of(leftBook, rightBook));
            }
        }
        return chunkBlocks;
    }

    @Override
    public void unloadChunk(Distribution distribution, AstralBooksCore core) throws IllegalAccessException {
        PersistentDataContainer chunkPersistentDataContainer = distribution.getChunkDataContainer(chunk);
        chunkPersistentDataContainer.remove(PersistentKey.CHUNK_TAG.getKey());
        BlocksToBooksPairs blocksToBooksPairs = new BlocksToBooksPairs();
        for (Map.Entry<Block, Pair<ItemStack, ItemStack>> blockPairEntry : core.getBlocksEntriesPairedToChunk(chunk).entrySet()) {
            Block block = blockPairEntry.getKey();
            Pair<ItemStack, ItemStack> pair = blockPairEntry.getValue();
            ItemStack left = pair.getFirstValue();
            ItemStack right = pair.getSecondValue();
            BlockToBookPair blockToBookPair = new BlockToBookPair(block.getX(), block.getY(), block.getZ(),
                    left != null ? distribution.convertBookToJson(left).toString() : null,
                    right != null ? distribution.convertBookToJson(right).toString() : null);
            blocksToBooksPairs.add(blockToBookPair);
        }
        if (!blocksToBooksPairs.isEmpty())
            chunkPersistentDataContainer.set(PersistentKey.CHUNK_TAG.getKey(), new BlocksToBooksPairsDataType(), blocksToBooksPairs);
        core.concentrateBooksForChunk(chunk);
    }
}
