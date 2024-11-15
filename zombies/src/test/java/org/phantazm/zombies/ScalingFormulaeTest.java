package org.phantazm.zombies;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScalingFormulaeTest {
    private static final double MARGIN_OF_ERROR = 0.0001;

    // steank originally misspelled this and blamed it on me - cob
    @Test
    void percentageAboveLargerNumberReturnsZero() {
        double result = ScalingFormulae.computeMultiplier(true, 1, true,
            0.8, 0.7, 69, 420);
        assertEquals(0, result, MARGIN_OF_ERROR);
    }

    @Test
    void percentageBelowSmallerNumberReturnsZero() {
        double result = ScalingFormulae.computeMultiplier(true, 0.6, true,
            0.8, 0.7, 69, 420);

        assertEquals(0, result, MARGIN_OF_ERROR);
    }

    @Test
    void percentageAboveLargerNumberReturnsMinMultiplier() {
        double result = ScalingFormulae.computeMultiplier(false, 1,
            true, 0.8, 0.7, 69, 420);

        assertEquals(69, result, MARGIN_OF_ERROR);
    }

    @Test
    void percentageBelowSmallerNumberReturnsMaxMultiplier() {
        double result = ScalingFormulae.computeMultiplier(false, 0.6,
            true, 0.8, 0.7, 69, 420);

        assertEquals(420, result, MARGIN_OF_ERROR);
    }

    @Test
    void case1() {
        double result = ScalingFormulae.computeMultiplier(true, 0.5,
            false, 1, 0, 0, 10);

        assertEquals(5, result, MARGIN_OF_ERROR);
    }

    @Test
    void case2() {
        double result = ScalingFormulae.computeMultiplier(true, 0.5,
            false, 1, 0, 0, 1);

        assertEquals(0.5, result, MARGIN_OF_ERROR);
    }

    @Test
    void case3() {
        double result = ScalingFormulae.computeMultiplier(true, 0.75,
            false, 1, 0, 0, 1);

        assertEquals(0.75, result, MARGIN_OF_ERROR);
    }

    @Test
    void cursed() {
        double result = ScalingFormulae.computeMultiplier(false, 0.71,
            true, 0.78, 0.22, 5, -2);

        assertEquals(4.125, result, MARGIN_OF_ERROR);
    }

    @Test
    void cursed2() {
        double result = ScalingFormulae.computeMultiplier(false, 0.71,
            false, 0.78, 0.22, 5, -2);

        assertEquals(-1.125, result, MARGIN_OF_ERROR);
    }

    @Test
    void cursed3() {
        double result = ScalingFormulae.computeMultiplier(false, 0.71,
            true, 0.78, 0.22, -2, 5);

        assertEquals(-1.125, result, MARGIN_OF_ERROR);
    }

    @Test
    void cursed4() {
        double result = ScalingFormulae.computeMultiplier(false, 0.71,
            false, 0.78, 0.22, -2, 5);

        assertEquals(4.125, result, MARGIN_OF_ERROR);
    }
}