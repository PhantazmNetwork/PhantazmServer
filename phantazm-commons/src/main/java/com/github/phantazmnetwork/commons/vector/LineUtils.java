package com.github.phantazmnetwork.commons.vector;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public final class LineUtils {
    public static void iterateLine(@NotNull Vec3D start, @NotNull Vec3D end, @NotNull Consumer<? super Vec3I> action) {
        Objects.requireNonNull(action, "action");

        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        double dz = end.getZ() - start.getZ();

        double adx = Math.abs(dx);
        double ady = Math.abs(dy);
        double adz = Math.abs(dz);

        int steps = (int) Math.round(Math.max(Math.max(adx, ady), adz));

        double xi = dx / steps;
        double yi = dy / steps;
        double zi = dz / steps;

        double x = start.getX();
        double y = start.getY();
        double z = start.getZ();

        Vec3I local = Vec3I.threadLocal();
        for(int i = 0; i <= steps; i++) {
            local.set((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
            action.accept(local);

            x += xi;
            y += yi;
            z += zi;
        }
    }
}