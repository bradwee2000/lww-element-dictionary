package com.bwee.crdt;

import java.util.Objects;

/**
 * Holds a value and allows updates only if the new value timestamp is later.
 */
class TimestampedValue<V> implements Crdt<TimestampedValue<V>> {
    private V value;
    private long timestamp;

    TimestampedValue(V value, long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public V getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Accept new value only if timestamp is later. In the event that timestamps are equal, it compares the hashcode.
     */
    public TimestampedValue<V> setValue(final V value, final long timestamp) {
        if (timestamp > this.timestamp || (this.timestamp == timestamp && this.value.hashCode() > value.hashCode())) {
            this.value = value;
            this.timestamp = timestamp;
        }
        return this;
    }

    @Override
    public TimestampedValue<V> merge(final TimestampedValue<V> other) {
        return new TimestampedValue(value, timestamp).setValue(other.value, other.timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimestampedValue<?> value1 = (TimestampedValue<?>) o;
        return timestamp == value1.timestamp &&
                Objects.equals(value, value1.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, timestamp);
    }

    @Override
    public String toString() {
        return "TimestampedValue{" +
                "value=" + value +
                ", timestamp=" + timestamp +
                '}';
    }
}
