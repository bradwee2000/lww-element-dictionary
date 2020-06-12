package com.bwee.crdt;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TimestampedValueTest {

    @Test
    public void testCreate_shouldCreateObject() {
        final TimestampedValue<Integer> val = new TimestampedValue<>(1000, 1);
        assertThat(val.getValue()).isEqualTo(1000);
        assertThat(val.getTimestamp()).isEqualTo(1);
    }

    @Test
    public void testUpdateWithLaterTimestamp_shouldUpdate() {
        final TimestampedValue<Integer> val = new TimestampedValue<>(1000, 1);
        val.setValue(2000, 2);
        assertThat(val.getValue()).isEqualTo(2000);
        assertThat(val.getTimestamp()).isEqualTo(2);
    }

    @Test
    public void testUpdateWithEarlierTimestamp_shouldNotAllowUpdate() {
        final TimestampedValue<Integer> val = new TimestampedValue<>(1000, 10);
        val.setValue(2000, 5);
        assertThat(val.getValue()).isEqualTo(1000);
        assertThat(val.getTimestamp()).isEqualTo(10);
    }

    @Test
    public void testMergeWithLaterValue_shouldReturnNewValueWithTheUpdate() {
        final TimestampedValue<Integer> val1 = new TimestampedValue<>(1000, 10);
        final TimestampedValue<Integer> val2 = new TimestampedValue<>(2000, 20);

        final TimestampedValue<Integer> merged = val1.merge(val2);

        assertThat(merged.getValue()).isEqualTo(2000);
        assertThat(merged.getTimestamp()).isEqualTo(20);
    }

    @Test
    public void testMerge_shouldBeCommutative() {
        final TimestampedValue<Integer> val1 = new TimestampedValue<>(1000, 10);
        final TimestampedValue<Integer> val2 = new TimestampedValue<>(2000, 20);
        assertThat(val1.merge(val2)).isEqualTo(val2.merge(val1));
    }

    @Test
    public void testMerge_shouldBeAssociative() {
        final TimestampedValue<Integer> val1 = new TimestampedValue<>(1000, 10);
        final TimestampedValue<Integer> val2 = new TimestampedValue<>(2000, 20);
        final TimestampedValue<Integer> val3 = new TimestampedValue<>(3000, 30);

        assertThat(val1.merge(val2.merge(val3))).isEqualTo(val1.merge(val2).merge(val3));
    }

    @Test
    public void testMerge_shouldBeIdempotent() {
        final TimestampedValue<Integer> val1 = new TimestampedValue<>(1000, 10);
        final TimestampedValue<Integer> val2 = new TimestampedValue<>(2000, 20);

        assertThat(val1.merge(val1.merge(val1)).getValue()).isEqualTo(1000);
        assertThat(val1.merge(val2).merge(val2).getValue()).isEqualTo(2000);
    }

    @Test
    public void testMergeWithEqualTimestamp_shouldBeCommutative() {
        final TimestampedValue<Integer> val1 = new TimestampedValue<>(1000, 10);
        final TimestampedValue<Integer> val2 = new TimestampedValue<>(2000, 10);

        // Don't care about the value as long as a merge will produce consistent results.
        assertThat(val1.merge(val2)).isEqualTo(val2.merge(val1));
    }
}
