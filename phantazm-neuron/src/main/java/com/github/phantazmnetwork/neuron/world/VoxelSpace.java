package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.iterator.AdvancingIterator;
import org.jetbrains.annotations.NotNull;

public abstract class VoxelSpace implements Space {
    @Override
    public @NotNull Iterable<? extends Solid> solidsOverlapping(double oX, double oY, double oZ, double vX, double vY,
                                                                double vZ, @NotNull Order order) {
        int xOrg = (int) Math.floor(oX);
        int yOrg = (int) Math.floor(oY);
        int zOrg = (int) Math.floor(oZ);

        int xInc = (int) Math.signum(vX);
        int yInc = (int) Math.signum(vY);
        int zInc = (int) Math.signum(vZ);

        int xEnd = Math.abs((int) Math.floor(oX + vX)) + xInc;
        int yEnd = Math.abs((int) Math.floor(oY + vY)) + yInc;
        int zEnd = Math.abs((int) Math.floor(oZ + vZ)) + zInc;

        Order.IterationVariables variables = order.computeVariables(xOrg, yOrg, zOrg, xInc, yInc, zInc, xEnd, yEnd,
                zEnd);

        return () -> new AdvancingIterator<>() {
            private int first = variables.getFirstOrigin() - variables.getFirstIncrement();
            private int second = variables.getSecondOrigin();
            private int third = variables.getThirdOrigin();

            @Override
            public boolean advance() {
                Solid candidate;
                do {
                    if((first += variables.getFirstIncrement()) == variables.getFirstEnd()) {
                        first = variables.getFirstOrigin();
                        if((second += variables.getSecondIncrement()) == variables.getSecondEnd()) {
                            second = variables.getSecondOrigin();
                            if((third += variables.getThirdIncrement()) == variables.getThirdEnd()) {
                                return false;
                            }
                        }
                    }

                    candidate = order.getAccessor().getSolid(first, second, third, VoxelSpace.this);
                }
                while (candidate == null);

                super.value = candidate;
                return true;
            }
        };
    }
}