package com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional;

import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class BetweenPointsFinder implements DirectionalEntityFinder {

    public record Data() implements Keyed {

        public static final Key SERIAL_KEY
                = Key.key(Namespaces.PHANTAZM, "gun.entity_finder.directional.between_points");

        @Override
        public @NotNull Key key() {
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
        Collection<Entity> entities = new ArrayList<>(instance.getEntities().size());
        instance.getEntityTracker().raytraceCandidates(start, end, EntityTracker.Target.ENTITIES, entities::add);

        return entities;
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }
}
