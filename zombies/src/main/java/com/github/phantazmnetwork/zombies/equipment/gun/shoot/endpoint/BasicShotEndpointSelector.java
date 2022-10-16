package com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.block.BlockIterator;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Basic implementation of a {@link ShotEndpointSelector}.
 */
@Model("zombies.gun.end_selector.basic")
public class BasicShotEndpointSelector implements ShotEndpointSelector {

    private final Data data;

    private final Supplier<Optional<? extends Entity>> entitySupplier;

    private final BlockIteration blockIteration;

    /**
     * Creates a {@link BasicShotEndpointSelector}.
     *
     * @param data            The {@link Data} for the {@link BasicShotEndpointSelector}
     * @param shooterSupplier A {@link Supplier} for the {@link Entity} find the endpoint from
     * @param blockIteration  A {@link BlockIteration} method to find the endpoint with
     */
    @FactoryMethod
    public BasicShotEndpointSelector(@NotNull Data data, @NotNull @Dependency("zombies.dependency.gun.shooter.supplier")
    Supplier<Optional<? extends Entity>> shooterSupplier,
            @NotNull @DataName("block_iteration") BlockIteration blockIteration) {
        this.data = Objects.requireNonNull(data, "data");
        this.entitySupplier = Objects.requireNonNull(shooterSupplier, "playerView");
        this.blockIteration = Objects.requireNonNull(blockIteration, "blockIteration");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Key> keyProcessor = ConfigProcessors.key();

        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                String blockIterationKey = element.getStringOrThrow("blockIterationPath");
                int maxDistance = element.getNumberOrThrow("maxDistance").intValue();
                if (maxDistance < 0) {
                    throw new ConfigProcessException("maxDistance must be greater than or equal to 0");
                }

                return new Data(blockIterationKey, maxDistance);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) {
                ConfigNode node = new LinkedConfigNode(2);
                node.putString("blockIterationPath", data.blockIterationPath());
                node.putNumber("maxDistance", data.maxDistance());
                return node;
            }
        };
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
     * @param blockIterationPath A path to the {@link BasicShotEndpointSelector}'s {@link BlockIteration}
     * @param maxDistance        The maximum distance of the endpoint from the start
     */
    @DataObject
    public record Data(@NotNull @DataPath("block_iteration") String blockIterationPath, int maxDistance) {

        /**
         * Creates a {@link Data}.
         *
         * @param blockIterationPath A path to the {@link BasicShotEndpointSelector}'s {@link BlockIteration}
         * @param maxDistance        The maximum distance of the endpoint from the start
         */
        public Data {
            Objects.requireNonNull(blockIterationPath, "blockIterationPath");
        }

    }

}
