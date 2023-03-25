package ro.niconeko.astralbooks.storage;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.utils.Side;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StorageCache {
    protected final AstralBooksPlugin plugin;
    protected final AbstractStorage storage;
    public ExecutorService poolExecutor;
    public LoadingCache<String, ItemStack> filterBooks;
    public LoadingCache<Pair<Integer, Side>, ItemStack> npcBooks;
    public LoadingCache<String, Pair<String, String>> commandFilters;
    public final Set<String> filters = new HashSet<>();
    public final Set<Pair<Integer, Side>> npcs = new HashSet<>();
    public final Set<String> commands = new HashSet<>();

    protected StorageCache(AstralBooksPlugin plugin, AbstractStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public void load() {
        this.poolExecutor = Executors.newFixedThreadPool(this.plugin.getSettings().getStorageSettings().getDatabaseThreads());
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
                    public @NotNull Pair<String, String> load(@NotNull String key) throws Exception {
                        return storage.getCommandFilterStack(key).get();
                    }
                });
        this.npcBooks = CacheBuilder.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull ItemStack load(@NotNull Pair<Integer, Side> key) throws Exception {
                        return storage.getNPCBookStack(key.getFirstValue(), key.getSecondValue()).get();
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
            //noinspection ResultOfMethodCallIgnored
            this.poolExecutor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean putNPCBook(int npcId, Side side, ItemStack book) {
        try {
            this.storage.putNPCBookStack(npcId, side, book);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    protected boolean removeNPCBook(int npcId, Side side) {
        try {
            this.storage.removeNPCBookStack(npcId, side);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    protected ItemStack getNPCBook(int npcId, Side side, ItemStack def) {
        try {
            return this.npcBooks.get(Pair.of(npcId, side));
        } catch (Exception e) {
            return def;
        }
    }

    protected boolean hasNPCBook(int npcId, Side side) {
        return this.npcs.contains(Pair.of(npcId, side));
    }

    protected Set<Pair<Integer, Side>> getNPCBooks() {
        return this.npcs;
    }

    protected boolean putFilterBook(String filterName, ItemStack book) {
        try {
            this.storage.putFilterBookStack(filterName, book);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    protected boolean removeFilterBook(String filterName) {
        try {
            this.storage.removeFilterBookStack(filterName);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
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
            ex.printStackTrace();
            return false;
        }
    }

    protected boolean removeCommandFilter(String cmd) {
        try {
            this.storage.removeCommandFilterStack(cmd);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    protected Pair<String, String> getCommandFilter(String cmd) {
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
