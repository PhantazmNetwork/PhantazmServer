package com.github.phantazmnetwork.neuron.world;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.commons.vector.VectorConstants;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.DoubleFunction;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

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

    public SpatialCollider(@NotNull Space space) {
        this.space = Objects.requireNonNull(space, "space");
    }

    private double collisionCheck(double oX, double oY, double oZ, double vX, double vY, double vZ, double dX,
                                  double dY, double dZ, double initialBest, ValueFunction valueFunction,
                                  DoubleBiPredicate betterThan, Predicate<Solid> bestThisLayer) {
        //bounding box of collision which will be directionally expanded
        //this is necessary to obtain an iterable of candidate solids to collision check
        double eoX = oX;
        double eoY = oY;
        double eoZ = oZ;

        /*
        perform the actual directional expansion. to do this, first we orient the directional (expansion) vector d
        <dX, dY, dZ> such that it is in the same quadrant as the original vector <vX, vY, vZ> v by ensuring it has the
        same sign as d

        we do this because it preserves the directional information of d in the final expanded bounding box. this
        information is critical to ensure performant iteration of solidsOverlapping

        (incidentally, this fast, branchless directional expansion algorithm is why origin-vector form is used for
        Collider impls)
         */
        double svX = Math.copySign(vX, dX);
        double svY = Math.copySign(vY, dY);
        double svZ = Math.copySign(vZ, dZ);

        //now that v and d have the same components, add them together to compute the final expansion vector
        double evX = svX + dX;
        double evY = svY + dY;
        double evZ = svZ + dZ;

        //if we actually changed any components of v, we'll need to move the origin to compensate for the "shift" that
        //this would cause; thus making for a proper directional expansion
        if(svX != vX) {
            eoX -= svX;
        }

        if(svY != vY) {
            eoY -= svY;
        }

        if(svZ != vZ) {
            eoZ -= svZ;
        }

        //y-last iteration to ensure we can implement some fast-exit strategies
        SolidIterator overlapping = space.solidsOverlapping(eoX, eoY, eoZ, evX, evY, evZ, Space.Order.XZY)
                .solidIterator();
        double best = initialBest;
        if(overlapping.hasNext()) {
            //make sure widths are positive
            double xW = Math.abs(vX);
            double yW = Math.abs(vY);
            double zW = Math.abs(vZ);

            //used as inputs to checkAxis calls
            double adjustedYZ = (Math.max(yW, zW) * (Math.abs(dY) + Math.abs(dZ))) / 2;
            double adjustedXZ = (Math.max(xW, zW) * (Math.abs(dX) + Math.abs(dZ))) / 2;
            double adjustedXY = (Math.max(xW, yW) * (Math.abs(dX) + Math.abs(dY))) / 2;

            double centerX = oX + (xW / 2);
            double centerY = oY + (yW / 2);
            double centerZ = oZ + (zW / 2);

            //convert origin-vector form to min-max, for easier overlaps checking
            double onX = oX + vX;
            double onY = oY + vY;
            double onZ = oZ + vZ;

            double aMinX = Math.min(oX, onX) + VectorConstants.EPSILON;
            double aMinY = Math.min(oY, onY) + VectorConstants.EPSILON;
            double aMinZ = Math.min(oZ, onZ) + VectorConstants.EPSILON;

            double aMaxX = Math.max(oX, onX) - VectorConstants.EPSILON;
            double aMaxY = Math.max(oY, onY) - VectorConstants.EPSILON;
            double aMaxZ = Math.max(oZ, onZ) - VectorConstants.EPSILON;

            Space.Order.IterationVariables variables = overlapping.getVariables();
            int yEnd = variables.getThirdEnd() - variables.getThirdIncrement();

            do {
                Solid candidate = overlapping.next();

                Vec3F candidateMin = candidate.getMin();
                Vec3F candidateMax = candidate.getMax();

                double cMinX = overlapping.getFirst() + candidateMin.getX();
                double cMinY = overlapping.getThird() + candidateMin.getY();
                double cMinZ = overlapping.getSecond() + candidateMin.getZ();

                double cMaxX = overlapping.getFirst() + candidateMax.getX();
                double cMaxY = overlapping.getThird() + candidateMax.getY();
                double cMaxZ = overlapping.getSecond() + candidateMax.getZ();

                //only check solids not overlapping with the original bounds
                if(!overlaps(aMinX, aMinY, aMinZ, aMaxX, aMaxY, aMaxZ, cMinX, cMinY, cMinZ, cMaxX, cMaxY, cMaxZ)) {
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
                                int newY = (int) Math.floor(value);
                                if(newY == yEnd) {
                                    return value;
                                }

                                overlapping.setPointer(variables.getFirstOrigin(), variables.getThirdOrigin(), newY);
                            }

                            best = value;
                        }
                    }
                }
            }
            while (overlapping.hasNext());
        }

        return best;
    }

    @Override
    public double highestCollisionAlong(double oX, double oY, double oZ, double vX, double vY, double vZ, double dX,
                                        double dY, double dZ) {
        return collisionCheck(oX, oY, oZ, vX, vY, vZ, dX, dY, dZ, Double.NEGATIVE_INFINITY, (solid, y) -> y + solid
                .getMax().getY(), (value, best) -> value > best, solid -> solid.getMax().getY() == 1.0F);
    }

    @Override
    public double lowestCollisionAlong(double oX, double oY, double oZ, double vX, double vY, double vZ, double dX,
                                       double dY, double dZ) {
        return collisionCheck(oX, oY, oZ, vX, vY, vZ, dX, dY, dZ, Double.POSITIVE_INFINITY, (solid, y) -> y + solid
                .getMin().getY(), (value, best) -> value < best, solid -> solid.getMin().getY() == 0.0F);
    }

    @Override
    public double heightAt(int x, int y, int z) {
        Solid solid = space.solidAt(x, y, z);
        return solid == null ? y : y + solid.getMax().getY();
    }

    /*
    checks for collisions when travelling along a certain axis. as checkPlanes expects the min and max vectors to be
    ordered in a certain way, performs a simple check on the 'slope' of the inequalities used for representing the
    moving bounding box (see checkPlanes)
    */
    private static boolean checkAxis(double size, double dA, double dB, double minA, double minB, double maxA,
                                     double maxB) {
        if(dA == 0 && dB == 0) {
            return true;
        }

        return dA * dB <= 0 ? checkPlanes(size, dA, dB, minA, minB, maxA, maxB) : checkPlanes(size, dA, dB, maxA, minB,
                minA, maxB);
    }

    /*
    this simple algorithm determines if a given bounds, denoted by a pair of 2d points, intersects the path traced by
    a bounding box moving in the direction denoted by the vector <dirX, dirZ>. the width of the bounding box is given
    by adjustedWidth, whose value must be precalculated as follows:

    (width * (Math.abs(dirX) + Math.abs(dirZ))) / 2

    the function works by testing points (min, max) against a pair of inequalities:

    First: (z * dirX) - (x * dirZ) < w
    Second: (z * dirX) - (x * dirZ) > -w

    the function follows the truth table shown below. a question mark denotes "don't care"

    minInFirst   |   minInSecond   |   maxInFirst   |   maxInSecond   |   collides
    0                1                 0                ?                 0
    0                1                 1                ?                 1
    1                0                 ?                0                 0
    1                0                 ?                1                 1  //same as #2 but inverted
    1                1                 ?                ?                 1

    some combinations of values are not possible given valid inputs, and thus they are not present in the truth table
    and are not tested for either. for example, a point that satisfies neither of the inequalities is not possible for
    a valid adjustedWidth parameter.

    more specifically, regarding invalid input, all double parameters must be finite, and adjustedWidth must be greater
    than 0. dirX and dirZ may be any pair of integers, including negative numbers, but they cannot both be zero (one
    may be zero if the other is non-zero). minX, minZ, maxX, and maxZ must be finite and have an additional special
    consideration that a vector drawn between them must NOT belong to the same or opposite quadrant as the direction
    vector
    */
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

    private static boolean overlaps(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                                    double minX2, double minY2, double minZ2, double maxX2, double maxY2,
                                    double maxZ2) {
        return minX < maxX2 && minY < maxY2 && minZ < maxZ2 && maxX >= minX2 && maxY >= minY2 && maxZ >= minZ2;
    }
}