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
import de.tr7zw.nbtapi.NBTChunk;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTContainer;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import ro.niconeko.astralbooks.AstralBooksCore;
import ro.niconeko.astralbooks.dist.Distribution;
import ro.niconeko.astralbooks.utils.PersistentKey;

import java.util.HashMap;
import java.util.Map;

public class NBTAPIChunkData implements ChunkData {
    private final Chunk chunk;
    private final NBTChunk nbtChunk;

    public NBTAPIChunkData(Chunk chunk) {
        this.chunk = chunk;
        this.nbtChunk = new NBTChunk(chunk);
    }

    @Override
    public Map<Block, Pair<ItemStack, ItemStack>> loadChunk(Distribution distribution) throws IllegalAccessException {
        Map<Block, Pair<ItemStack, ItemStack>> chunkBlocks = new HashMap<>();
        if (nbtChunk.getPersistentDataContainer().hasKey(PersistentKey.CHUNK_TAG.getValue())) {
            NBTCompoundList blocksDataContainers = nbtChunk.getPersistentDataContainer().getCompoundList(PersistentKey.CHUNK_TAG.getValue());
            for (NBTCompound blockDataContainer : blocksDataContainers) {
                if (!blockDataContainer.hasKey(PersistentKey.BLOCK_LOCATION_X.getValue())
                        || !blockDataContainer.hasKey(PersistentKey.BLOCK_LOCATION_Y.getValue())
                        || !blockDataContainer.hasKey(PersistentKey.BLOCK_LOCATION_Z.getValue()))
                    continue;
                String leftBookJson = blockDataContainer.getString(PersistentKey.BLOCK_LEFT_BOOK.getValue());
                ItemStack leftBook = (leftBookJson == null || leftBookJson.isEmpty()) ?
                        null :
                        distribution.convertJsonToBook(AstralBooksCore.GSON.fromJson(leftBookJson, JsonObject.class));
                String rightBookJson = blockDataContainer.getString((PersistentKey.BLOCK_RIGHT_BOOK.getValue()));
                ItemStack rightBook = (rightBookJson == null || rightBookJson.isEmpty()) ?
                        null :
                        distribution.convertJsonToBook(AstralBooksCore.GSON.fromJson(rightBookJson, JsonObject.class));
                Block block = this.chunk.getWorld().getBlockAt(
                        blockDataContainer.getInteger(PersistentKey.BLOCK_LOCATION_X.getValue()),
                        blockDataContainer.getInteger(PersistentKey.BLOCK_LOCATION_Y.getValue()),
                        blockDataContainer.getInteger(PersistentKey.BLOCK_LOCATION_Z.getValue()));
                chunkBlocks.put(block, Pair.of(leftBook, rightBook));
            }
        }
        return chunkBlocks;
    }

    @Override
    public void unloadChunk(Distribution distribution, AstralBooksCore core) throws IllegalAccessException {
        this.nbtChunk.getPersistentDataContainer().removeKey(PersistentKey.CHUNK_TAG.getValue());
        NBTCompoundList blocksDataContainers = this.nbtChunk.getPersistentDataContainer().getCompoundList(PersistentKey.CHUNK_TAG.getValue());
        for (Map.Entry<Block, Pair<ItemStack, ItemStack>> blockPairEntry : core.getBlocksEntriesPairedToChunk(chunk).entrySet()) {
            Block block = blockPairEntry.getKey();
            Pair<ItemStack, ItemStack> pair = blockPairEntry.getValue();
            ItemStack left = pair.getFirstValue();
            ItemStack right = pair.getSecondValue();
            String leftJson = left != null ? distribution.convertBookToJson(left).toString() : null;
            String rightJson = right != null ? distribution.convertBookToJson(right).toString() : null;
            NBTContainer blockDataContainer = new NBTContainer();
            blockDataContainer.setInteger(PersistentKey.BLOCK_LOCATION_X.getValue(), block.getX());
            blockDataContainer.setInteger(PersistentKey.BLOCK_LOCATION_Y.getValue(), block.getY());
            blockDataContainer.setInteger(PersistentKey.BLOCK_LOCATION_Z.getValue(), block.getZ());
            if (leftJson != null)
                blockDataContainer.setString(PersistentKey.BLOCK_LEFT_BOOK.getValue(), leftJson);
            if (rightJson != null)
                blockDataContainer.setString(PersistentKey.BLOCK_RIGHT_BOOK.getValue(), rightJson);
            blocksDataContainers.addCompound(blockDataContainer);
        }
        core.concentrateBooksForChunk(chunk);
    }
}
