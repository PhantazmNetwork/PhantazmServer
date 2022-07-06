package com.github.phantazmnetwork.commons;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

public final class InterpolationUtils {
    private InterpolationUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>Uses a DDA (Digital Differential Analyzer) interpolation algorithm to iterate every Vec3I block position from
     * the starting to the ending vector. All blocks iterated form the set of all full blocks that intersect with the
     * line formed from the starting vector to the ending vector.</p>
     *
     * <p>The vectors supplied to the consumer are actually the same, thread-local vector, mutated every iteration. If
     * it is necessary to store the results of this iteration, new (mutable or immutable) instances must be created
     * using {@link Vec3I#mutableCopy()} or {@link Vec3I#immutable()}.</p>
     *
     * <p>This method is suitable for performing ray-intersection checks. As soon as the given Predicate returns true,
     * or the ending vector is reached, whichever comes first, iteration will stop.</p>
     * @param start the starting vector
     * @param end the ending vector
     * @param action the predicate which accepts Vec3I instances; iteration stops when this returns true
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

        double xym = dy / dx;
        double yzm = dz / dy;
        double zxm = dx / dz;

        Vec3I local = Vec3I.threadLocal();
        for(int i = 0; i <= steps; i++) {
            int nx = (int) Math.floor(x);
            int ny = (int) Math.floor(y);
            int nz = (int) Math.floor(z);

            if(hasPrevious) {
                boolean cx = nx != px;
                boolean cy = ny != py;
                boolean cz = nz != pz;

                if(cx && cy && cz) {
                    //if all axes change at once, there are two blocks to check this iteration
                    //it's necessary to use a painfully complex algorithm here

                    boolean lxy = cmp(xym, nx, x, y, ny);
                    boolean lyz = cmp(yzm, ny, y, z, nz);
                    boolean lzx = cmp(zxm, nz, z, x, nx);

                    //(a, b, c) = first block to check
                    //(d, e, f) = second block to check
                    int a;
                    int b;
                    int c;

                    int d;
                    int e;
                    int f;

                    if(lxy) {
                        d = nx;

                        if(lyz) {
                            a = nx;
                            b = py;
                            c = pz;

                            e = ny;
                            f = pz;
                        }
                        else {
                            e = py;
                            f = nz;

                            b = py;

                            if(lzx) {
                                a = px;
                                c = nz;
                            }
                            else {
                                a = nx;
                                c = pz;
                            }
                        }
                    }
                    else {
                        a = px;

                        if(lyz) {
                            b = ny;
                            c = pz;

                            e = ny;

                            if(lzx) {
                                d = px;
                                f = nz;
                            }
                            else {
                                d = nx;
                                f = pz;
                            }
                        }
                        else {
                            d = px;
                            e = ny;
                            f = nz;

                            b = py;
                            c = nz;
                        }
                    }

                    //test both blocks
                    if(action.test(local.set(a, b, c)) || action.test(local.set(d, e, f))) {
                        break;
                    }
                }
                else {
                    //will sample at most 1 block here
                    if(check(action, local, cx, cy, xym, nx, x, y, ny, nx, py, pz, px, ny, pz)) {
                        break;
                    }

                    if(check(action, local, cy, cz, yzm, ny, y, z, nz, px, ny, pz, px, py, nz)) {
                        break;
                    }

                    if(check(action, local, cz, cx, zxm, nz, z, x, nx, px, py, nz, nx, py, pz)) {
                        break;
                    }
                }
            }

            if(action.test(local.set(px = nx, py = ny, pz = nz))) { //test the next block
                break;
            }

            hasPrevious = true;

            x += xi;
            y += yi;
            z += zi;
        }
    }

    private static boolean check(Predicate<? super Vec3I> p, Vec3I l, boolean ch, boolean cv,
                                 double m, int nh, double h, double v, int nv,
                                 int a, int b, int c,
                                 int d, int e, int f) {
        if(ch && cv) {
            if(cmp(m, nh, h, v, nv)) {
                return p.test(l.set(a, b, c));
            }

            return p.test(l.set(d, e, f));
        }

        return false;
    }

    private static boolean cmp(double m, int nh, double h, double v, int nv) {
        return offset(m, nh, h, v) <= Math.abs(nv);
    }

    private static double offset(double m, int nh, double h, double v) {
        return Math.abs(m * (nh - h) + v);
    }
}