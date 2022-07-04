package com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint;

import com.github.phantazmnetwork.api.RayUtils;
import net.kyori.adventure.key.Keyed;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.Key;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.block.BlockIterator;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
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

    @Override
    public @NotNull Optional<Point> getEnd(@NotNull Pos start) {
        return playerView.getPlayer().map(player -> {
            Instance instance = player.getInstance();
            if (instance == null) {
                return null;
            }

            Iterator<Point> it = new BlockIterator(start, 0, data.maxDistance());
            Point blockLocation = null;
            Block block;
            while (it.hasNext()) {
                blockLocation = it.next();
                block = instance.getBlock(blockLocation);
                if (!block.isAir()) {
                    break;
                }
            }

            if (blockLocation != null) {
                return RayUtils.rayTrace(new BoundingBox(1D, 1D, 1D),
                        blockLocation.add(0.5D, 0D, 0.5D), start).orElse(null); // TODO: actual raytrace with shapes
            }

            return null;
        });
    }

    @Override
    public @NotNull Keyed getData() {
        return data;
    }
}
