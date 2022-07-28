package com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.block.BlockIterator;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Basic implementation of a {@link ShotEndpointSelector}.
 */
public class BasicShotEndpointSelector implements ShotEndpointSelector {

    private final Data data;
    private final Supplier<Optional<? extends Entity>> entitySupplier;
    private final BlockIteration blockIteration;

    /**
     * Creates a {@link BasicShotEndpointSelector}.
     *
     * @param data           The {@link Data} for the {@link BasicShotEndpointSelector}
     * @param entitySupplier A {@link Supplier} for the {@link Entity} find the endpoint from
     * @param blockIteration A {@link BlockIteration} method to find the endpoint with
     */
    public BasicShotEndpointSelector(@NotNull Data data, @NotNull Supplier<Optional<? extends Entity>> entitySupplier,
            @NotNull BlockIteration blockIteration) {
        this.data = Objects.requireNonNull(data, "data");
        this.entitySupplier = Objects.requireNonNull(entitySupplier, "playerView");
        this.blockIteration = Objects.requireNonNull(blockIteration, "blockIteration");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Key> keyProcessor = AdventureConfigProcessors.key();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Key blockIterationKey = keyProcessor.dataFromElement(element.getElementOrThrow("blockIterationKey"));
                int maxDistance = element.getNumberOrThrow("maxDistance").intValue();
                if (maxDistance < 0) {
                    throw new ConfigProcessException("maxDistance must be greater than or equal to 0");
                }

                return new Data(blockIterationKey, maxDistance);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.put("blockIterationKey", keyProcessor.elementFromData(data.blockIterationKey()));
                node.putNumber("maxDistance", data.maxDistance());
                return node;
            }
        };
    }

    /**
     * Creates a dependency consumer for {@link Data}s.
     *
     * @return A dependency consumer for {@link Data}s
     */
    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> keys.add(data.blockIterationKey());
    }

    @Override
    public @NotNull Optional<Point> getEnd(@NotNull Pos start) {
        return entitySupplier.get().map(player -> {
            Instance instance = player.getInstance();
            if (instance == null) {
                return null;
            }

            Iterator<Point> it = new BlockIterator(start, 0, data.maxDistance());
            return blockIteration.findEnd(instance, player, start, it).orElse(null);
        });
    }

    /**
     * Data for a {@link BasicShotEndpointSelector}.
     *
     * @param blockIterationKey A {@link Key} to the {@link BasicShotEndpointSelector}'s {@link BlockIteration}
     * @param maxDistance       The maximum distance of the endpoint from the start
     */
    public record Data(@NotNull Key blockIterationKey, int maxDistance) implements Keyed {

        /**
         * The serial {@link Key} for this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.end_selector.basic");

        /**
         * Creates a {@link Data}.
         *
         * @param blockIterationKey A {@link Key} to the {@link BasicShotEndpointSelector}'s {@link BlockIteration}
         * @param maxDistance       The maximum distance of the endpoint from the start
         */
        public Data {
            Objects.requireNonNull(blockIterationKey, "blockIterationKey");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

}
