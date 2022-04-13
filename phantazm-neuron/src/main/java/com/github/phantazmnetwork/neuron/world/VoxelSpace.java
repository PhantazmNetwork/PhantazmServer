package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.pipe.Pipe;
import org.jetbrains.annotations.NotNull;

/**
 * A standard extension of {@link Space} which provides an implementation of
 * {@link Space#solidsOverlapping(double, double, double, double, double, double, Order)} based off of
 * {@link Space#solidAt(int, int, int)}.
 */
public abstract class VoxelSpace implements Space {
    private class BasicSolidPipe extends Pipe.Advancing<Solid> implements SolidPipe {
        private final Order order;
        private final Order.IterationVariables variables;

        private int first;
        private int second;
        private int third;

        private BasicSolidPipe(@NotNull Order order, @NotNull Order.IterationVariables variables) {
            this.order = order;
            this.variables = variables;
            this.first = variables.getFirstOrigin() - variables.getFirstIncrement();
            this.second = variables.getSecondOrigin();
            this.third = variables.getThirdOrigin();
        }

        @Override
        protected boolean advance() {
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

                candidate = order.getSpaceAccessor().getSolid(first, second, third, VoxelSpace.this);
            }
            while (candidate == null);

            super.value = candidate;
            return true;
        }

        @Override
        public void setPointer(int first, int second, int third) {
            this.first = first - variables.getFirstIncrement();
            this.second = second;
            this.third = third;
        }

        @Override
        public @NotNull Space.Order getOrder() {
            return order;
        }

        @Override
        public @NotNull Order.IterationVariables getVariables() {
            return variables;
        }

        @Override
        public int getFirst() {
            return first;
        }

        @Override
        public int getSecond() {
            return second;
        }

        @Override
        public int getThird() {
            return third;
        }
    }

    @Override
    public @NotNull SolidSource solidsOverlapping(double oX, double oY, double oZ, double vX, double vY, double vZ,
                                                  @NotNull Order order) {
        int xOrg = (int) Math.floor(oX);
        int yOrg = (int) Math.floor(oY);
        int zOrg = (int) Math.floor(oZ);

        int xInc = (int) Math.signum(vX);
        int yInc = (int) Math.signum(vY);
        int zInc = (int) Math.signum(vZ);

        int xEnd = ((int) Math.floor(oX + vX)) + xInc;
        int yEnd = ((int) Math.floor(oY + vY)) + yInc;
        int zEnd = ((int) Math.floor(oZ + vZ)) + zInc;

        Order.IterationVariables variables = order.getVariablesSupplier().make(xOrg, yOrg, zOrg, xInc, yInc, zInc, xEnd,
                yEnd, zEnd);
        return () -> new BasicSolidPipe(order, variables);
    }
}