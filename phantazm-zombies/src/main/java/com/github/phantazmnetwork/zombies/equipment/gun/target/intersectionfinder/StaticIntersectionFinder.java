package com.github.phantazmnetwork.zombies.equipment.gun.target.intersectionfinder;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class StaticIntersectionFinder implements IntersectionFinder {

    public record Data() implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM,"gun.intersection_finder.static");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                return new Data();
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                return new LinkedConfigNode(0);
            }
        };
    }

    private final Data data;

    public StaticIntersectionFinder(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull Optional<Vec> getHitLocation(@NotNull Entity entity, @NotNull Pos start) {
        BoundingBox boundingBox = entity.getBoundingBox();
        double centerX = (boundingBox.minX() + boundingBox.maxX()) / 2;
        double centerY = (boundingBox.minY() + boundingBox.maxY()) / 2;
        double centerZ = (boundingBox.minZ() + boundingBox.maxZ()) / 2;

        return Optional.of(new Vec(centerX, centerY, centerZ));
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }
}
