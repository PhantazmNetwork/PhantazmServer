package org.phantazm.core.tracker;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface BoundedTracker<T extends Bounded> {
    @NotNull Optional<T> closestInRangeToCenter(@NotNull Point origin, double distance);

    @NotNull Optional<T> closestInRangeToBounds(@NotNull Point origin, double width, double height, double depth,
            double distance);

    default @NotNull Optional<T> closestInRangeToBounds(@NotNull Point origin, double width, double height,
            double distance) {
        return closestInRangeToBounds(origin, width, height, width, distance);
    }

    void forEachInRangeToCenter(@NotNull Point origin, double distance, @NotNull Consumer<? super T> consumer);

    @NotNull Optional<T> atPoint(int x, int y, int z);

    default @NotNull Optional<T> atPoint(@NotNull Point point) {
        return atPoint(point.blockX(), point.blockY(), point.blockZ());
    }

    @NotNull @Unmodifiable List<T> items();

    static <T extends Bounded> @NotNull BoundedTracker<T> tracker(@NotNull Collection<T> collection) {
        return new BoundedTrackerImpl<>(collection);
    }
}
