package com.bwee.crdt;

public interface Dictionary<K, V, T extends Dictionary<K, V, T>> extends Crdt<T>{

    /**
     * Adds a key-value pair entry. Succeeding puts of the same key will overwrite the value.
     */
    void put(K key, V value);

    /**
     * Removes the key-value entry given its key.
     */
    void remove(K key);

    /**
     * Returns the value given its key.
     */
    V get(K key);

    /**
     * Return true if key exists in the entries.
     */
    boolean contains(K key);

    /**
     * Removes all key-value pairs.
     */
    void clear();
}
