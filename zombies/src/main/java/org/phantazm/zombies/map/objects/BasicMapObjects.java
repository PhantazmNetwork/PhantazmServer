package org.phantazm.zombies.map.objects;

import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.vector.Bounds3I;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.zombies.map.*;
import org.phantazm.zombies.map.handler.BasicWindowHandler;
import org.phantazm.zombies.map.shop.Shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class BasicMapObjects implements MapObjects {
    private static class BasicWindowTracker implements WindowTracker {
        private final List<Window> windows;
        private final Long2ObjectMap<Window[]> windowsByChunk;

        private BasicWindowTracker(List<Window> windows) {
            this.windows = windows;
            this.windowsByChunk = computeWindowsByChunk(windows);
        }

        private Long2ObjectMap<Window[]> computeWindowsByChunk(List<Window> windows) {
            Long2ObjectOpenHashMap<List<Window>> map = new Long2ObjectOpenHashMap<>();

            for (Window window : windows) {
                WindowInfo info = window.getWindowInfo();
                Bounds3I frameRegion = info.frameRegion();

                Point center = window.getCenter();
                Point mod = new Vec(frameRegion.lengthX() / 2D, frameRegion.lengthY() / 2D, frameRegion.lengthZ() / 2D);

                Point start = center.sub(mod);
                Point end = center.add(mod);

                int startX = start.blockX() >> 4;
                int startZ = start.blockZ() >> 4;

                int endX = end.blockX() >> 4;
                int endZ = end.blockZ() >> 4;

                for (int cx = startX; cx <= endX; cx++) {
                    for (int cz = startZ; cz <= endZ; cz++) {
                        map.computeIfAbsent(ChunkUtils.getChunkIndex(cx, cz), ignored -> new ArrayList<>(4))
                                .add(window);
                    }
                }
            }

            Long2ObjectOpenHashMap<Window[]> arrayMap = new Long2ObjectOpenHashMap<>(map.size());
            for (Long2ObjectMap.Entry<List<Window>> entry : map.long2ObjectEntrySet()) {
                arrayMap.put(entry.getLongKey(), entry.getValue().toArray(Window[]::new));
            }

            return arrayMap;
        }

        @Override
        public @NotNull Optional<Window> windowInRange(@NotNull Point origin, double distance) {
            int chunkStartX = (int)Math.floor(origin.x() - distance) >> 4;
            int chunkStartZ = (int)Math.floor(origin.z() - distance) >> 4;

            int chunkEndX = (int)Math.floor(origin.x() + distance) >> 4;
            int chunkEndZ = (int)Math.floor(origin.z() + distance) >> 4;

            Window closestWindow = null;
            double closestWindowDistance = Float.POSITIVE_INFINITY;

            for (int cx = chunkStartX; cx <= chunkEndX; cx++) {
                for (int cz = chunkStartZ; cz <= chunkEndZ; cz++) {
                    Window[] chunkWindows = windowsByChunk.get(ChunkUtils.getChunkIndex(cx, cz));
                    if (chunkWindows != null) {
                        for (Window window : chunkWindows) {
                            double thisDistance = origin.distanceSquared(window.getCenter());
                            if (thisDistance <= distance * distance && thisDistance < closestWindowDistance) {
                                closestWindow = window;
                                closestWindowDistance = thisDistance;
                            }
                        }
                    }
                }
            }

            return Optional.ofNullable(closestWindow);
        }

        @Override
        public @NotNull Optional<Window> windowAt(@NotNull Point origin) {
            long key = ChunkUtils.getChunkIndex(origin.chunkX(), origin.chunkZ());
            Window[] chunkWindows = windowsByChunk.get(key);

            if (chunkWindows == null) {
                return Optional.empty();
            }

            for (Window window : chunkWindows) {
                if (window.getWindowBounds().contains(origin.blockX(), origin.blockY(), origin.blockZ())) {
                    return Optional.of(window);
                }
            }

            return Optional.empty();
        }

        @Override
        public @NotNull @Unmodifiable List<Window> windows() {
            return windows;
        }
    }

    private final List<Spawnpoint> spawnpoints;
    private final List<Window> windows;
    private final List<Shop> shops;
    private final List<Door> doors;
    private final List<Room> rooms;
    private final List<Round> rounds;
    private final DependencyProvider mapDependencyProvider;
    private final Module module;

    private final WindowTracker windowTracker;


    public BasicMapObjects(@NotNull List<Spawnpoint> spawnpoints, @NotNull List<Window> windows,
            @NotNull List<Shop> shops, @NotNull List<Door> doors, @NotNull List<Room> rooms,
            @NotNull List<Round> rounds, @NotNull DependencyProvider mapDependencyProvider, @NotNull Module module) {
        this.spawnpoints = List.copyOf(spawnpoints);
        this.windows = List.copyOf(windows);
        this.shops = List.copyOf(shops);
        this.doors = List.copyOf(doors);
        this.rooms = List.copyOf(rooms);
        this.rounds = List.copyOf(rounds);
        this.mapDependencyProvider = Objects.requireNonNull(mapDependencyProvider, "mapDependencyProvider");
        this.module = Objects.requireNonNull(module, "module");

        this.windowTracker = new BasicWindowTracker(this.windows);
    }

    @Override
    public @Unmodifiable @NotNull List<Spawnpoint> spawnpoints() {
        return spawnpoints;
    }

    @Override
    public @Unmodifiable @NotNull List<Window> windows() {
        return windows;
    }

    @Override
    public @Unmodifiable @NotNull List<Shop> shops() {
        return shops;
    }

    @Override
    public @Unmodifiable @NotNull List<Door> doors() {
        return doors;
    }

    @Override
    public @Unmodifiable @NotNull List<Room> rooms() {
        return rooms;
    }

    @Override
    public @Unmodifiable @NotNull List<Round> rounds() {
        return rounds;
    }

    @Override
    public @NotNull DependencyProvider mapDependencyProvider() {
        return mapDependencyProvider;
    }

    @Override
    public @NotNull Module module() {
        return module;
    }

    @Override
    public @NotNull WindowTracker windowTracker() {
        return windowTracker;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (BasicMapObjects)obj;
        return Objects.equals(this.spawnpoints, that.spawnpoints) && Objects.equals(this.windows, that.windows) &&
                Objects.equals(this.shops, that.shops) && Objects.equals(this.doors, that.doors) &&
                Objects.equals(this.rooms, that.rooms) && Objects.equals(this.rounds, that.rounds) &&
                Objects.equals(this.mapDependencyProvider, that.mapDependencyProvider) &&
                Objects.equals(this.module, that.module);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spawnpoints, windows, shops, doors, rooms, rounds, mapDependencyProvider, module);
    }

    @Override
    public String toString() {
        return "BasicMapObjects[" + "spawnpoints=" + spawnpoints + ", " + "windows=" + windows + ", " + "shops=" +
                shops + ", " + "doors=" + doors + ", " + "rooms=" + rooms + ", " + "rounds=" + rounds + ", " +
                "mapDependencyProvider=" + mapDependencyProvider + ", " + "module=" + module + ']';
    }

}
