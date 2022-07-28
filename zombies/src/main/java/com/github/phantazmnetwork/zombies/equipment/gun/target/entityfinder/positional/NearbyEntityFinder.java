package com.github.phantazmnetwork.zombies.equipment.gun.target.entityfinder.positional;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

/**
 * A {@link PositionalEntityFinder} which finds all nearby {@link Entity}s.
 */
public class NearbyEntityFinder implements PositionalEntityFinder {

    private final Data data;

    /**
     * Creates a {@link NearbyEntityFinder}.
     *
     * @param data The {@link NearbyEntityFinder}'s {@link Data}
     */
    public NearbyEntityFinder(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                double range = element.getNumberOrThrow("range").doubleValue();
                return new Data(range);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                ConfigNode node = new LinkedConfigNode(1);
                node.putNumber("range", data.range());

                return node;
            }
        };
    }

    @Override
    public @NotNull Collection<Entity> findEntities(@NotNull Instance instance, @NotNull Point start) {
        return instance.getNearbyEntities(start, data.range());
    }

    /**
     * Data for a {@link NearbyEntityFinder}.
     *
     * @param range The euclidean distance range to search for nearby {@link Entity}s
     */
    public record Data(double range) implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY =
                Key.key(Namespaces.PHANTAZM, "gun.target.entity_finder.positional.nearby_entity");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

}
