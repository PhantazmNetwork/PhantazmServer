package com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.directional;

import net.kyori.adventure.key.Keyed;
import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class AroundEndFinder implements DirectionalEntityFinder {

    public record Data(double range) implements Keyed {

        public static final Key SERIAL_KEY
                = Key.key(Namespaces.PHANTAZM, "gun.entity_finder.directional.around_end");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    public AroundEndFinder(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull Collection<Entity> findEntities(@NotNull Instance instance, @NotNull Pos start,
                                                    @NotNull Point end) {
        return instance.getNearbyEntities(end, data.range());
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }
}
