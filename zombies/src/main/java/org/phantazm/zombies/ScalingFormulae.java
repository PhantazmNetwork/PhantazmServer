package org.phantazm.zombies;


public class ScalingFormulae {
    public static double computeMultiplier(boolean returnZeroOutsideRange, double percentage, boolean largerNumberIsStart,
        double largerNumber, double smallerNumber, double startMultiplier, double endMultiplier) {
        if (returnZeroOutsideRange) {
            if (percentage > largerNumber || percentage < smallerNumber) {
                return 0.0;
            }
        } else {
            if (largerNumberIsStart) {
                if (percentage > largerNumber) {
                    return startMultiplier;
                }
                if (percentage < smallerNumber) {
                    return endMultiplier;
                }
            } else {
                if (percentage > largerNumber) {
                    return endMultiplier;
                }
                if (percentage < smallerNumber) {
                    return startMultiplier;
                }
            }
        }

        double range = largerNumber - smallerNumber;
        return largerNumberIsStart ? ((largerNumber - percentage) / range) * (endMultiplier - startMultiplier) + startMultiplier :
            ((range - (largerNumber - percentage)) / range) * (endMultiplier - startMultiplier) + startMultiplier;
    }
}
