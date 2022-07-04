package com.github.phantazmnetwork.commons;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class InterpolationUtils {
    private InterpolationUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>Uses a DDA (Digital Differential Analyzer) interpolation algorithm to iterate every Vec3I block position from
     * the starting to the ending vector.</p>
     *
     * <p>The vectors supplied to the consumer are actually the same, thread-local vector, mutated every iteration. If
     * it is necessary to store the results of this iteration, new (mutable or immutable) instances must be created
     * using {@link Vec3I#mutableCopy()} or {@link Vec3I#immutable()}.</p>
     *
     * <p>This method is suitable for performing ray-intersection checks. As soon as the given Predicate returns true,
     * or the ending vector is reached, whichever comes first, iteration will stop.</p>
     * @param start the starting vector
     * @param end the ending vector
     * @param action the predicate which accepts Vec3I instances
     */
    public static void interpolateLine(@NotNull Vec3D start, @NotNull Vec3D end,
                                       @NotNull Predicate<? super Vec3I> action) {
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

        int px = 0;
        int py = 0;
        int pz = 0;

        boolean hasPrevious = false;

        Vec3I local = Vec3I.threadLocal();
        for(int i = 0; i <= steps; i++) {
            int bx = (int) Math.floor(x);
            int by = (int) Math.floor(y);
            int bz = (int) Math.floor(z);

            if(hasPrevious) {
                boolean cx = bx != px;
                boolean cy = by != py;
                boolean cz = bz != pz;

                if(cx && cy) {
                    local.set(px, by, bz);
                    if(action.test(local)) {
                        break;
                    }
                }

                if(cy && cz) {
                    local.set(bx, py, bz);
                    if(action.test(local)) {
                        break;
                    }
                }

                if(cz && cx) {
                    local.set(bx, by, pz);
                    if(action.test(local)) {
                        break;
                    }
                }
            }

            local.set(px = bx, py = by, pz = bz);
            if(action.test(local)) {
                break;
            }

            hasPrevious = true;

            x += xi;
            y += yi;
            z += zi;
        }
    }
}