package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.neuron.vector.Vec3D;

public interface Bounds extends Vec3D {
    double getMaxX();

    double getMaxY();

    double getMaxZ();

    Bounds EMPTY = new Bounds() {
        @Override
        public double getMaxX() {
            return 0;
        }

        @Override
        public double getMaxY() {
            return 0;
        }

        @Override
        public double getMaxZ() {
            return 0;
        }

        @Override
        public double getX() {
            return 0;
        }

        @Override
        public double getY() {
            return 0;
        }

        @Override
        public double getZ() {
            return 0;
        }
    };
}
