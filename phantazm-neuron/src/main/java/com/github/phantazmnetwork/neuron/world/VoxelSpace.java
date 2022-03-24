package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.iterator.AdvancingIterator;
import com.github.phantazmnetwork.commons.vector.Vec3IFunction;
import org.jetbrains.annotations.NotNull;

public abstract class VoxelSpace implements Space {
    @SuppressWarnings("DuplicatedCode")
    @Override
    public @NotNull Iterable<? extends Solid> solidsOverlapping(double oX, double oY, double oZ, double vX, double vY,
                                                                double vZ, @NotNull Order order) {
        int xInc = (int) Math.signum(vX);
        int yInc = (int) Math.signum(vY);
        int zInc = (int) Math.signum(vZ);

        int xOrg = (int) Math.floor(oX);
        int yOrg = (int) Math.floor(oY);
        int zOrg = (int) Math.floor(oZ);

        int xEnd = Math.abs((int) Math.floor(oX + vX));
        int yEnd = Math.abs((int) Math.floor(oY + vY));
        int zEnd = Math.abs((int) Math.floor(oZ + vZ));

        int firstOrg;
        int secondOrg;
        int thirdOrg;

        int firstInc;
        int secondInc;
        int thirdInc;

        int firstEnd;
        int secondEnd;
        int thirdEnd;

        Vec3IFunction<Solid> solidGetter;

        //TODO: switch cases here are triggering the duplication warning, find a way to deduplicate if possible (?)
        switch (order) {
            case XYZ -> {
                firstOrg = xOrg;
                secondOrg = yOrg;
                thirdOrg = zOrg;

                firstInc = xInc;
                secondInc = yInc;
                thirdInc = zInc;

                firstEnd = xEnd;
                secondEnd = yEnd;
                thirdEnd = zEnd;

                solidGetter = this::solidAt;
            }
            case YXZ -> {
                firstOrg = yOrg;
                secondOrg = xOrg;
                thirdOrg = zOrg;

                firstInc = yInc;
                secondInc = xInc;
                thirdInc = zInc;

                firstEnd = yEnd;
                secondEnd = xEnd;
                thirdEnd = zEnd;

                solidGetter = (f, s, t) -> solidAt(s, f, t);
            }
            case YZX -> {
                firstOrg = yOrg;
                secondOrg = zOrg;
                thirdOrg = xOrg;

                firstInc = yInc;
                secondInc = zInc;
                thirdInc = xInc;

                firstEnd = yEnd;
                secondEnd = zEnd;
                thirdEnd = xEnd;

                solidGetter = (f, s, t) -> solidAt(t, f, s);
            }
            case ZYX -> {
                firstOrg = zOrg;
                secondOrg = yOrg;
                thirdOrg = xOrg;

                firstInc = zInc;
                secondInc = yInc;
                thirdInc = xInc;

                firstEnd = zEnd;
                secondEnd = yEnd;
                thirdEnd = xEnd;

                solidGetter = (f, s, t) -> solidAt(t, s, f);
            }
            case ZXY -> {
                firstOrg = zOrg;
                secondOrg = xOrg;
                thirdOrg = yOrg;

                firstInc = zInc;
                secondInc = xInc;
                thirdInc = yInc;

                firstEnd = zEnd;
                secondEnd = xEnd;
                thirdEnd = yEnd;

                solidGetter = (f, s, t) -> solidAt(s, t, f);
            }
            case XZY -> {
                firstOrg = xOrg;
                secondOrg = zOrg;
                thirdOrg = yOrg;

                firstInc = xInc;
                secondInc = zInc;
                thirdInc = yInc;

                firstEnd = xEnd;
                secondEnd = zEnd;
                thirdEnd = yEnd;

                solidGetter = (f, s, t) -> solidAt(f, t, s);
            }
            default -> throw new IllegalStateException("Invalid Solid.Order value " + order);
        }

        return () -> new AdvancingIterator<>() {
            private int first = firstOrg - firstInc;
            private int second = secondOrg;
            private int third = thirdOrg;

            @Override
            public boolean advance() {
                Solid candidate;
                do {
                    if(Math.abs(first += firstInc) > firstEnd) {
                        first = firstOrg;
                        if(Math.abs(second += secondInc) > secondEnd) {
                            second = secondOrg;
                            if(Math.abs(third += thirdInc) > thirdEnd) {
                                return false;
                            }
                        }
                    }

                    candidate = solidGetter.apply(first, second, third);
                }
                while (candidate == null);

                super.value = candidate;
                return true;
            }
        };
    }
}