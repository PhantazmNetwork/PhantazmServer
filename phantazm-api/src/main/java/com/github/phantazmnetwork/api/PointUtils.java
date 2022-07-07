package com.github.phantazmnetwork.api;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;

public class PointUtils {

    private PointUtils() {
        throw new UnsupportedOperationException();
    }

    public static void sortPointsByDistance(@NotNull Point root, @NotNull List<? extends Point> points) {
        Object2DoubleMap<Point> distanceMap = new Object2DoubleOpenHashMap<>(points.size());
        ToDoubleFunction<Point> distanceGetter = root::distanceSquared;
        ToDoubleFunction<Point> mapGetter = point -> distanceMap.computeIfAbsent(point, distanceGetter);
        points.sort(Comparator.comparingDouble(mapGetter));
    }

}
