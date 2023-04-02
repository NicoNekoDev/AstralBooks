package ro.niconeko.astralbooks.storage;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.NicoNekoDev.SimpleTuples.Pair;
import io.github.NicoNekoDev.SimpleTuples.Triplet;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.utils.Side;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StorageCache {
    protected final AstralBooksPlugin plugin;
    protected final Storage storage;
    public ExecutorService poolExecutor;
    public LoadingCache<String, ItemStack> filterBooks;
    public LoadingCache<Pair<Integer, Side>, ItemStack> npcBooks;
    public LoadingCache<String, Pair<String, String>> commandFilters;
    public final Set<String> filters = new HashSet<>();
    public final Set<Pair<Integer, Side>> npcs = new HashSet<>();
    public final Set<String> commands = new HashSet<>();

    public LoadingCache<Triplet<UUID, Integer, Integer>, LinkedList<Pair<Date, ItemStack>>> allPlayersSecurityBooks;
    public LoadingCache<Pair<Integer, Integer>, LinkedList<Triplet<UUID, Date, ItemStack>>> allSecurityBooks;
    public LoadingCache<Pair<UUID, Date>, ItemStack> playerSecurityBooks;
    public LoadingCache<UUID, Set<Date>> playerTimestamps;

    protected StorageCache(AstralBooksPlugin plugin, Storage storage) {
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
        this.allPlayersSecurityBooks = CacheBuilder.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull LinkedList<Pair<Date, ItemStack>> load(@NotNull Triplet<UUID, Integer, Integer> key) throws Exception {
                        return storage.getAllBookSecurityStack(key.getFirstValue(), key.getSecondValue(), key.getThirdValue()).get();
                    }
                });
        this.allSecurityBooks = CacheBuilder.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull LinkedList<Triplet<UUID, Date, ItemStack>> load(@NotNull Pair<Integer, Integer> key) throws Exception {
                        return storage.getAllBookSecurityStack(key.getFirstValue(), key.getSecondValue()).get();
                    }
                });
        this.playerSecurityBooks = CacheBuilder.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull ItemStack load(@NotNull Pair<UUID, Date> key) throws Exception {
                        return storage.getSecurityBookStack(key.getFirstValue(), key.getSecondValue()).get();
                    }
                });
        this.playerTimestamps = CacheBuilder.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull Set<Date> load(@NotNull UUID key) {
                        Set<Date> result = new HashSet<>();
                        // this is stupid!
                        for (Map.Entry<Triplet<UUID, Integer, Integer>, LinkedList<Pair<Date, ItemStack>>> entry : StorageCache.this.allPlayersSecurityBooks.asMap().entrySet()) {
                            if (entry.getKey().getFirstValue().equals(key))
                                for (Pair<Date, ItemStack> pair : entry.getValue())
                                    result.add(pair.getFirstValue());
                        }
                        for (LinkedList<Triplet<UUID, Date, ItemStack>> tripletList : StorageCache.this.allSecurityBooks.asMap().values()) {
                            for (Triplet<UUID, Date, ItemStack> triplet : tripletList) {
                                if (triplet.getFirstValue().equals(key))
                                    result.add(triplet.getSecondValue());
                            }
                        }
                        return result;
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

    protected LinkedList<Pair<Date, ItemStack>> getAllBookSecurity(UUID uuid, int page, int amount) {
        try {
            return this.allPlayersSecurityBooks.get(Triplet.of(uuid, page, amount));
        } catch (ExecutionException e) {
            return new LinkedList<>();
        }
    }

    protected LinkedList<Triplet<UUID, Date, ItemStack>> getAllBookSecurity(int page, int amount) {
        try {
            return this.allSecurityBooks.get(Pair.of(page, amount));
        } catch (ExecutionException e) {
            return new LinkedList<>();
        }
    }

    protected boolean putBookSecurity(UUID uuid, Date date, ItemStack book) {
        try {
            this.storage.putBookSecurityStack(uuid, date, book);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    protected ItemStack getBookSecurity(UUID uuid, Date date) {
        try {
            return this.playerSecurityBooks.get(Pair.of(uuid, date));
        } catch (Exception ex) {
            return null;
        }
    }
}
