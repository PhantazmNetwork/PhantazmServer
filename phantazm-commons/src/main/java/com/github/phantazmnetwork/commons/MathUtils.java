package com.github.phantazmnetwork.commons;

public final class MathUtils {
    public static final double E_INVERSE = 1.0 / Math.E;

    private static final int SERIES_MAX = 250;
    private static final double ACCURACY = Math.pow(2.0, -49);

    private MathUtils() {
        throw new UnsupportedOperationException();
    }

    private static final double[] branchCoefficients;

    static {
        branchCoefficients = new double[8];
        branchCoefficients[0] = 1.0;
        branchCoefficients[1] = 1.0;
        for (int i = 2; i < branchCoefficients.length; i++) {
            double t = 0.0;
            for (int j = 2; j < i; j++) {
                t += j * branchCoefficients[j] * branchCoefficients[i + 1 - j];
            }
            branchCoefficients[i] = (branchCoefficients[i - 1] - t) / (i + 1);
        }
    }

    /**
     * <p>Computes the lambert-W (product logarithm) for the specified double value. Based on code from
     * <a href="https://github.com/dcwuser/metanumerics">this GitHub repository.</a>.</p>
     *
     * <p>The input may not be smaller than {@link MathUtils#E_INVERSE}, or an IllegalArgumentException will be thrown.
     * </p>
     * @param x the input value
     * @return the result of applying the lambert W function to the provided argument
     */
    public static double lambertW(double x) {
        if (x < -E_INVERSE) {
            throw new IllegalArgumentException("The Lambert W function is undefined for " + x);
        }

        double w;
        if(x < -E_INVERSE / 2.0) {
            double t = Math.sqrt(-2.0 * (1.0 + Math.log(-x)));
            w = -1.0 + t;
            double tk = t;
            for (int i = 2; i < branchCoefficients.length; i++) {
                double wOld = w;
                tk *= -t;
                w += branchCoefficients[i] * tk;
                if (w == wOld) {
                    return w;
                }
            }
        }
        else if(x < E_INVERSE) {
            w = lambertSeriesZero(x);
        }
        else if(x > Math.E) {
            w = lambertSeriesLarge(x);
        }
        else {
            w = 0.5;
        }

        return lambertHalley(x, w);
    }

    private static double lambertSeriesZero(double x) {
        return x * (1.0 - x + (3.0 / 2.0) * x * x - (8.0 / 3.0) * x * x * x + (125.0 / 24.0) * x * x * x * x);
    }

    private static double lambertSeriesLarge(double x) {
        double l1 = Math.log(x);
        double l2 = Math.log(l1);
        return l1 - l2 + l2 / l1 + l2 * (l2 - 2.0) / (2.0 * l1 * l1) + l2 * (2.0 * l2 * l2 - 9.0 * l2 + 6.0) / (6.0 * l1
                * l1 * l1);
    }

    private static double lambertHalley (double x, double w) {
        for (int i = 0; i < SERIES_MAX; i++) {
            double e = Math.exp(w);
            double f = e * w - x;
            double dW = f / ((w + 1.0) * e - ((w + 2.0) / (w + 1.0)) * f / 2.0);
            w = w - dW;
            if (Math.abs(dW) <= ACCURACY * Math.abs(w)) {
                return (w);
            }
        }

        //should never happen
        throw new ArithmeticException("Series does not converge");
    }
}
