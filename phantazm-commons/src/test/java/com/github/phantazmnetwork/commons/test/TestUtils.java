package com.github.phantazmnetwork.commons.test;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TestUtils {
    public static void comparativeBenchmark(Supplier<Consumer<String>> first, Supplier<Consumer<String>> second,
                                            String firstName, String secondName, String operationName, int reps,
                                            int iters) {
        long[] secondTimes = new long[reps];
        long[] firstTimes = new long[reps];

        for(int r = 0; r < reps; r++) {
            Consumer<String> firstConsumer = first.get();
            Consumer<String> secondConsumer = second.get();

            String val = Integer.valueOf(r).toString();

            long firstStart = System.nanoTime();
            for(int i = 0; i < iters; i++) {
                firstConsumer.accept(val);
            }
            firstTimes[r] = System.nanoTime() - firstStart;

            long secondStart = System.nanoTime();
            for(int i = 0; i < iters; i++) {
                secondConsumer.accept(val);
            }
            secondTimes[r] = System.nanoTime() - secondStart;
        }

        long secondSum = 0;
        long firstSum = 0;
        for(int i = 0; i < reps; i++) {
            firstSum += firstTimes[i];
            secondSum += secondTimes[i];
        }

        double secondAvg = ((double)secondSum / (double)reps);
        double firstAvg = ((double)firstSum / (double)reps);

        System.out.println(firstName + " average: " + firstAvg + "ns for " + iters + " " + operationName);
        System.out.println(secondName + " average: " + secondAvg + "ns for " + iters + " " + operationName);
        System.out.println(firstName + " is " + secondAvg / firstAvg + "x faster than " + secondName + " over " + reps
                + " repetitions");
    }
}
