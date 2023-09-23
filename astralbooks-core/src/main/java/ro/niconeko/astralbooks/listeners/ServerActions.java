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

package ro.niconeko.astralbooks.listeners;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ro.niconeko.astralbooks.AstralBooksCore;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.dist.Distribution;
import ro.niconeko.astralbooks.persistent.BlockToBookPair;
import ro.niconeko.astralbooks.persistent.BlocksToBooksPairs;
import ro.niconeko.astralbooks.persistent.BlocksToBooksPairsDataType;
import ro.niconeko.astralbooks.utils.PersistentKey;
import ro.niconeko.astralbooks.utils.tuples.PairTuple;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ServerActions implements Listener {
    private final AstralBooksPlugin plugin;
    private final AstralBooksCore api;
    private final Distribution distribution;

    public ServerActions(AstralBooksPlugin plugin) {
        this.plugin = plugin;
        this.api = plugin.getAPI();
        this.distribution = this.api.getDistribution();
        for (World world : Bukkit.getWorlds())
            for (Chunk chunk : world.getLoadedChunks())
                this.chunkLoad(chunk);
    }

    public void onDisable() {
        for (World world : Bukkit.getWorlds())
            for (Chunk chunk : world.getLoadedChunks())
                this.chunkUnload(chunk);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        this.chunkLoad(event.getChunk());
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        for (Chunk chunk : event.getWorld().getLoadedChunks())
            this.chunkLoad(chunk);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        this.chunkUnload(event.getChunk());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        for (Chunk chunk : event.getWorld().getLoadedChunks())
            this.chunkUnload(chunk);
    }

    @SuppressWarnings("DataFlowIssue")
    private void chunkLoad(Chunk chunk) {
        try {
            Map<Block, PairTuple<ItemStack, ItemStack>> chunkBlocks = new HashMap<>();
            PersistentDataContainer chunkPersistentDataContainer = chunk.getPersistentDataContainer();
            if (chunkPersistentDataContainer.has(PersistentKey.CHUNK_TAG, PersistentDataType.TAG_CONTAINER_ARRAY)) {
                BlocksToBooksPairs blocksDataContainers = chunkPersistentDataContainer.get(PersistentKey.CHUNK_TAG, new BlocksToBooksPairsDataType());
                for (BlockToBookPair blockDataContainer : blocksDataContainers.getList()) {
                    String leftBookJson = blockDataContainer.leftBook();
                    ItemStack leftBook = (leftBookJson == null || leftBookJson.isEmpty()) ?
                            null :
                            distribution.convertJsonToBook(AstralBooksCore.PRETTY_GSON.fromJson(leftBookJson, JsonObject.class));
                    String rightBookJson = blockDataContainer.rightBook();
                    ItemStack rightBook = (rightBookJson == null || rightBookJson.isEmpty()) ?
                            null :
                            distribution.convertJsonToBook(AstralBooksCore.PRETTY_GSON.fromJson(rightBookJson, JsonObject.class));
                    Block block = chunk.getWorld().getBlockAt(blockDataContainer.x(), blockDataContainer.y(), blockDataContainer.z());
                    chunkBlocks.put(block, new PairTuple<>(leftBook, rightBook));
                }
            }
            this.api.deployBooksForChunk(chunk, chunkBlocks);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Chunk load failed!", ex);
        }
    }

    private void chunkUnload(Chunk chunk) {
        try {
            PersistentDataContainer chunkPersistentDataContainer = chunk.getPersistentDataContainer();
            chunkPersistentDataContainer.remove(PersistentKey.CHUNK_TAG);
            BlocksToBooksPairs blocksToBooksPairs = new BlocksToBooksPairs();
            for (Map.Entry<Block, PairTuple<ItemStack, ItemStack>> blockPairEntry : this.api.getBlocksEntriesPairedToChunk(chunk).entrySet()) {
                Block block = blockPairEntry.getKey();
                PairTuple<ItemStack, ItemStack> pair = blockPairEntry.getValue();
                ItemStack left = pair.firstValue();
                ItemStack right = pair.secondValue();
                BlockToBookPair blockToBookPair = new BlockToBookPair(block.getX(), block.getY(), block.getZ(),
                        left != null ? distribution.convertBookToJson(left).toString() : null,
                        right != null ? distribution.convertBookToJson(right).toString() : null);
                blocksToBooksPairs.add(blockToBookPair);
            }
            if (!blocksToBooksPairs.isEmpty())
                chunkPersistentDataContainer.set(PersistentKey.CHUNK_TAG, new BlocksToBooksPairsDataType(), blocksToBooksPairs);
            this.api.concentrateBooksForChunk(chunk);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Chunk unload failed!", ex);
        }
    }
}
