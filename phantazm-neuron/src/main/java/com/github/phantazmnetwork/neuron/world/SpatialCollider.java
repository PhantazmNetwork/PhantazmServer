package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3F;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
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
                                  DoubleBiPredicate betterThan, Predicate<Solid> bestThisLayer, boolean highest) {
        //bounding box of collision which will be directionally expanded
        //this is necessary to obtain an iterable of candidate solids to collision check
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
        SolidIterator overlapping = space.solidsOverlapping(eoX, eoY, eoZ, evX, evY, evZ, Space.Order.XZY).iterator();
        double best = initialBest;
        if(overlapping.hasNext()) {
            //used as inputs to checkAxis calls
            double adjustedYZ = (Math.max(height, depth) * (Math.abs(dY) + Math.abs(dZ))) / 2;
            double adjustedXZ = (Math.max(width, depth) * (Math.abs(dX) + Math.abs(dZ))) / 2;
            double adjustedXY = (Math.max(width, height) * (Math.abs(dX) + Math.abs(dY))) / 2;

            double centerX = x + (width / 2);
            double centerY = y + (height / 2);
            double centerZ = z + (depth / 2);

            Space.Order.IterationVariables variables = overlapping.getVariables();
            do {
                Solid candidate = overlapping.next();
                Vec3F candidateMin = candidate.getMin();

                double relX = x - candidateMin.getX();
                double relY = y - candidateMin.getY();
                double relZ = z - candidateMin.getZ();

                for(Solid component : candidate.getComponents()) {
                    //stop checking this solid if any of its sub-solids overlap original bounds
                    if(component.overlaps(relX, relY, relZ, width, height, depth)) {
                        break;
                    }

                    Vec3F componentMin = component.getMin();
                    Vec3F componentMax = component.getMax();

                    double cMinX = overlapping.getFirst() + componentMin.getX();
                    double cMinY = overlapping.getThird() + componentMin.getY();
                    double cMinZ = overlapping.getSecond() + componentMin.getZ();

                    double cMaxX = overlapping.getFirst() + componentMax.getX();
                    double cMaxY = overlapping.getThird() + componentMax.getY();
                    double cMaxZ = overlapping.getSecond() + componentMax.getZ();

                    double minX = cMinX - centerX;
                    double minY = cMinY - centerY;
                    double minZ = cMinZ - centerZ;

                    double maxX = cMaxX - centerX;
                    double maxY = cMaxY - centerY;
                    double maxZ = cMaxZ - centerZ;

                    if(checkAxis(adjustedXZ, dX, dZ, minX, minZ, maxX, maxZ) && checkAxis(adjustedXY, dX, dY, minX,
                            minY, maxX, maxY) && checkAxis(adjustedYZ, dZ, dY, minZ, minY, maxZ, maxY)) {
                        double value = valueFunction.apply(candidate, overlapping.getThird());

                        //collision found
                        if(betterThan.test(value, best)) {
                            //we have found the highest/lowest possible solid this layer
                            if(bestThisLayer.test(candidate)) {
                                int nextY = overlapping.getThird() + variables.getThirdIncrement();
                                if(nextY == variables.getThirdEnd()) {
                                    return value;
                                }

                                //fast exit if we are moving down or up
                                if(highest && dY < 0) {
                                    return value;
                                }
                                else if(!highest && dY > 0) {
                                    return value;
                                }

                                overlapping.setPointer(variables.getFirstOrigin(), variables.getSecondOrigin(), nextY);
                            }

                            best = value;
                        }

                        //don't bother with other components
                        break;
                    }
                }
            }
            while (overlapping.hasNext());
        }

        return best;
    }

    @Override
    public double highestCollisionAlong(double x, double y, double z, double width, double height, double depth,
                                        double dX, double dY, double dZ) {
        return collisionCheck(x, y, z, width, height, depth, dX, dY, dZ, Double.NEGATIVE_INFINITY, (solid, val) -> val +
                solid.getMax().getY(), (value, best) -> value > best, solid -> solid.getMax().getY() == 1.0F,
                true);
    }

    @Override
    public double lowestCollisionAlong(double x, double y, double z, double width, double height, double depth,
                                       double dX, double dY, double dZ) {
        return collisionCheck(x, y, z, width, height, depth, dX, dY, dZ, Double.POSITIVE_INFINITY, (solid, val) -> val +
                solid.getMin().getY(), (value, best) -> value < best, solid -> solid.getMin().getY() == 0.0F,
                false);
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