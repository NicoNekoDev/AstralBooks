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
import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ro.niconeko.astralbooks.AstralBooksCore;
import ro.niconeko.astralbooks.dist.Distribution;
import ro.niconeko.astralbooks.utils.PersistentKey;
import ro.niconeko.astralbooks.utils.tuples.PairTuple;

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
    public Map<Block, PairTuple<ItemStack, ItemStack>> loadChunk(Distribution distribution) throws IllegalAccessException {
        Map<Block, PairTuple<ItemStack, ItemStack>> chunkBlocks = new HashMap<>();
        if (nbtChunk.getPersistentDataContainer().hasTag(PersistentKey.CHUNK_TAG.getValue())) {
            NBTCompoundList blocksDataContainers = nbtChunk.getPersistentDataContainer().getCompoundList(PersistentKey.CHUNK_TAG.getValue());
            for (ReadWriteNBT blockDataContainer : blocksDataContainers) {
                if (!blockDataContainer.hasTag(PersistentKey.BLOCK_LOCATION_X.getValue())
                        || !blockDataContainer.hasTag(PersistentKey.BLOCK_LOCATION_Y.getValue())
                        || !blockDataContainer.hasTag(PersistentKey.BLOCK_LOCATION_Z.getValue()))
                    continue;
                String leftBookJson = blockDataContainer.getString(PersistentKey.BLOCK_LEFT_BOOK.getValue());
                ItemStack leftBook = (leftBookJson == null || leftBookJson.isEmpty()) ?
                        null :
                        distribution.convertJsonToBook(AstralBooksCore.PRETTY_GSON.fromJson(leftBookJson, JsonObject.class));
                String rightBookJson = blockDataContainer.getString((PersistentKey.BLOCK_RIGHT_BOOK.getValue()));
                ItemStack rightBook = (rightBookJson == null || rightBookJson.isEmpty()) ?
                        null :
                        distribution.convertJsonToBook(AstralBooksCore.PRETTY_GSON.fromJson(rightBookJson, JsonObject.class));
                Block block = this.chunk.getWorld().getBlockAt(
                        blockDataContainer.getInteger(PersistentKey.BLOCK_LOCATION_X.getValue()),
                        blockDataContainer.getInteger(PersistentKey.BLOCK_LOCATION_Y.getValue()),
                        blockDataContainer.getInteger(PersistentKey.BLOCK_LOCATION_Z.getValue()));
                chunkBlocks.put(block, new PairTuple<>(leftBook, rightBook));
            }
        }
        return chunkBlocks;
    }

    @Override
    public void unloadChunk(Distribution distribution, AstralBooksCore core) throws IllegalAccessException {
        this.nbtChunk.getPersistentDataContainer().removeKey(PersistentKey.CHUNK_TAG.getValue());
        NBTCompoundList blocksDataContainers = this.nbtChunk.getPersistentDataContainer().getCompoundList(PersistentKey.CHUNK_TAG.getValue());
        for (Map.Entry<Block, PairTuple<ItemStack, ItemStack>> blockPairEntry : core.getBlocksEntriesPairedToChunk(chunk).entrySet()) {
            Block block = blockPairEntry.getKey();
            PairTuple<ItemStack, ItemStack> pair = blockPairEntry.getValue();
            ItemStack left = pair.firstValue();
            ItemStack right = pair.secondValue();
            String leftJson = left != null ? distribution.convertBookToJson(left).toString() : null;
            String rightJson = right != null ? distribution.convertBookToJson(right).toString() : null;
            NBTContainer blockDataContainer = getNbtContainer(block, leftJson, rightJson);
            blocksDataContainers.addCompound(blockDataContainer);
        }
        core.concentrateBooksForChunk(chunk);
    }

    @NotNull
    private static NBTContainer getNbtContainer(Block block, String leftJson, String rightJson) {
        NBTContainer blockDataContainer = new NBTContainer();
        blockDataContainer.setInteger(PersistentKey.BLOCK_LOCATION_X.getValue(), block.getX());
        blockDataContainer.setInteger(PersistentKey.BLOCK_LOCATION_Y.getValue(), block.getY());
        blockDataContainer.setInteger(PersistentKey.BLOCK_LOCATION_Z.getValue(), block.getZ());
        if (leftJson != null)
            blockDataContainer.setString(PersistentKey.BLOCK_LEFT_BOOK.getValue(), leftJson);
        if (rightJson != null)
            blockDataContainer.setString(PersistentKey.BLOCK_RIGHT_BOOK.getValue(), rightJson);
        return blockDataContainer;
    }
}
