package com.github.phantazmnetwork.commons;

public final class MathUtils {
    public static final double E_INVERSE = 1.0 / Math.E;

    private static final int SERIES_MAX = 250;
    private static final double ACCURACY = Math.pow(2.0, -49);

    private MathUtils() {
        throw new UnsupportedOperationException();
    }

    public static double lambertW(int branch, double z) {
        if(branch != -1 && branch != 0) {
            throw new IllegalArgumentException("The only valid values for branch_index is -1 (W_{-1}) and 0 (W_{0}).");
        }

        if(z < -E_INVERSE) {
            throw new IllegalArgumentException("The real branches of Lambert W function are not defined for z < -1/e," +
                    " value was " + z);
        }

        double w = initW(branch, z);
        if(Double.isNaN(w)) {
            return w;
        }

        double delta;
        for(int i = 0; i < SERIES_MAX; i++) {
            double ew = Math.exp(w);
            delta = w * ew - z;

            if(delta == 0) {
                break;
            }

            if(Math.abs(-delta / (ew * (w + 1))) < ACCURACY) {
                break;
            }

            w -= delta / (ew * (w + 1) - (delta * (w + 2) / (2 * (w + 1))));
        }

        return w;
    }

    private static double initW(int branch, double z) {
        double w;
        if(z >= 0) {
            if(branch == 0) {
                if(z <= 500.0) {
                    w = 0.665 * (1 + 0.0195 * Math.log(z + 1.0)) * Math.log(z + 1.0) + 0.04;
                }
                else {
                    w  = Math.log(z - 4.0) - (1.0 - 1.0 / Math.log(z)) * Math.log(Math.log(z));
                }
            }
            else {
                w = Double.NaN;
            }
        }
        else {
            if(Math.abs(z + Math.exp(-1)) >  0.01) {
                if(branch == 0) {
                    w = 0;
                }
                else {
                    w = Math.log(-z) - Math.log(-Math.log(-z));
                }
            }
            else {
                if(z == -Math.exp(-1)) {
                    w = -1;
                }
                else {
                    double sqrt = Math.sqrt(2 * (Math.exp(1) * z + 1));
                    if(branch == 0) {
                        w = -1 + sqrt;
                    }
                    else {
                        w = -1 - sqrt;
                    }
                }

            }
        }

        return w;
    }
}
