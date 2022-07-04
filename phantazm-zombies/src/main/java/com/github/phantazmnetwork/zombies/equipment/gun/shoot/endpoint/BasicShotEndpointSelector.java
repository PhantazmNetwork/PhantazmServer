package com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint;

import com.github.phantazmnetwork.api.PointUtils;
import com.github.phantazmnetwork.api.RayUtils;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.block.BlockIterator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class BasicShotEndpointSelector implements ShotEndpointSelector {

    public record Data(int maxDistance) implements Keyed {

        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "selector.shot.end.basic");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    private final Data data;

    private final PlayerView playerView;

    public BasicShotEndpointSelector(@NotNull Data data, @NotNull PlayerView playerView) {
        this.data = data;
        this.playerView = playerView;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull Optional<Point> getEnd(@NotNull Pos start) {
        return playerView.getPlayer().map(player -> {
            Instance instance = player.getInstance();
            if (instance == null) {
                return null;
            }

            Iterator<Point> it = new BlockIterator(start, 0, data.maxDistance());
            Point blockLocation = null;
            Block block = null;
            while (it.hasNext()) {
                blockLocation = it.next();
                block = instance.getBlock(blockLocation);
                if (!block.isAir()) {
                    Shape shape = block.registry().collisionShape();
                    Point finalBlockLocation = blockLocation;
                    Optional<Vec> rayTrace = RayUtils.rayTrace(shape, blockLocation, start).map(trace -> {
                        if (shape.childBounds().isEmpty()) {
                            return trace;
                        }

                        List<Vec> traces = new ArrayList<>(shape.childBounds().size() + 1);
                        traces.add(trace);

                        for (Shape child : shape.childBounds()) {
                            RayUtils.rayTrace(child, finalBlockLocation, start).ifPresent(traces::add);
                        }

                        PointUtils.sortPointsByDistance(start, traces);
                        return traces.get(0);
                    });

                    if (rayTrace.isPresent()) {
                        return rayTrace.get();
                    }
                }
            }

            if (block != null) {
                return RayUtils.rayTrace(new BoundingBox(1D, 1D, 1D),
                        blockLocation.add(0.5D, 0D, 0.5D), start).orElse(null);
            }

            return null;
        });
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }
}
