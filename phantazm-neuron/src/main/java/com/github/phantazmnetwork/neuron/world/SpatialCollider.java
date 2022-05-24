package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3F;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.DoublePredicate;
import java.util.function.Predicate;

/**
 * A standard {@link Collider} implementation based off of a {@link Space} instance.
 */
@SuppressWarnings("ClassCanBeRecord")
public class SpatialCollider implements Collider {
    @FunctionalInterface
    private interface DoubleBiPredicate {
        boolean test(double a, double b);
    }

    @FunctionalInterface
    private interface ValueFunction {
        double apply(Solid solid, int y);
    }

    private final Space space;

    /**
     * Creates a new instance of SpatialCollider using the given {@link Space} implementation.
     * @param space the Space implementation to use
     * @throws NullPointerException if space is null
     */
    public SpatialCollider(@NotNull Space space) {
        this.space = Objects.requireNonNull(space, "space");
    }

    private double collisionCheck(double x, double y, double z, double width, double height, double depth, double dX,
                                  double dY, double dZ, double initialBest, ValueFunction valueFunction,
                                  DoubleBiPredicate betterThan, Predicate<Solid> bestThisLayer,
                                  DoublePredicate fastExit) {
        //bounding box of collision which will be directionally expanded this is necessary to obtain an iterable of
        //candidate solids to collision check
        double eoX = x;
        double eoY = y;
        double eoZ = z;

        /*
        perform the actual directional expansion. to do this, first we orient the directional (expansion) vector d
        <dX, dY, dZ> such that it is in the same quadrant as the original vector <vX, vY, vZ> v by ensuring it has the
        same sign as d

        we do this because it preserves the directional information of d in the final expanded bounding box. this
        information is critical to ensure performant iteration of solidsOverlapping

        (incidentally, this fast, branchless directional expansion algorithm is why origin-vector form is used for
        Collider impls)
         */
        double svX = Math.copySign(width, dX);
        double svY = Math.copySign(height, dY);
        double svZ = Math.copySign(depth, dZ);

        //now that v and d have the same components, add them together to compute the final expansion vector
        double evX = svX + dX;
        double evY = svY + dY;
        double evZ = svZ + dZ;

        //if we actually changed any components of v, we'll need to move the origin to compensate for the "shift" that
        //this would cause; thus making for a proper directional expansion
        if(svX != width) {
            eoX -= svX;
        }

        if(svY != height) {
            eoY -= svY;
        }

        if(svZ != depth) {
            eoZ -= svZ;
        }

        //y-last iteration to ensure we can implement some fast-exit strategies
        SolidPipe overlapping = space.solidsOverlapping(eoX, eoY, eoZ, evX, evY, evZ, Space.Order.XZY).iterator();
        double best = initialBest;
        if(overlapping.hasNext()) {
            double minX = Math.min(eoX, eoX + evX);
            double minY = Math.min(eoY, eoY + evY);
            double minZ = Math.min(eoZ, eoZ + evZ);

            double ewX = Math.abs(evX);
            double ewY = Math.abs(evY);
            double ewZ = Math.abs(evZ);

            double adX = Math.abs(dX);
            double adY = Math.abs(dY);
            double adZ = Math.abs(dZ);

            //used as inputs to checkAxis calls
            double adjustedXY = (height * adX + width * adY) / 2;
            double adjustedXZ = (depth * adX + width * adZ) / 2;
            double adjustedYZ = (depth * adY + height * adZ) / 2;

            double centerX = x + (width / 2);
            double centerY = y + (height / 2);
            double centerZ = z + (depth / 2);

            Space.Order.IterationVariables variables = overlapping.getVariables();
            do {
                Solid candidate = overlapping.next();
                Vec3F candidateMin = candidate.getMin();

                double moX = overlapping.getFirst() + candidateMin.getX();
                double moY = overlapping.getThird() + candidateMin.getY();
                double moZ = overlapping.getSecond() + candidateMin.getZ();

                if(!candidate.overlaps(minX - moX, minY - moY, minZ - moZ, ewX, ewY, ewZ)) {
                    //if candidate does not overlap the expanded bounds, we aren't colliding
                    continue;
                }

                if(candidate.overlaps(x - moX, y - moY, z - moZ, width, height, depth)) {
                    //if we're overlapping the original bounds, we aren't colliding
                    continue;
                }

                boolean hit = false;
                if(candidate.hasChildren()) {
                    for(Solid component : candidate.getChildren()) {
                        //stop checking this solid if any of its sub-solids overlap original bounds
                        //noinspection AssignmentUsedAsCondition
                        if(hit = checkSolid(overlapping, component, centerX, centerY, centerZ, adjustedXZ, adjustedXY,
                                adjustedYZ, dX, dY, dZ)) {
                            break;
                        }
                    }
                }
                else {
                    hit = checkSolid(overlapping, candidate, centerX, centerY, centerZ, adjustedXZ, adjustedXY,
                            adjustedYZ, dX, dY, dZ);
                }

                if(hit) {
                    double value = valueFunction.apply(candidate, overlapping.getThird());

                    //collision found
                    if(betterThan.test(value, best)) {
                        //we have found the highest/lowest possible solid this layer
                        if(bestThisLayer.test(candidate)) {
                            int nextY = overlapping.getThird() + variables.getThirdIncrement();

                            //fast exit: we already found the best possible this layer + we're on the last layer
                            if(nextY == variables.getThirdEnd()) {
                                return value;
                            }

                            //fast exit: we must have found the best possible in the entire volume
                            if(fastExit.test(dY)) {
                                return value;
                            }

                            overlapping.setPointer(variables.getFirstOrigin(), variables.getSecondOrigin(), nextY);
                        }

                        best = value;
                    }
                }
            }
            while (overlapping.hasNext());
        }

        return best;
    }

