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

public class BasicShotEndpointSelector implements ShotEndpointSelector {

    public record Data(@NotNull Key blockIterationKey, int maxDistance) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.end_selector.basic");

        public Data {
            Objects.requireNonNull(blockIterationKey, "blockIterationKey");
        }

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

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

    public static @NotNull BiConsumer<Data, Collection<Key>> dependencyConsumer() {
        return (data, keys) -> {
            keys.add(data.blockIterationKey());
        };
    }

    private final Data data;

    private final Supplier<Optional<? extends Entity>> entitySupplier;

    private final BlockIteration blockIteration;

    public BasicShotEndpointSelector(@NotNull Data data, @NotNull Supplier<Optional<? extends Entity>> entitySupplier,
                                     @NotNull BlockIteration blockIteration) {
        this.data = Objects.requireNonNull(data, "data");
        this.entitySupplier = Objects.requireNonNull(entitySupplier, "playerView");
        this.blockIteration = Objects.requireNonNull(blockIteration, "blockIteration");
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

}
