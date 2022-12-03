package com.github.phantazmnetwork.neuron.node;

import com.github.steanky.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Represents a class capable of computing the <i>heuristic</i> and <i>distance</i> values for a given step. The
 * heuristic is a floating point value representing the estimated best-case distance from the current position to the
 * destination. The distance value, however, represents the distance between the current position and a neighboring
 * node.</p>
 *
 * <p>Pathfinding behavior can be subtly altered by adjusting the distance value. Although the default is simply the
 * squared straight-line distance, larger or smaller variables will cause certain nodes to be avoided or preferred
 * respectively. For example, sufficiently increasing the distance when the terrain below the neighboring node is
 * harmful will cause agents to walk around the hazard.</p>
 */
public interface Calculator {
    /**
     * A simple Calculator implementation whose heuristic and distance functions just return the squared distance
     * between the two points they are given. For typical grid-based navigation, this produces suitably natural paths.
     */
    Calculator SQUARED_DISTANCE = new Calculator() {
        @Override
        public float heuristic(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
            return (float)Vec3I.distanceSquared(fromX, fromY, fromZ, toX, toY, toZ);
        }

        @Override
        public float distance(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
            return (float)Vec3I.distanceSquared(fromX, fromY, fromZ, toX, toY, toZ);
        }
    };

    /**
     * <p>Computes the heuristic.</p>
     *
     * <p>In order to ensure pathfinding that always finds the least-cost solution, the heuristic must be
     * <i>admissible</i>. A heuristic function is admissible only if it never overestimates the distance required to
     * reach the goal from the current node. The simplest heuristic function is just the straight-line distance from the
     * current node to the destination. Since the shortest path between two points is a line, this function is
     * admissible.</p>
     *
     * <p>Another way to think about the heuristic is that it is a value that causes A* to bias itself in favor of
     * certain nodes, leading it to more quickly identity a path. A <i>perfect</i> heuristic would always correctly
     * identify the exact distance required to be traveled in order to get from a given node to the destination.</p>
     *
     * <p>When the heuristic is always 0, A* effectively operates like Dijkstra's algorithm. This is generally
     * inefficient and should be avoided, as it will not "intelligently" prioritize more promising nodes.</p>
     *
     * @param fromX current x
     * @param fromY current y
     * @param fromZ current z
     * @param toX   destination x
     * @param toY   destination y
     * @param toZ   destination z
     * @return the heuristic value, {@code h}
     */
    float heuristic(int fromX, int fromY, int fromZ, int toX, int toY, int toZ);

    /**
     * Computes the distance between the given starting node and the destination. This value typically reflects the
     * squared straight-line distance, but may be reduced or increased in order to bias the resulting path towards or
     * away from certain nodes.
     *
     * @param fromX current x
     * @param fromY current y
     * @param fromZ current z
     * @param toX   destination x
     * @param toY   destination y
     * @param toZ   destination z
     * @return the distance value
     */
    float distance(int fromX, int fromY, int fromZ, int toX, int toY, int toZ);

    /**
     * Convenience overload for {@link Calculator#heuristic(int, int, int, int, int, int)}.
     *
     * @param from the first vector
     * @param to   the second vector
     * @return the heuristic value
     */
    default float heuristic(@NotNull Vec3I from, @NotNull Vec3I to) {
        return heuristic(from.x(), from.y(), from.z(), to.x(), to.y(), to.z());
    }

    /**
     * Convenience overload for {@link Calculator#distance(int, int, int, int, int, int)}.
     *
     * @param from the first vector
     * @param to   the second vector
     * @return the distance value
     */
    default float distance(@NotNull Vec3I from, @NotNull Vec3I to) {
        return distance(from.x(), from.y(), from.z(), to.x(), to.y(), to.z());
    }
}