    private static boolean checkSolid(SolidPipe overlapping, Solid component, double centerX, double centerY,
                                      double centerZ, double adjustedXZ, double adjustedXY, double adjustedYZ,
                                      double dX, double dY, double dZ) {
        Vec3F componentMin = component.getMin();
        Vec3F componentMax = component.getMax();

        double minX = overlapping.getFirst() + componentMin.getX() - centerX;
        double minY = overlapping.getThird() + componentMin.getY() - centerY;
        double minZ = overlapping.getSecond() + componentMin.getZ() - centerZ;

        double maxX = overlapping.getFirst() + componentMax.getX() - centerX;
        double maxY = overlapping.getThird() + componentMax.getY() - centerY;
        double maxZ = overlapping.getSecond() + componentMax.getZ() - centerZ;

        return checkAxis(adjustedXZ, dX, dZ, minX, minZ, maxX, maxZ) && checkAxis(adjustedXY, dX, dY, minX, minY, maxX,
                maxY) && checkAxis(adjustedYZ, dZ, dY, minZ, minY, maxZ, maxY);
    }

    @Override
    public double highestCollisionAlong(double x, double y, double z, double width, double height, double depth,
                                        double dX, double dY, double dZ) {
        return collisionCheck(x, y, z, width, height, depth, dX, dY, dZ, Double.NEGATIVE_INFINITY, (solid, val) -> val +
                solid.getMax().getY(), (value, best) -> value > best, solid -> solid.getMax().getY() == 1.0F,
                val -> val < 0);
    }

    @Override
    public double lowestCollisionAlong(double x, double y, double z, double width, double height, double depth,
                                       double dX, double dY, double dZ) {
        return collisionCheck(x, y, z, width, height, depth, dX, dY, dZ, Double.POSITIVE_INFINITY, (solid, val) -> val +
                solid.getMin().getY(), (value, best) -> value < best, solid -> solid.getMin().getY() == 0.0F,
                val -> val > 0);
    }

    @Override
    public double heightAt(int x, int y, int z) {
        Solid solid = space.solidAt(x, y, z);
        return solid == null ? y : y + solid.getMax().getY();
    }

    private static boolean checkAxis(double size, double dA, double dB, double minA, double minB, double maxA,
                                     double maxB) {
        if(dA == 0 && dB == 0) {
            return true;
        }

        return dA * dB <= 0 ? checkPlanes(size, dA, dB, minA, minB, maxA, maxB) : checkPlanes(size, dA, dB, maxA, minB,
                minA, maxB);
    }

    private static boolean checkPlanes(double size, double dA, double dB, double minA, double minB, double maxA,
                                       double maxB) {
        double bMinusAMin = (minB * dA) - (minA * dB);
        if(bMinusAMin >= size) { //!minInFirst
            return (maxB * dA) - (maxA * dB) < size;  //... && maxInFirst
        }

        //we know minInFirst is true
        if(bMinusAMin > -size) { //... && minInSecond
            return true;
        }

        return (maxB * dA) - (maxA * dB) > -size; // ... && !minInSecond
    }
}