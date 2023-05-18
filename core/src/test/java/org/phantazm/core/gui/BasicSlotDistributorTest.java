package org.phantazm.core.gui;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BasicSlotDistributorTest {
    @Test
    void test3x3_1Item_0padding() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 1);

        assertArrayEquals(new int[] {4}, slots);
    }

    @Test
    void test3x3_2Items_0padding() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 2);

        assertArrayEquals(new int[] {3, 5}, slots);
    }

    @Test
    void test3x3_3Items_0padding() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 3);

        assertArrayEquals(new int[] {3, 4, 5}, slots);
    }

    @Test
    void test3x3_4Items_0padding() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 4);

        assertArrayEquals(new int[] {0, 1, 2, 7}, slots);
    }

    @Test
    void test3x3_5Items_0padding() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 5);

        assertArrayEquals(new int[] {0, 1, 2, 6, 8}, slots);
    }

    @Test
    void test3x3_6Items_0padding() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 6);

        assertArrayEquals(new int[] {0, 1, 2, 6, 7, 8}, slots);
    }

    @Test
    void test3x3_7Items_0padding() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 7);

        assertArrayEquals(new int[] {0, 1, 2, 3, 4, 5, 7}, slots);
    }

    @Test
    void test3x3_8Items_0padding() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 8);

        assertArrayEquals(new int[] {0, 1, 2, 3, 4, 5, 6, 8}, slots);
    }

    @Test
    void test3x3_9Items_0padding() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 9);

        assertArrayEquals(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8}, slots);
    }

    @Test
    void test3x3_1Item_Npadding() {
        for (int i = 0; i < 1000; i++) {
            SlotDistributor slotDistributor = new BasicSlotDistributor(i);
            int[] slots = slotDistributor.distribute(3, 3, 1);

            assertArrayEquals(new int[] {4}, slots);
        }
    }

    @Test
    void test3x3_2Items_1padding() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(1);
        int[] slots = slotDistributor.distribute(3, 3, 2);

        assertArrayEquals(new int[] {3, 5}, slots);
    }

    @Test
    void test3x3_3Items_1padding() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(1);
        int[] slots = slotDistributor.distribute(3, 3, 3);

        assertArrayEquals(new int[] {0, 2, 7}, slots);
    }

    @Test
    void test3x3_4Items_1padding() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(1);
        int[] slots = slotDistributor.distribute(3, 3, 4);

        assertArrayEquals(new int[] {0, 2, 6, 8}, slots);
    }

    @Test
    void test3x3_4Items_Npadding() {
        for (int i = 3; i < 1000; i++) {
            SlotDistributor slotDistributor = new BasicSlotDistributor(i);
            int[] slots = Assertions.assertDoesNotThrow(() -> slotDistributor.distribute(3, 3, 4), "i: " + i);

            assertArrayEquals(new int[] {0, 2, 6, 8}, slots, "i: " + i);
        }
    }

    @Test
    void test3x3_9Items_Npadding() {
        for (int i = 1; i < 1000; i++) {
            SlotDistributor slotDistributor = new BasicSlotDistributor(i);
            int[] slots = Assertions.assertDoesNotThrow(() -> slotDistributor.distribute(3, 3, 9), "i: " + i);

            assertArrayEquals(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8}, slots, "i: " + i);
        }
    }


    @Test
    void test3x3_2Items_2padding() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(2);
        int[] slots = slotDistributor.distribute(3, 3, 2);

        assertArrayEquals(new int[] {3, 5}, slots);
    }

    @Test
    void throwsWhenTooBig() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        assertThrows(IllegalArgumentException.class, () -> slotDistributor.distribute(3, 3, 10));
    }
}
