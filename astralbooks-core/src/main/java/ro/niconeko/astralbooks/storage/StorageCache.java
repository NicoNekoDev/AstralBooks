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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.storage.settings.StorageSettings;
import ro.niconeko.astralbooks.utils.Side;
import ro.niconeko.astralbooks.utils.tuples.PairTuple;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class StorageCache {
    protected final AstralBooksPlugin plugin;
    protected final Storage storage;
    public ExecutorService poolExecutor;
    public LoadingCache<String, ItemStack> filterBooks;
    public LoadingCache<PairTuple<Integer, Side>, ItemStack> npcBooks;
    public LoadingCache<String, PairTuple<String, String>> commandFilters;
    public final Set<String> filters = new HashSet<>();
    public final Set<PairTuple<Integer, Side>> npcs = new HashSet<>();
    public final Set<String> commands = new HashSet<>();

    public LoadingCache<UUID, Set<Date>> playerTimestamps;

    protected StorageCache(AstralBooksPlugin plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public void load(StorageSettings settings) {
        this.poolExecutor = Executors.newFixedThreadPool(settings.THREADS.get());
        this.filterBooks = CacheBuilder.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull ItemStack load(@NotNull String key) throws Exception {
                        return storage.getFilterBookStack(key).get();
                    }
                });
        this.commandFilters = CacheBuilder.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull PairTuple<String, String> load(@NotNull String key) throws Exception {
                        return storage.getCommandFilterStack(key).get();
                    }
                });
        this.npcBooks = CacheBuilder.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull ItemStack load(@NotNull PairTuple<Integer, Side> key) throws Exception {
                        return storage.getNPCBookStack(key.firstValue(), key.secondValue()).get();
                    }
                });
        this.playerTimestamps = CacheBuilder.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull Set<Date> load(@NotNull UUID key) {
                        return new HashSet<>();
                    }
                });
    }

    public void unload() {
        try {
            this.filters.clear();
            if (this.filterBooks != null)
                this.filterBooks.invalidateAll();
            this.filterBooks = null;
            this.poolExecutor.shutdown();
            if (!this.poolExecutor.awaitTermination(30, TimeUnit.SECONDS))
                this.plugin.getLogger().warning("Failed to shut down storage cache thread-pools correctly!");
        } catch (InterruptedException ex) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to unload storage cache correctly!", ex);
        }
    }

    protected boolean putNPCBook(int npcId, Side side, ItemStack book) {
        try {
            this.storage.putNPCBookStack(npcId, side, book);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    protected boolean removeNPCBook(int npcId, Side side) {
        try {
            this.storage.removeNPCBookStack(npcId, side);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    protected ItemStack getNPCBook(int npcId, Side side, ItemStack def) {
        try {
            return this.npcBooks.get(new PairTuple<>(npcId, side));
        } catch (Exception e) {
            return def;
        }
    }

    protected boolean hasNPCBook(int npcId, Side side) {
        return this.npcs.contains(new PairTuple<>(npcId, side));
    }

    protected Set<PairTuple<Integer, Side>> getNPCBooks() {
        return this.npcs;
    }

    protected boolean putFilterBook(String filterName, ItemStack book) {
        try {
            this.storage.putFilterBookStack(filterName, book);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    protected boolean removeFilterBook(String filterName) {
        try {
            this.storage.removeFilterBookStack(filterName);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    protected ItemStack getFilterBook(String filterName, ItemStack def) {
        try {
            return this.filterBooks.get(filterName);
        } catch (Exception ignore) {
            return def;
        }
    }

    protected boolean hasFilterBook(String filterName) {
        return this.filters.contains(filterName);
    }

    protected Set<String> getFilterNames() {
        return this.filters;
    }

    protected boolean putCommandFilter(String cmd, String filterName, String permission) {
        try {
            this.storage.putCommandFilterStack(cmd, filterName, permission);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    protected boolean removeCommandFilter(String cmd) {
        try {
            this.storage.removeCommandFilterStack(cmd);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    protected PairTuple<String, String> getCommandFilter(String cmd) {
        try {
            return this.commandFilters.get(cmd);
        } catch (Exception ignore) {
            return null;
        }
    }

    protected boolean hasCommandFilter(String cmd) {
        return this.commands.contains(cmd);
    }

    protected Set<String> getCommandFilterNames() {
        return this.commands;
    }
}
