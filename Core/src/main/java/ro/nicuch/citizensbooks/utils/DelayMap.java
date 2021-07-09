package ro.nicuch.citizensbooks.utils;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public interface DelayMap<K, V> extends Map<K, V> {

    boolean renewKey(K key, long lifeTimeMillis);

    boolean renewKey(K key, long lifeTime, TimeUnit unit);

    boolean renewKey(K key);

    void expireKey(K key);

    @Nullable V put(K key, V value, long lifeTimeMillis);

    @Nullable V put(K key, V value, long lifeTime, TimeUnit unit);

    void putAll(Map<? extends K, ? extends V> map, long lifeTimeMillis);

    void cleanup(BiConsumer<K, V> whenRemoved);

    void cleanup();
}
