package org.phantazm.zombies.endless;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BasicEndlessTest {
    @Test
    void splitBalancing() {
        double[] weights = new double[]{0.5, 0.5};
        int[] actualValues = new int[2];

        BasicEndless.balance(actualValues, weights);

        assertArrayEquals(new int[]{0, 1}, actualValues);
    }

    @Test
    void complexBalancing() {
        double[] actualMobCounts = new double[]{0.5, 0.5, 10, 15.5};
        int[] actualValues = new int[4];

        BasicEndless.balance(actualValues, actualMobCounts);

        assertArrayEquals(new int[]{0, 1, 10, 16}, actualValues);
    }

    @Test
    void smallArray() {
        double[] actualMobCounts = new double[]{10};
        int[] actualValues = new int[1];

        BasicEndless.balance(actualValues, actualMobCounts);

        assertArrayEquals(new int[]{10}, actualValues);
    }

    @Test
    void invariantTesting() {
        int weightMax = 50;
        for (int i = 0; i <= BasicEndless.ABSOLUTE_MAX_SPAWN_AMOUNT; i++) {
            for (int j = 1; j <= weightMax; j++) {
                for (int k = 1; k <= weightMax; k++) {
                    for (int l = 1; l <= weightMax; l++) {
                        double[] weights = BasicEndless.normalizeWeights(new int[]{j, k, l});

                        double weightSum = 0;
                        for (double weight : weights) {
                            weightSum += weight;
                        }

                        assertEquals(1, weightSum, 0.001);

                        double[] exactMobCounts = new double[]{
                            i * weights[0],
                            i * weights[1],
                            i * weights[2]
                        };

                        assertEquals(i, exactMobCounts[0] + exactMobCounts[1] + exactMobCounts[2], 0.001);

                        int[] mobCounts = new int[3];
                        BasicEndless.balance(mobCounts, exactMobCounts);

                        int actualCount = 0;
                        for (int count : mobCounts) {
                            actualCount += count;
                        }

                        assertEquals(i, actualCount);
                    }
                }
            }
        }
    }
}