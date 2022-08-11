package com.github.phantazmnetwork.core.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BasicSlotDistributorTest {
    @Test
    void test3x3_1Item_padding0() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 1);

        assertArrayEquals(new int[] {4}, slots);
    }

    @Test
    void test3x3_2Items_padding0() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 2);

        assertArrayEquals(new int[] {3, 5}, slots);
    }

    @Test
    void test3x3_3Items_padding0() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 3);

        assertArrayEquals(new int[] {3, 4, 5}, slots);
    }

    @Test
    void test3x3_4Items_padding0() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 4);

        assertArrayEquals(new int[] {0, 1, 2, 7}, slots);
    }

    @Test
    void test3x3_5Items_padding0() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 5);

        assertArrayEquals(new int[] {0, 1, 2, 6, 8}, slots);
    }

    @Test
    void test3x3_6Items_padding0() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 6);

        assertArrayEquals(new int[] {0, 1, 2, 6, 7, 8}, slots);
    }

    @Test
    void test3x3_7Items_padding0() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 7);

        assertArrayEquals(new int[] {0, 1, 2, 3, 4, 5, 7}, slots);
    }

    @Test
    void test3x3_8Items_padding0() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 8);

        assertArrayEquals(new int[] {0, 1, 2, 3, 4, 5, 6, 8}, slots);
    }

    @Test
    void test3x3_9Items_padding0() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(0);
        int[] slots = slotDistributor.distribute(3, 3, 9);

        assertArrayEquals(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8}, slots);
    }

    @Test
    void test3x3_1Item_paddingN() {
        for (int i = 0; i < 1000; i++) {
            SlotDistributor slotDistributor = new BasicSlotDistributor(i);
            int[] slots = slotDistributor.distribute(3, 3, 1);

            assertArrayEquals(new int[] {4}, slots);
        }
    }

    @Test
    void test3x3_2Items_padding1() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(1);
        int[] slots = slotDistributor.distribute(3, 3, 2);

        assertArrayEquals(new int[] {3, 5}, slots);
    }

    @Test
    void test3x3_3Items_padding1() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(1);
        int[] slots = slotDistributor.distribute(3, 3, 3);

        assertArrayEquals(new int[] {0, 2, 7}, slots);
    }

    @Test
    void test3x3_4Items_padding1() {
        SlotDistributor slotDistributor = new BasicSlotDistributor(1);
        int[] slots = slotDistributor.distribute(3, 3, 4);

        assertArrayEquals(new int[] {0, 2, 6, 8}, slots);
    }
}
