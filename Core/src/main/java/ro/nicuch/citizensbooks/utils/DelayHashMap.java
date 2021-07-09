package ro.nicuch.citizensbooks.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

public class DelayHashMap<K, V> implements DelayMap<K, V> {

    private final ConcurrentMap<K, V> map;
    private final ConcurrentMap<K, DelayedKey<K>> keysMap;
    private final DelayQueue<DelayedKey<K>> queue = new DelayQueue<>();
    private final long defaultLifeTime;

    public DelayHashMap() {
        this(5, TimeUnit.MINUTES, 16, 0.75f);
    }

    public DelayHashMap(long defaultLifeTime, TimeUnit unit) {
        this(defaultLifeTime, unit, 16, 0.75f);
    }

    public DelayHashMap(long defaultLifeTime, TimeUnit unit, int initialCapacity) {
        this(defaultLifeTime, unit, initialCapacity, 0.75f);
    }

    public DelayHashMap(long defaultLifeTime, TimeUnit unit, int initialCapacity, float loadFactor) {
        this.map = new ConcurrentHashMap<>(initialCapacity, loadFactor);
        this.keysMap = new ConcurrentHashMap<>(initialCapacity, loadFactor);
        this.defaultLifeTime = TimeUnit.MILLISECONDS.convert(defaultLifeTime, unit);
    }

    @Override
    public boolean renewKey(K key, long lifeTimeMillis) {
        DelayedKey<K> delayedKey = this.keysMap.get(key);
        if (delayedKey != null) {
            delayedKey.renew(lifeTimeMillis);
            return true;
        }
        return false;
    }

    @Override
    public boolean renewKey(K key, long lifeTime, TimeUnit unit) {
        return this.renewKey(key, TimeUnit.MILLISECONDS.convert(lifeTime, unit));
    }

    @Override
    public boolean renewKey(K key) {
        return this.renewKey(key, this.defaultLifeTime);
    }

    @Override
    public void expireKey(K key) {
        DelayedKey<K> delayedKey = this.keysMap.get(key);
        if (delayedKey == null)
            return;
        delayedKey.expire();
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.map.get(key);
    }

    @Nullable
    @Override
    public V put(K key, V value, long lifeTimeMillis) {
        DelayedKey<K> newKey = new DelayedKey<>(key, lifeTimeMillis);
        DelayedKey<K> oldKey = this.keysMap.put(key, newKey);
        if (oldKey != null)
            this.queue.remove(oldKey);
        this.queue.offer(newKey);
        return this.map.put(key, value);
    }

    @Nullable
    @Override
    public V put(K key, V value, long lifeTime, TimeUnit unit) {
        return this.put(key, value, TimeUnit.MILLISECONDS.convert(lifeTime, unit));
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        return this.put(key, value, this.defaultLifeTime);
    }

    @Override
    public V remove(Object key) {
        DelayedKey<K> oldKey = this.keysMap.remove(key);
        if (oldKey != null)
            this.queue.remove(oldKey);
        return this.map.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        this.putAll(map, this.defaultLifeTime);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map, long lifeTimeMillis) {
        this.map.putAll(map);
        for (K key : map.keySet()) {
            DelayedKey<K> delayedKey = new DelayedKey<>(key, lifeTimeMillis);
            this.keysMap.put(key, delayedKey);
            this.queue.offer(delayedKey);
        }
    }

    @Override
    public void clear() {
        this.map.clear();
        this.keysMap.clear();
        this.queue.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return this.map.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return this.map.values();
    }

    @NotNull
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return this.map.entrySet();
    }

    @Override
    public void cleanup(BiConsumer<K, V> whenRemoved) {
        DelayedKey<K> delayedKey = this.queue.poll();
        while (delayedKey != null) {
            V value = this.map.remove(delayedKey.getKey());
            this.keysMap.remove(delayedKey.getKey());
            whenRemoved.accept(delayedKey.getKey(), value);
            delayedKey = this.queue.poll();
        }
    }

    @Override
    public void cleanup() {
        DelayedKey<K> delayedKey = this.queue.poll();
        while (delayedKey != null) {
            V value = this.map.remove(delayedKey.getKey());
            this.keysMap.remove(delayedKey.getKey());
            delayedKey = this.queue.poll();
        }
    }

    private static class DelayedKey<K> implements Delayed {

        private long startTime = System.currentTimeMillis();
        private long lifeTimeMillis;
        private final K key;

        public DelayedKey(K key, long lifeTimeMillis) {
            this.lifeTimeMillis = lifeTimeMillis;
            this.key = key;
        }

        public DelayedKey(K key, long time, TimeUnit unit) {
            this(key, TimeUnit.MILLISECONDS.convert(time, unit));
        }

        public K getKey() {
            return key;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DelayedKey<K> that = (DelayedKey<K>) o;
            return this.key.equals(that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.key);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(getDelayMillis(), TimeUnit.MILLISECONDS);
        }

        private long getDelayMillis() {
            return (this.startTime + this.lifeTimeMillis) - System.currentTimeMillis();
        }

        public void renew(long lifeTimeMillis) {
            this.startTime = System.currentTimeMillis();
            this.lifeTimeMillis = lifeTimeMillis;
        }

        public void expire() {
            this.startTime = Long.MIN_VALUE;
        }

        @SuppressWarnings("unchecked")
        @Override
        public int compareTo(@NotNull Delayed that) {
            return Long.compare(this.getDelayMillis(), ((DelayedKey<K>) that).getDelayMillis());
        }
    }
}
