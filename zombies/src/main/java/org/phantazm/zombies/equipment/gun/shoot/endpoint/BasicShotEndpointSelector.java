package org.phantazm.zombies.equipment.gun.shoot.endpoint;

import com.github.steanky.element.core.annotation.*;
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
import org.phantazm.core.RayUtils;
import org.phantazm.zombies.equipment.gun.shoot.blockiteration.BlockIteration;

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
    public BasicShotEndpointSelector(@NotNull Data data, @NotNull Supplier<Optional<? extends Entity>> shooterSupplier,
            @NotNull @Child("block_iterations") Collection<BlockIteration> blockIteration) {
        this.data = Objects.requireNonNull(data, "data");
        this.entitySupplier = Objects.requireNonNull(shooterSupplier, "playerView");
        this.blockIterations = List.copyOf(blockIteration);
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
                return RayUtils.rayTrace(new BoundingBox(1D, 1D, 1D), blockLocation.add(0.5, 0, 0.5), start)
                        .orElse(null);
            }

            return null;
        });
    }

    /**
     * Data for a {@link BasicShotEndpointSelector}.
     *
     * @param blockIterations Paths to the {@link BasicShotEndpointSelector}'s {@link BlockIteration}
     * @param maxDistance     The maximum distance of the endpoint from the start
     */
    @DataObject
    public record Data(@NotNull @ChildPath("block_iterations") Collection<String> blockIterations, int maxDistance) {
    }
}
