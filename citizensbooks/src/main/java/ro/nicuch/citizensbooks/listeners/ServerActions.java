package ro.nicuch.citizensbooks.listeners;

import com.google.gson.JsonObject;
import io.github.NicoNekoDev.SimpleTuples.Pair;
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

    private void chunkLoad(Chunk chunk) {
        try {
            if (!this.distribution.isPersistentDataContainerEnabled())
                return;
            Map<Block, Pair<ItemStack, ItemStack>> chunkBlocks = new HashMap<>();
            PersistentDataContainer chunkPersistentDataContainer = this.distribution.getChunkDataContainer(chunk);
            if (chunkPersistentDataContainer.has(PersistentKey.CHUNK_TAG.getKey(), PersistentDataType.TAG_CONTAINER_ARRAY)) {
                BlocksToBooksPairs blocksDataContainers = chunkPersistentDataContainer.get(PersistentKey.CHUNK_TAG.getKey(), new BlocksToBooksPairsDataType());
                for (BlockToBookPair blockDataContainer : blocksDataContainers.getList()) {
                    ItemStack leftBook = blockDataContainer.leftBook() != null ? this.distribution.convertJsonToBook(
                            this.distribution.getGson().fromJson(blockDataContainer.leftBook(), JsonObject.class)) : null;
                    ItemStack rightBook = blockDataContainer.rightBook() != null ? this.distribution.convertJsonToBook(
                            this.distribution.getGson().fromJson(blockDataContainer.rightBook(), JsonObject.class)) : null;
                    Block block = chunk.getWorld().getBlockAt(blockDataContainer.x(), blockDataContainer.y(), blockDataContainer.z());
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
        this.chunkUnload(event.getChunk());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        for (Chunk chunk : event.getWorld().getLoadedChunks())
            this.chunkUnload(chunk);
    }

    private void chunkUnload(Chunk chunk) {
        if (!this.distribution.isPersistentDataContainerEnabled())
            return;
        try {
            PersistentDataContainer chunkPersistentDataContainer = this.distribution.getChunkDataContainer(chunk);
            chunkPersistentDataContainer.remove(PersistentKey.CHUNK_TAG.getKey());
            BlocksToBooksPairs blocksToBooksPairs = new BlocksToBooksPairs();
            for (Map.Entry<Block, Pair<ItemStack, ItemStack>> blockPairEntry : this.api.getBlocksEntriesPairedToChunk(chunk).entrySet()) {
                Block block = blockPairEntry.getKey();
                Pair<ItemStack, ItemStack> pair = blockPairEntry.getValue();
                ItemStack left = pair.getFirstValue();
                ItemStack right = pair.getSecondValue();
                BlockToBookPair blockToBookPair = new BlockToBookPair(block.getX(), block.getY(), block.getZ(),
                        left != null ? this.distribution.convertBookToJson(left).toString() : null,
                        right != null ? this.distribution.convertBookToJson(right).toString() : null);
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
