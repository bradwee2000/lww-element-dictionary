package com.bwee.crdt;

public interface Crdt<T extends Crdt<T>> {

    T merge(T other);
}
