package com.github.phantazmnetwork.commons.iterator;

import java.util.Iterator;

final class SynchronizedIterator<TValue> implements EnhancedIterator<TValue> {
    private final Object sync = new Object();
    private final Iterator<? extends TValue> iterator;

    SynchronizedIterator(Iterator<? extends TValue> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        synchronized (sync) {
            return iterator.hasNext();
        }
    }

    @Override
    public TValue next() {
        synchronized (sync) {
            return iterator.next();
        }
    }
}
