package ro.niconeko.astralbooks.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import ro.niconeko.astralbooks.AstralBooksCore;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.dist.Distribution;

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

    private void chunkLoad(Chunk chunk) {
        if (!(this.plugin.isNBTAPIEnabled() || this.api.noNBTAPIRequired()))
            return;
        try {
            this.api.deployBooksForChunk(chunk, this.api.chunkDataFactory(chunk).loadChunk(this.distribution));
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Chunk load failed!", ex);
        }
    }

    private void chunkUnload(Chunk chunk) {
        if (!(this.plugin.isNBTAPIEnabled() || this.api.noNBTAPIRequired()))
            return;
        try {
            this.api.chunkDataFactory(chunk).unloadChunk(this.distribution, this.api);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Chunk unload failed!", ex);
        }
    }
}
