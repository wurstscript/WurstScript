package de.peeeq.wurstscript.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A map from keys of type K to values of type V
 * with the additional feature of creating indexes for values.
 * Indexes are automatically kept up to date.
 *
 * Assumption: index functions do not change.
 */
public class MapWithIndexes<K, V> {
    private final Map<K, V> base = new LinkedHashMap<>();
    private final List<BiConsumer<K, V>> onInserts = new ArrayList<>();
    private final List<BiConsumer<K, V>> onDelete = new ArrayList<>();


    public <I> Index<I, K> createIndex(Function<V, I> indexedValue) {
        Multimap<I, K> index = LinkedHashMultimap.create();
        onInserts.add((k, v) -> index.put(indexedValue.apply(v), k));
        onDelete.add((k, v) -> index.remove(indexedValue.apply(v), k));
        return key -> ImmutableList.copyOf(index.get(key));
    }

    public PredIndex<K> createPredicateIndex(Predicate<V> indexedValue) {
        Set<K> index = new LinkedHashSet<>();
        onInserts.add((k, v) -> {
            if (indexedValue.test(v)) {
                index.add(k);
            }
        });
        onDelete.add((k, v) -> {
            index.remove(k);
        });
        return () -> ImmutableList.copyOf(index);
    }

    public <I> Index<I, K> createMultiIndex(Function<V, ? extends Collection<I>> indexedValue) {
        Multimap<I, K> index = LinkedHashMultimap.create();
        onInserts.add((k, v) -> {
            Collection<I> is = indexedValue.apply(v);
            for (I i : is) {
                index.put(i, k);
            }
        });
        onDelete.add((k, v) -> {
            Collection<I> is = indexedValue.apply(v);
            for (I i : is) {
                index.remove(i, k);
            }
        });
        return key -> ImmutableList.copyOf(index.get(key));
    }


    public boolean isEmpty() {
        return base.isEmpty();
    }

    public boolean containsKey(K key) {
        return base.containsKey(key);
    }

    public V get(K key) {
        return base.get(key);
    }

    public V put(K key, V value) {
        Preconditions.checkNotNull(key, "key must not be null");
        Preconditions.checkNotNull(value, "value must not be null");
        V oldV = base.put(key, value);
        if (oldV != null) {
            for (BiConsumer<K, V> f : onDelete) {
                f.accept(key, value);
            }
        }
        for (BiConsumer<K, V> f : onInserts) {
            f.accept(key, value);
        }
        return oldV;
    }

    public V remove(K key) {
        V value = base.remove(key);
        if (value != null) {
            for (BiConsumer<K, V> f : onDelete) {
                f.accept(key, value);
            }
        }
        return value;
    }

    public void clear() {
        for (Map.Entry<K, V> e : base.entrySet()) {
            for (BiConsumer<K, V> f : onDelete) {
                f.accept(e.getKey(), e.getValue());
            }
        }
        base.clear();
    }

    public Set<K> keySet() {
        return base.keySet();
    }

    public Collection<V> values() {
        return base.values();
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return base.entrySet();
    }

    public void removeAll(Collection<K> ks) {
        for (K k : ks) {
            remove(k);
        }
    }

    public interface Index<I, K> {
        Collection<K> lookup(I i);
    }

    public interface PredIndex<K> {
        Collection<K> lookup();
    }
}
