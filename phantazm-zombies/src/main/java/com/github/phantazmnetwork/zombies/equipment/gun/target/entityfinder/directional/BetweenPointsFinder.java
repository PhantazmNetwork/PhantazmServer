package com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional;

import com.github.phantazmnetwork.api.config.VariantSerializable;
import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.Key;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class BetweenPointsFinder implements DirectionalEntityFinder {

    public record Data() implements VariantSerializable {

        public static final Key SERIAL_KEY
                = Key.key(Namespaces.PHANTAZM, "gun.entity_finder.directional.between_points");

        @Override
        public @NotNull Key getSerialKey() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    public BetweenPointsFinder(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull Collection<Entity> findEntities(@NotNull Instance instance, @NotNull Pos start,
                                                    @NotNull Point end) {
        Collection<Entity> entities = new ArrayList<>(instance.getEntities()); // TODO: optimize

        Point diff = end.sub(start);
        BoundingBox boundingBox = new BoundingBox(Math.abs(diff.x()), Math.abs(diff.y()), Math.abs(diff.z()));
        Point src = new Vec((start.x() + end.x()) / 2, Math.min(start.y(), end.y()), (start.z() + end.z()) / 2);
        entities.removeIf(entity -> !boundingBox.intersectEntity(src, entity));

        return entities;
    }

    @Override
    public @NotNull VariantSerializable getData() {
        return data;
    }
}
