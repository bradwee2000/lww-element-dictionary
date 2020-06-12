package com.bwee.crdt;

import org.junit.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class LwwElementDictionaryTest {
    private static int counter = 0;

    private final Supplier<Long> timestamp = () -> (long) counter++;
    private final Supplier<Long> frozenTimestamp = () -> 1l;

    @Test
    public void testContains_shouldReturnTrueIfEntryExists() {
        final LwwElementDictionary<String, String> map = new LwwElementDictionary<>(timestamp);
        map.put("A", "Apple");
        assertThat(map.contains("A")).isTrue();
        assertThat(map.contains("UNKNOWN")).isFalse();
    }

    @Test
    public void testPutAndGet_shouldReturnValueAssociatedWithKey() {
        final LwwElementDictionary<String, String> map = new LwwElementDictionary<>(timestamp);
        map.put("A", "Apple");
        map.put("O", "Orange");
        assertThat(map.get("A")).isEqualTo("Apple");
        assertThat(map.get("O")).isEqualTo("Orange");
    }

    @Test
    public void testPutNullValue_shouldAllowNulls() {
        final LwwElementDictionary<String, String> map = new LwwElementDictionary<>(timestamp);
        map.put("A", null);
        assertThat(map.get("A")).isNull();
    }

    @Test
    public void testPutNullKey_shouldAllowNullKey() {
        final LwwElementDictionary<String, String> map = new LwwElementDictionary<>(timestamp);
        map.put(null, "Apple");
        assertThat(map.get(null)).isEqualTo("Apple");
    }

    @Test
    public void testGetUnknownKey_shouldReturnNull() {
        final LwwElementDictionary<String, String> map = new LwwElementDictionary<>(timestamp);
        assertThat(map.get("UNKNOWN")).isNull();
    }

    @Test
    public void testPutOverwrite_shouldOverwriteValue() {
        final LwwElementDictionary<String, String> map = new LwwElementDictionary<>(timestamp);
        map.put("A", "Apple");
        map.put("B", "Banana");
        map.put("A", "Alligator");

        assertThat(map.get("A")).isEqualTo("Alligator");
        assertThat(map.get("B")).isEqualTo("Banana"); // should be unaffected
    }

    @Test
    public void testRemoveExistingKey_shouldRemoveEntry() {
        final LwwElementDictionary<String, String> map = new LwwElementDictionary<>(timestamp);
        map.put("A", "Apple");
        map.remove("A");
        assertThat(map.get("A")).isNull();
    }

    @Test
    public void testClear_shouldHaveEmptyMap() {
        final LwwElementDictionary<String, String> map = new LwwElementDictionary<>(timestamp);
        map.put("A", "Apple");
        map.clear();
        assertThat(map.contains("A")).isFalse();
    }

    @Test
    public void testMerge_shouldJoinOperationsFromAllMaps() {
        final LwwElementDictionary<String, String> mapA = new LwwElementDictionary<>(timestamp);
        final LwwElementDictionary<String, String> mapB = new LwwElementDictionary<>(timestamp);
        final LwwElementDictionary<String, String> mapC = new LwwElementDictionary<>(timestamp);

        mapA.put("A", "Apple");
        mapA.put("B", "Banana");
        mapA.remove("C");       // ignored because timestamp is earlier

        mapB.put("B", "Brains");     // should overwrite Banana
        mapB.put("C", "Carrot");
        mapB.remove("A");       // should remove Apple from mapA

        mapC.put("C", "Cat");        // should overwrite Carrot

        final LwwElementDictionary<String, String> merged = mapA.merge(mapB).merge(mapC);

        assertThat(merged.contains("A")).isFalse();
        assertThat(merged.get("B")).isEqualTo("Brains");
        assertThat(merged.get("C")).isEqualTo("Cat");
    }

    @Test
    public void testMerge_shouldBeCommutative() {
        final LwwElementDictionary<String, String> mapA = new LwwElementDictionary<>(timestamp);
        final LwwElementDictionary<String, String> mapB = new LwwElementDictionary<>(timestamp);

        mapA.put("A", "Apple");
        mapA.put("B", "Banana");
        mapA.remove("C");

        mapB.put("B", "Brains");
        mapB.put("C", "Carrot");
        mapB.remove("A");

        final LwwElementDictionary<String, String> mergedA = mapA.merge(mapB); // A + B
        final LwwElementDictionary<String, String> mergedB = mapB.merge(mapA); // B + C

        // Assert that A + B = B + C
        assertThat(mergedA).isEqualTo(mergedB);
    }

    @Test
    public void testMerge_shouldBeAssociative() {
        final LwwElementDictionary<String, String> mapA = new LwwElementDictionary<>(timestamp);
        final LwwElementDictionary<String, String> mapB = new LwwElementDictionary<>(timestamp);
        final LwwElementDictionary<String, String> mapC = new LwwElementDictionary<>(timestamp);

        mapA.put("A", "Apple");
        mapA.put("B", "Banana");
        mapA.remove("E");      // ignored due to earlier timestamp

        mapB.put("B", "Brains");    // should overwrite Banana
        mapB.put("C", "Carrot");
        mapB.remove("A");

        mapC.put("A", "Alligator"); // should overwrite Apple
        mapC.put("D", "Doughnut");
        mapC.put("E", "Eggplant");

        final LwwElementDictionary<String, String> mergedA = mapA.merge(mapB).merge(mapC); // (A + B) + C
        final LwwElementDictionary<String, String> mergedB = mapA.merge(mapB.merge(mapC)); // A + (B + C)

        // Assert that (A + B) + C = A + (B + C)
        assertThat(mergedA).isEqualTo(mergedB);
    }

    @Test
    public void testMultipleMerge_shouldBeIdempotent() {
        final LwwElementDictionary<String, String> mapA = new LwwElementDictionary<>(timestamp);
        final LwwElementDictionary<String, String> mapB = new LwwElementDictionary<>(timestamp);

        mapA.put("A", "Apple");
        mapA.remove("B");       // Ignored due to earlier timestamp

        mapB.put("A", "Alligator");  // should overwrite Apple
        mapB.put("B", "Banana");

        // Merge multiple times
        final LwwElementDictionary<String, String> merged = mapA.merge(mapB).merge(mapB).merge(mapA);

        assertThat(merged.get("A")).isEqualTo("Alligator");
        assertThat(merged.get("B")).isEqualTo("Banana");
    }

    @Test
    public void testMergeWithUpdatesOfEqualTimestamp_shouldBeCommutative() {
        // Freeze timestamp
        final LwwElementDictionary<String, String> mapA = new LwwElementDictionary<>(frozenTimestamp);
        final LwwElementDictionary<String, String> mapB = new LwwElementDictionary<>(frozenTimestamp);

        mapA.put("A", "Apple");
        mapA.put("B", "Banana");
        mapB.put("A", "Alligator");
        mapB.put("B", "Bison");

        final LwwElementDictionary<String, String> mergedA = mapA.merge(mapB);
        final LwwElementDictionary<String, String> mergedB = mapB.merge(mapA);

        // Don't care what value it got. Just need to make sure they are equal.
        assertThat(mergedA.get("A")).isEqualTo(mergedB.get("A"));
        assertThat(mergedA.get("B")).isEqualTo(mergedB.get("B"));
    }

    @Test
    public void testOperationsWithEqualTimestamp_shouldBeBiasedTowardRemoveOperation() {
        final LwwElementDictionary<String, String> mapA = new LwwElementDictionary<>(frozenTimestamp);
        final LwwElementDictionary<String, String> mapB = new LwwElementDictionary<>(frozenTimestamp);

        mapA.remove("A");
        mapB.put("A", "Apple");

        assertThat(mapA.merge(mapB).get("A")).isNull();
    }
}