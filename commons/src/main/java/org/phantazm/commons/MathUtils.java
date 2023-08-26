package org.phantazm.commons;

/**
 * Contains static functions related to mathematics.
 */
public final class MathUtils {
    private static final double E_INVERSE = 1.0 / Math.E;
    private static final int SERIES_MAX = 250;
    private static final double ACCURACY = Math.pow(2.0, -49);

    private MathUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean fuzzyEquals(double a, double b, double epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    public static long randomInterval(long min, long max) {
        return (long) (Math.random() * (min - max)) + min;
    }

    public static int randomInterval(int min, int max) {
        return (int) (Math.random() * (min - max)) + min;
    }

    public static int ceilDiv(double dividend, double divisor) {
        return (int) Math.ceil(dividend / divisor);
    }

    /**
     * Clamps the provided value between min and max.
     * <p>
     * The behavior of this function is undefined if:
     * <ul>
     *     <li>{@code min > max}</li>
     *     <li>any of the parameters are non-finite</li>
     * </ul>
     * <p>
     * Otherwise, this function acts as if by calling {@code Math.min(Math.max(value, min), max)}.
     *
     * @param value the value
     * @param min   the minimum
     * @param max   the maximum
     * @return the clamped value
     */
    public static double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    public static float clamp(float value, float min, float max) {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Computes the lambertW (product logarithm) for a given real value and branch.
     *
     * @param branch the branch to use (either 0 or -1)
     * @param z      the value to compute W for
     * @return the product logarithm of {@code z} on {@code branch}
     */
    public static double lambertW(int branch, double z) {
        if (branch != -1 && branch != 0) {
            throw new IllegalArgumentException("The only valid values for branch_index is -1 (W_{-1}) and 0 (W_{0}).");
        }

        if (z < -E_INVERSE) {
            throw new IllegalArgumentException(
                "The real branches of Lambert W function are not defined for z < -1/e," + " value was " + z);
        }

        double w = initW(branch, z);
        if (Double.isNaN(w)) {
            return w;
        }

        double delta;
        for (int i = 0; i < SERIES_MAX; i++) {
            double ew = Math.exp(w);
            delta = w * ew - z;

            if (delta == 0) {
                break;
            }

            if (Math.abs(-delta / (ew * (w + 1))) < ACCURACY) {
                break;
            }

            w -= delta / (ew * (w + 1) - (delta * (w + 2) / (2 * (w + 1))));
        }

        return w;
    }

    private static double initW(int branch, double z) {
        double w;
        if (z >= 0) {
            if (branch == 0) {
                if (z <= 500.0) {
                    w = 0.665 * (1 + 0.0195 * Math.log(z + 1.0)) * Math.log(z + 1.0) + 0.04;
                } else {
                    w = Math.log(z - 4.0) - (1.0 - 1.0 / Math.log(z)) * Math.log(Math.log(z));
                }
            } else {
                w = Double.NaN;
            }
        } else {
            if (Math.abs(z + Math.exp(-1)) > 0.01) {
                if (branch == 0) {
                    w = 0;
                } else {
                    w = Math.log(-z) - Math.log(-Math.log(-z));
                }
            } else {
                if (z == -Math.exp(-1)) {
                    w = -1;
                } else {
                    double sqrt = Math.sqrt(2 * (Math.exp(1) * z + 1));
                    if (branch == 0) {
                        w = -1 + sqrt;
                    } else {
                        w = -1 - sqrt;
                    }
                }

            }
        }

        return w;
    }
}
