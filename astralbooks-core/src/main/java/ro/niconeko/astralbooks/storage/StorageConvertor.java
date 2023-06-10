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

package ro.niconeko.astralbooks.storage;

import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.utils.Side;

import java.sql.SQLException;
import java.util.Date;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class StorageConvertor {
    private final AstralBooksPlugin plugin;
    private final Storage currentStorage;
    private final Storage previousStorage;

    public StorageConvertor(AstralBooksPlugin plugin, Storage currentStorage, Storage previousStorage) {
        this.plugin = plugin;
        this.currentStorage = currentStorage;
        this.previousStorage = previousStorage;
    }

    public void convert() {
        if (this.currentStorage.getStorageType() == this.previousStorage.getStorageType()) {
            this.plugin.getLogger().log(Level.WARNING, "You can't convert to the current storage type!");
            return;
        }
        AtomicBoolean failed = new AtomicBoolean(false);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            if (failed.get()) {
                this.previousStorage.cache.unload();
                this.previousStorage.unload();
                scheduler.shutdown();
            }
        }, 1, TimeUnit.SECONDS);
        try {
            this.previousStorage.cache.load();
            this.previousStorage.load();
        } catch (SQLException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to load previous storage type! Please check configuration...", ex);
            failed.set(true);
            return;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {

            Queue<Triplet<Integer, Side, ItemStack>> allNPCBookStacks = this.previousStorage.getAllNPCBookStacks(failed);
            this.plugin.getLogger().info("Converting [" + allNPCBookStacks.size() + "] npc books...");
            this.currentStorage.setAllNPCBookStacks(allNPCBookStacks, failed);

            Queue<Pair<String, ItemStack>> allFilterBookStacks = this.previousStorage.getAllFilterBookStacks(failed);
            this.plugin.getLogger().info("Converting [" + allFilterBookStacks.size() + "] filter books...");
            this.currentStorage.setAllFilterBookStacks(allFilterBookStacks, failed);

            Queue<Triplet<String, String, String>> allCommandFilterStacks = this.previousStorage.getAllCommandFilterStacks(failed);
            this.plugin.getLogger().info("Converting [" + allCommandFilterStacks.size() + "] command filters...");
            this.currentStorage.setAllCommandFilterStacks(allCommandFilterStacks, failed);

            Queue<Triplet<UUID, Date, ItemStack>> allBookSecurityStacks = this.previousStorage.getAllBookSecurityStacks(failed);
            this.plugin.getLogger().info("Converting [" + allBookSecurityStacks.size() + "] security books...");
            this.currentStorage.setAllBookSecurityStacks(allBookSecurityStacks, failed);

            failed.set(true);
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS))
                    this.plugin.getLogger().severe("A previous task timed out to shut down correctly!");
            } catch (InterruptedException e) {
                this.plugin.getLogger().severe("A previous task failed to shut down correctly!");
            }
            this.plugin.getLogger().info("Conversion done! Reloading plugin...");

            Bukkit.getScheduler().runTask(this.plugin, () -> {
                this.plugin.reloadPlugin();
                this.plugin.getLogger().info("Plugin reloaded!");
            });

        });
        executor.shutdown();
    }
}
