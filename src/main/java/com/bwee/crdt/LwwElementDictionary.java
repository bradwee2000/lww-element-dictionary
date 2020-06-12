package com.bwee.crdt;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static java.lang.Long.MIN_VALUE;
import static java.lang.Math.max;

public class LwwElementDictionary<K, V> implements Dictionary<K, V, LwwElementDictionary<K, V>> {
    private final Map<K, TimestampedValue<V>> adds;
    private final Map<K, Long> removes;
    private final Supplier<Long> timestampSupplier;

    public LwwElementDictionary(final Supplier<Long> timestampSupplier) {
        this(timestampSupplier, new HashMap<>(), new HashMap<>());
    }

    private LwwElementDictionary(final Supplier<Long> timestampSupplier,
                                 final Map<K, TimestampedValue<V>> adds,
                                 final Map<K, Long> removes) {
        this.timestampSupplier = timestampSupplier;
        this.adds = adds;
        this.removes = removes;
    }

    /**
     * Adds the key-value pair to the map.
     * <p>
     * This operation updates the timestamp associated with the add entry.
     */
    @Override
    public void put(final K key, final V value) {
        final long timestamp = timestampSupplier.get();
        if (adds.containsKey(key)) {
            adds.get(key).setValue(value, timestamp);
        } else {
            System.out.println("LALALLALA" + key);
            adds.put(key, new TimestampedValue<>(value, timestamp));
        }

        removeDuplicates(key);
    }

    /**
     * Removes the key-value pair from the map given its key.
     * <p>
     * This adds an entry to the remove list or updates its timestamp.
     */
    @Override
    public void remove(final K key) {
        final long timestamp = timestampSupplier.get();

        // save max of existing and current timestamp
        removes.put(key, max(timestamp, removes.getOrDefault(key, MIN_VALUE)));

        removeDuplicates(key);
    }

    /**
     * Returns the value if the key exists in the map. Otherwise, returns null.
     */
    @Override
    public V get(final K key) {
        return contains(key) ? adds.get(key).getValue() : null;
    }

    /**
     * Return true if key exists in the map.
     * <p>
     * The key exists if it's in the add list and not in the remove list or is in the remove list but with an earlier
     * timestamp than that in the add list.
     */
    @Override
    public boolean contains(final K key) {
        // If key is not in adds, return false
        if (!adds.containsKey(key)) {
            return false;
        }

        // If key is in adds but not in removes, return true
        if (!removes.containsKey(key)) {
            return true;
        }

        // If timestamps are equal, bias is toward the remove action
        return removes.get(key) < adds.get(key).getTimestamp();
    }

    @Override
    public LwwElementDictionary<K, V> merge(final LwwElementDictionary<K, V> other) {
        final Map<K, TimestampedValue<V>> mergedAdds = new HashMap<>(adds);
        final Map<K, Long> mergedRemoves = new HashMap<>(removes);

        // Union of adds -> update value if timestamp is later
        other.adds.entrySet().forEach(e ->
                mergedAdds.put(e.getKey(), mergedAdds.getOrDefault(e.getKey(), e.getValue()).merge(e.getValue())));

        // Union of removes -> set max of timestamp
        other.removes.entrySet().forEach(e ->
                mergedRemoves.put(e.getKey(), max(e.getValue(), mergedRemoves.getOrDefault(e.getKey(), MIN_VALUE))));

        return new LwwElementDictionary<>(timestampSupplier, mergedAdds, mergedRemoves);
    }

    @Override
    public void clear() {
        adds.clear();
        removes.clear();
    }

    private void removeDuplicates(final K key) {
        removeDuplicates(key, adds, removes);
    }

    private void removeDuplicates(final K key, final Map<K, TimestampedValue<V>> adds, final Map<K, Long> removes) {
        if (adds.containsKey(key) && removes.containsKey(key)) {
            final long addTs = adds.get(key).getTimestamp();
            final long removeTs = removes.get(key);
            if (addTs > removeTs) {
                removes.remove(key);
            } else {
                adds.remove(key);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LwwElementDictionary<?, ?> crdtMap = (LwwElementDictionary<?, ?>) o;

        return Objects.equals(adds, crdtMap.adds) &&
                Objects.equals(removes, crdtMap.removes) &&
                Objects.equals(timestampSupplier, crdtMap.timestampSupplier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adds, removes, timestampSupplier);
    }

    @Override
    public String toString() {
        return "CrdtMap{" +
                "adds=" + adds +
                ", removes=" + removes +
                ", timestampSupplier=" + timestampSupplier +
                '}';
    }

}
