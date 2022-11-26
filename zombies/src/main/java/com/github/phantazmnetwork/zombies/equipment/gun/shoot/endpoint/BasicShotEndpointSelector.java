package com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint;

import com.github.phantazmnetwork.core.RayUtils;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.blockiteration.BlockIteration;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.block.BlockIterator;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

/**
 * Basic implementation of a {@link ShotEndpointSelector}.
 */
@Model("zombies.gun.end_selector.basic")
@Cache(false)
public class BasicShotEndpointSelector implements ShotEndpointSelector {

    private final Data data;

    private final Supplier<Optional<? extends Entity>> entitySupplier;

    private final Collection<BlockIteration> blockIterations;

    /**
     * Creates a {@link BasicShotEndpointSelector}.
     *
     * @param data            The {@link Data} for the {@link BasicShotEndpointSelector}
     * @param shooterSupplier A {@link Supplier} for the {@link Entity} find the endpoint from
     * @param blockIteration  {@link BlockIteration}s method to find the endpoint with
     */
    @FactoryMethod
    public BasicShotEndpointSelector(@NotNull Data data, @NotNull @Dependency("zombies.dependency.gun.shooter.supplier")
    Supplier<Optional<? extends Entity>> shooterSupplier,
            @NotNull @DataName("block_iterations") Collection<BlockIteration> blockIteration) {
        this.data = Objects.requireNonNull(data, "data");
        this.entitySupplier = Objects.requireNonNull(shooterSupplier, "playerView");
        this.blockIterations = List.copyOf(blockIteration);
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        ConfigProcessor<Collection<String>> pathsProcessor = ConfigProcessor.STRING.collectionProcessor();
        return new ConfigProcessor<>() {

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                Collection<String> blockIterationPaths =
                        pathsProcessor.dataFromElement(element.getElementOrThrow("blockIterationPaths"));
                int maxDistance = element.getNumberOrThrow("maxDistance").intValue();
                if (maxDistance < 0) {
                    throw new ConfigProcessException("maxDistance must be greater than or equal to 0");
                }

                return new Data(blockIterationPaths, maxDistance);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(2);
                node.put("blockIterationPaths", pathsProcessor.elementFromData(data.blockIterationPaths()));
                node.putNumber("maxDistance", data.maxDistance());
                return node;
            }
        };
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull Optional<Point> getEnd(@NotNull Pos start) {
        return entitySupplier.get().map(player -> {
            Instance instance = player.getInstance();
            if (instance == null) {
                return null;
            }

            Collection<BlockIteration.Context> contexts = new ArrayList<>(blockIterations.size());
            for (BlockIteration blockIteration : blockIterations) {
                contexts.add(blockIteration.createContext());
            }
            Iterator<Point> it = new BlockIterator(start, 0, data.maxDistance());
            Point blockLocation = null;
            Block block = null;

            blockLoop:
            while (it.hasNext()) {
                blockLocation = it.next();
                block = instance.getBlock(blockLocation);

                for (BlockIteration.Context context : contexts) {
                    if (!context.isValidEndpoint(blockLocation, block)) {
                        continue blockLoop;
                    }
                }

                Shape shape = block.registry().collisionShape();
                Optional<Vec> intersectionOptional = RayUtils.getIntersectionPosition(shape, blockLocation, start);
                if (intersectionOptional.isPresent()) {
                    Vec intersection = intersectionOptional.get();
                    for (BlockIteration.Context context : contexts) {
                        if (!context.acceptRaytracedBlock(intersection, block)) {
                            continue blockLoop;
                        }

                        return intersection;
                    }
                }
            }

            if (block != null) {
                return RayUtils.rayTrace(new BoundingBox(1D, 1D, 1D), blockLocation.add(0.5D, 0D, 0.5D), start)
                        .orElse(null);
            }

            return null;
        });
    }

    /**
     * Data for a {@link BasicShotEndpointSelector}.
     *
     * @param blockIterationPaths Paths to the {@link BasicShotEndpointSelector}'s {@link BlockIteration}
     * @param maxDistance         The maximum distance of the endpoint from the start
     */
    @DataObject
    public record Data(@NotNull @DataPath("block_iterations") Collection<String> blockIterationPaths, int maxDistance) {

        /**
         * Creates a {@link Data}.
         *
         * @param blockIterationPaths A path to the {@link BasicShotEndpointSelector}'s {@link BlockIteration}
         * @param maxDistance         The maximum distance of the endpoint from the start
         */
        public Data {
            Objects.requireNonNull(blockIterationPaths, "blockIterationPaths");
        }

    }

}
