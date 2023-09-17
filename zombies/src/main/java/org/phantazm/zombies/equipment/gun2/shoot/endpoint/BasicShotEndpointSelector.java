package org.phantazm.zombies.equipment.gun2.shoot.endpoint;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.block.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.RayUtils;
import org.phantazm.zombies.equipment.gun2.Keys;
import org.phantazm.zombies.equipment.gun2.shoot.blockiteration.BlockIteration;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;
import java.util.function.Supplier;


public class BasicShotEndpointSelector implements PlayerComponent<ShotEndpointSelector> {

    private final Data data;

    private final Collection<PlayerComponent<BlockIteration>> blockIterations;

    public BasicShotEndpointSelector(@NotNull Data data, @NotNull Supplier<Optional<? extends Entity>> shooterSupplier,
        @NotNull Collection<PlayerComponent<BlockIteration>> blockIteration) {
        this.data = Objects.requireNonNull(data);
        this.blockIterations = List.copyOf(blockIteration);
    }

    @Override
    public @NotNull ShotEndpointSelector forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        Collection<BlockIteration> iterations = new ArrayList<>(blockIterations.size());
        for (PlayerComponent<BlockIteration> blockIteration : blockIterations) {
            iterations.add(blockIteration.forPlayer(player, injectionStore));
        }

        Supplier<Optional<? extends Entity>> entitySupplier = injectionStore.get(Keys.GUN_MODULE).entitySupplier();

        return start -> entitySupplier.get().map(entity -> {
            Instance instance = entity.getInstance();
            if (instance == null) {
                return null;
            }

            Collection<BlockIteration.Context> contexts = new ArrayList<>(blockIterations.size());
            for (BlockIteration blockIteration : iterations) {
                contexts.add(blockIteration.createContext());
            }
            Iterator<Point> it = new BlockIterator(start, 0, data.maxDistance());
            Point blockLocation = null;
            Block block = null;

            blockLoop:
            while (it.hasNext()) {
                blockLocation = it.next();

                if (!instance.isChunkLoaded(blockLocation)) {
                    break;
                }
                block = instance.getBlock(blockLocation);

                for (BlockIteration.Context context : contexts) {
                    if (!context.isValidEndpoint(blockLocation, block)) {
                        block = null;
                        continue blockLoop;
                    }
                }

                Shape shape = block.registry().collisionShape();
                Optional<Vec> intersectionOptional = RayUtils.getIntersectionPosition(shape, blockLocation, start);
                if (intersectionOptional.isEmpty()) {
                    continue;
                }

                Vec intersection = intersectionOptional.get();
                for (BlockIteration.Context context : contexts) {
                    if (!context.acceptRaytracedBlock(intersection, block)) {
                        block = null;
                        continue blockLoop;
                    }
                }

                return intersection;
            }

            Pos limit = start.add(start.direction().mul(data.maxDistance));
            if (block != null) {
                return RayUtils.rayTrace(block.registry().collisionShape(), blockLocation, start).orElse(limit.asVec());
            }

            return limit;
        });
    }

    public record Data(@NotNull @ChildPath("block_iterations") Collection<String> blockIterations,
        int maxDistance) {
    }
}
