package com.github.phantazmnetwork.neuron.world;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

@SuppressWarnings("ClassCanBeRecord")
public class SpatialCollider implements Collider {
    @FunctionalInterface
    private interface DoubleBiPredicate {
        boolean test(double a, double b);
    }

    private final Space space;

    public SpatialCollider(@NotNull Space space) {
        this.space = Objects.requireNonNull(space, "space");
    }

    @Override
    public double highestCollisionAlong(double oX, double oY, double oZ, double vX, double vY, double vZ, double dX,
                                        double dY, double dZ) {
        return collidesMovingAlong(oX, oY, oZ, vX, vY, vZ, dX, dY, dZ, solid -> solid.getY() + solid.originY() + solid
                .vectorY(), (a, b) -> a > b, Double.NEGATIVE_INFINITY);
    }

    @Override
    public double lowestCollisionAlong(double oX, double oY, double oZ, double vX, double vY, double vZ, double dX,
                                       double dY, double dZ) {
        return collidesMovingAlong(oX, oY, oZ, vX, vY, vZ, dX, dY, dZ, solid -> solid.getY() + solid.originY(),
                (a, b) -> a < b, Double.POSITIVE_INFINITY);
    }

    @Override
    public double heightAt(int x, int y, int z) {
        Solid solid = space.solidAt(x, y, z);
        return solid == null ? y : solid.originY() + solid.vectorY();
    }

    private double collidesMovingAlong(double oX, double oY, double oZ, double vX, double vY, double vZ, double dX,
                                       double dY, double dZ, ToDoubleFunction<Solid> valueFunction,
                                       DoubleBiPredicate valuePredicate, double initialValue) {
        double eoX = oX;
        double eoY = oY;
        double eoZ = oZ;

        double evX = vX;
        double evY = vY;
        double evZ = vZ;

        //perform a directional expansion in direction (dX, dY, dZ)
        if(dX < 0) {
            eoX += dX;
            evX -= dX;
        }
        else {
            evX += dX;
        }

        if(dY < 0) {
            eoY += dY;
            evY -= dY;
        }
        else {
            evY += dY;
        }

        if(dZ < 0) {
            eoZ += dZ;
            evZ -= dZ;
        }
        else {
            evZ += dZ;
        }

        Iterator<? extends Solid> overlapping = space.solidsOverlapping(eoX, eoY, eoZ, evX, evY, evZ, dX, dY, dZ)
                .iterator();
        double best = initialValue;
        if(overlapping.hasNext()) {
            double adjustedYZ = (Math.max(vY, vZ) * (Math.abs(dY) + Math.abs(dZ))) / 2;
            double adjustedXZ = (Math.max(vX, vZ) * (Math.abs(dX) + Math.abs(dZ))) / 2;
            double adjustedXY = (Math.max(vX, vY) * (Math.abs(dX) + Math.abs(dY))) / 2;

            double centerX = oX + (vX / 2);
            double centerY = oY + (vY / 2);
            double centerZ = oZ + (vZ / 2);

            do {
                Solid candidate = overlapping.next();

                double coX = candidate.getX() + candidate.originX();
                double coY = candidate.getY() + candidate.originY();
                double coZ = candidate.getZ() + candidate.originZ();

                float cvX = candidate.vectorX();
                float cvY = candidate.vectorY();
                float cvZ = candidate.vectorZ();

                //only check solids not overlapping with the original bounds
                if(!Solid.overlaps(coX, coY, coZ, cvX, cvY, cvZ, oX, oY, oZ, vX, vY, vZ)) {
                    double minX = coX - centerX;
                    double minY = coY - centerY;
                    double minZ = coZ - centerZ;

                    double maxX = minX + cvX;
                    double maxY = minY + cvY;
                    double maxZ = minZ + cvZ;

                    if(checkAxis(adjustedXZ, dX, dZ, minX, minZ, maxX, maxZ) && checkAxis(adjustedXY, dX, dY, minX,
                            minY, maxX, maxY) && checkAxis(adjustedYZ, dZ, dY, minZ, minY, maxZ, maxY)) {
                        //collision found
                        double value = valueFunction.applyAsDouble(candidate);
                        if(valuePredicate.test(value, best)) {
                            best = value;
                        }
                    }
                }
            }
            while (overlapping.hasNext());
        }

        return best;
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

    the function follows the truth table shown below. a question mark denotes "don't cares"

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
}