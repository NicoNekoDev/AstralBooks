package ro.nicuch.citizensbooks.listeners;

import com.google.gson.JsonObject;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ro.nicuch.citizensbooks.CitizensBooksAPI;
import ro.nicuch.citizensbooks.CitizensBooksPlugin;
import ro.nicuch.citizensbooks.dist.Distribution;
import ro.nicuch.citizensbooks.persistent.BlockToBookPair;
import ro.nicuch.citizensbooks.persistent.BlocksToBooksPairs;
import ro.nicuch.citizensbooks.persistent.BlocksToBooksPairsDataType;
import ro.nicuch.citizensbooks.utils.PersistentKey;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@SuppressWarnings("DataFlowIssue")
public class ServerActions implements Listener {
    private final CitizensBooksPlugin plugin;
    private final CitizensBooksAPI api;
    private final Distribution distribution;

    public ServerActions(CitizensBooksPlugin plugin) {
        this.plugin = plugin;
        this.api = plugin.getAPI();
        this.distribution = this.api.getDistribution();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        try {
            if (!this.distribution.isPersistentDataContainerEnabled())
                return;
            Chunk chunk = event.getChunk();
            Map<Block, Pair<ItemStack, ItemStack>> chunkBlocks = new HashMap<>();
            PersistentDataContainer chunkPersistentDataContainer = this.distribution.getChunkDataContainer(chunk);
            if (chunkPersistentDataContainer.has(PersistentKey.CHUNK_TAG.getKey(), PersistentDataType.TAG_CONTAINER_ARRAY)) {
                BlocksToBooksPairs blocksDataContainers = chunkPersistentDataContainer.get(PersistentKey.CHUNK_TAG.getKey(), new BlocksToBooksPairsDataType());
                for (BlockToBookPair blockDataContainer : blocksDataContainers.getList()) {
                    ItemStack leftBook = this.distribution.convertJsonToBook(
                            this.distribution.getGson().fromJson(blockDataContainer.leftBook(), JsonObject.class));
                    ItemStack rightBook = this.distribution.convertJsonToBook(
                            this.distribution.getGson().fromJson(blockDataContainer.rightBook(), JsonObject.class));
                    Block block = chunk.getBlock(blockDataContainer.x(), blockDataContainer.y(), blockDataContainer.z());
                    chunkBlocks.put(block, Pair.of(leftBook, rightBook));
                }
            }
            //
            this.api.deployBooksForChunk(chunk, chunkBlocks);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Chunk load failed!", ex);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (!this.distribution.isPersistentDataContainerEnabled())
            return;
        try {
            Chunk chunk = event.getChunk();
            PersistentDataContainer chunkPersistentDataContainer = this.distribution.getChunkDataContainer(chunk);
            chunkPersistentDataContainer.remove(PersistentKey.CHUNK_TAG.getKey());
            BlocksToBooksPairs blocksToBooksPairs = new BlocksToBooksPairs();
            for (Map.Entry<Block, Pair<ItemStack, ItemStack>> blockPairEntry : this.api.getBlocksEntriesPairedToChunk(chunk).entrySet()) {
                Block block = blockPairEntry.getKey();
                Pair<ItemStack, ItemStack> pair = blockPairEntry.getValue();
                BlockToBookPair blockToBookPair = new BlockToBookPair(block.getX(), block.getY(), block.getZ(),
                        this.distribution.convertBookToJson(pair.getFirstValue()).toString(),
                        this.distribution.convertBookToJson(pair.getSecondValue()).toString());
                blocksToBooksPairs.add(blockToBookPair);
            }
            if (!blocksToBooksPairs.isEmpty())
                chunkPersistentDataContainer.set(PersistentKey.CHUNK_TAG.getKey(), new BlocksToBooksPairsDataType(), blocksToBooksPairs);
            this.api.concentrateBooksForChunk(chunk);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Chunk unload failed!", ex);
        }
    }
}
