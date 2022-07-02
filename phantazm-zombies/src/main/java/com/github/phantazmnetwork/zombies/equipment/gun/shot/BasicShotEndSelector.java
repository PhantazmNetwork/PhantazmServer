package com.github.phantazmnetwork.zombies.equipment.gun.shot;

import com.github.phantazmnetwork.api.RayUtils;
import com.github.phantazmnetwork.api.player.PlayerView;
import com.github.phantazmnetwork.api.target.TargetSelectorInstance;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.zombies.equipment.target.TargetSelector;
import net.kyori.adventure.key.Key;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.block.BlockIterator;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class BasicShotEndSelector implements TargetSelector<Point> {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "selector.shot.end.basic");

    private final int maxDistance;

    public BasicShotEndSelector(int maxDistance) {
        this.maxDistance = maxDistance;
    }

    @Override
    public @NotNull TargetSelectorInstance<Point> createSelector(@NotNull MobStore store, @NotNull PlayerView playerView) {
        return () -> playerView.getPlayer().map(player -> {
            Instance instance = player.getInstance();
            if (instance == null) {
                return null;
            }

            Pos start = player.getPosition().add(0, player.getEyeHeight(), 0);

            Iterator<Point> it = new BlockIterator(player, maxDistance);
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
    public @NotNull Key getSerialKey() {
        return SERIAL_KEY;
    }
}
