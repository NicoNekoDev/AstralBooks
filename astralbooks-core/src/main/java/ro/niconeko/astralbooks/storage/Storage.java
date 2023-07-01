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

import org.bukkit.inventory.ItemStack;
import ro.niconeko.astralbooks.AstralBooksPlugin;
import ro.niconeko.astralbooks.utils.tuples.PairTuple;
import ro.niconeko.astralbooks.utils.tuples.TripletTuple;
import ro.niconeko.astralbooks.storage.settings.StorageSettings;
import ro.niconeko.astralbooks.utils.Side;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public abstract class Storage {
    public final AstralBooksPlugin plugin;
    protected Connection connection;
    protected final StorageCache cache;
    protected final StorageType storageType;
    protected boolean loaded;
    protected int purgeSecurityBooksOlderThan = 30;
    public final ReentrantLock lock = new ReentrantLock();

    protected Storage(AstralBooksPlugin plugin, StorageType storageType) {
        this.plugin = plugin;
        this.storageType = storageType;
        this.cache = new StorageCache(plugin, this);
    }

    public final StorageType getStorageType() {
        return this.storageType;
    }

    public final boolean isLoaded() {
        return this.loaded;
    }

    protected boolean load() throws SQLException {
        try {
            this.lock.lock();
            this.plugin.getLogger().info("Loading " + this.storageType.getFormattedName() + " database...");
            this.loadSettings(this.plugin.getSettings().getStorageSettings());
            Class.forName(this.getDriver());
            this.connection = DriverManager.getConnection(this.getURL());
            this.connection.setAutoCommit(true);
            if (!this.createTables())
                return false;
            if (!this.preloadCache())
                return false;
            this.loaded = true;
            return true;
        } catch (ClassNotFoundException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "(" + this.storageType.getFormattedName() + ") Failed to find " + this.storageType.getFormattedName() + " driver!", ex);
            return false;
        } finally {
            this.lock.unlock();
        }
    }

    protected void unload() {
        try {
            this.lock.lock();
            this.plugin.getLogger().info("Unloading " + this.storageType.getFormattedName() + " database...");
            this.loaded = false;
            try {
                this.connection.close();
            } catch (SQLException ex) {
                this.plugin.getLogger().log(Level.SEVERE, "(" + this.storageType.getFormattedName() + ") Failed to unload database!", ex);
            }
        } finally {
            this.lock.unlock();
        }
    }


    protected String getDriver() {
        throw new IllegalStateException("Tried to get " + this.storageType.getFormattedName() + " driver... please report this issue!");
    }

    protected String getURL() {
        throw new IllegalStateException("Tried to get " + this.storageType.getFormattedName() + " url... please report this issue!");
    }

    protected abstract void loadSettings(StorageSettings storageSettings);

    protected abstract boolean createTables();

    protected abstract boolean preloadCache();

    protected abstract Future<ItemStack> getFilterBookStack(String filterName);

    protected abstract Future<ItemStack> getNPCBookStack(int npcId, Side side);

    protected abstract Future<PairTuple<String, String>> getCommandFilterStack(String cmd);

    protected abstract void removeFilterBookStack(String filterName);

    protected abstract void removeNPCBookStack(int npcId, Side side);

    protected abstract void removeCommandFilterStack(String cmd);

    protected abstract void putNPCBookStack(int npcId, Side side, ItemStack book);

    protected abstract void putFilterBookStack(String filterName, ItemStack book);

    protected abstract void putCommandFilterStack(String cmd, String filterName, String permission);

    protected abstract Future<LinkedList<PairTuple<Date, ItemStack>>> getAllBookSecurityStack(UUID uuid, int page, int amount);

    protected abstract Future<LinkedList<TripletTuple<UUID, Date, ItemStack>>> getAllBookSecurityStack(int page, int amount);

    protected abstract void putBookSecurityStack(UUID uuid, Date date, ItemStack book);

    protected abstract Future<ItemStack> getSecurityBookStack(UUID uuid, Date date);

    protected abstract Queue<TripletTuple<Integer, Side, ItemStack>> getAllNPCBookStacks(AtomicBoolean failed);

    protected abstract Queue<PairTuple<String, ItemStack>> getAllFilterBookStacks(AtomicBoolean failed);

    protected abstract Queue<TripletTuple<String, String, String>> getAllCommandFilterStacks(AtomicBoolean failed);

    protected abstract Queue<TripletTuple<UUID, Date, ItemStack>> getAllBookSecurityStacks(AtomicBoolean failed);

    protected abstract void setAllNPCBookStacks(Queue<TripletTuple<Integer, Side, ItemStack>> queue, AtomicBoolean failed);

    protected abstract void setAllFilterBookStacks(Queue<PairTuple<String, ItemStack>> queue, AtomicBoolean failed);

    protected abstract void setAllCommandFilterStacks(Queue<TripletTuple<String, String, String>> queue, AtomicBoolean failed);

    protected abstract void setAllBookSecurityStacks(Queue<TripletTuple<UUID, Date, ItemStack>> queue, AtomicBoolean failed);

    protected abstract Map<UUID, Set<Date>> cleanOldSecurityBookStacks();

}
