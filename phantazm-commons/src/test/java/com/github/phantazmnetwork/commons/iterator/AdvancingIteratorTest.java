package com.github.phantazmnetwork.commons.iterator;

import com.github.phantazmnetwork.commons.iterator.AdvancingIterator;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class AdvancingIteratorTest {
    @Test
    void nextFailsNoAdvance() {
        Iterator<String> iterator = new AdvancingIterator<>() {
            @Override
            public boolean advance() {
                return false;
            }
        };

        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void nextSucceedsThenFails() {
        Iterator<String> iterator = new AdvancingIterator<>() {
            int state = 0;

            @Override
            public boolean advance() {
                value = Integer.toString(state);
                return state++ == 0;
            }
        };

        assertEquals("0", iterator.next());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void redundantHasNext() {
        AdvancingIterator<String> iterator = new AdvancingIterator<>() {
            int state = 0;

            @Override
            public boolean advance() {
                value = Integer.toString(state);
                return state++ == 0;
            }
        };

        for(int i = 0; i < 10; i++) {
            assertTrue(iterator.hasNext());
        }

        assertEquals("0", iterator.value);
    }
}