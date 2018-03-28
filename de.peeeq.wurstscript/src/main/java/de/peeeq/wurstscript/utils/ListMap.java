package de.peeeq.wurstscript.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 *
 */
public class ListMap<K,V> extends AbstractMap<K,V> {
    private List<? extends K> keys;
    private List<? extends V> values;

    public ListMap(List<? extends K> keys, List<? extends V> values) {
        this.keys = keys;
        this.values = values;
    }

    static class Entry<K,V> implements Map.Entry<K,V> {

        private final K key;
        private final V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V v) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new AbstractSet<Map.Entry<K,V>>() {
            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return new Iterator<Map.Entry<K, V>>() {
                    int pos = 0;

                    @Override
                    public boolean hasNext() {
                        return pos < size();
                    }

                    @Override
                    public Entry<K, V> next() {
                        Entry<K, V> entry = new Entry<>(keys.get(pos), values.get(pos));
                        pos++;
                        return entry;
                    }
                };
            }

            @Override
            public int size() {
                return Math.min(keys.size(), values.size());
            }
        };
    }

    @Override
    public V get(Object key) {
        int index = keys.indexOf(key);
        if (index >= 0 && index < values.size()) {
            return values.get(index);
        }
        return null;
    }

    @Override
    public boolean containsKey(Object key) {
        int index = keys.indexOf(key);
        return index >= 0 && index < values.size();
    }
}
