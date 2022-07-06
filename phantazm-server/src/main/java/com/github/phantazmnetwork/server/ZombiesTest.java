package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.ClientBlockHandler;
import com.github.phantazmnetwork.api.InstanceClientBlockHandler;
import com.github.phantazmnetwork.api.VecUtils;
import com.github.phantazmnetwork.api.hologram.Hologram;
import com.github.phantazmnetwork.api.hologram.InstanceHologram;
import com.github.phantazmnetwork.commons.InterpolationUtils;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

final class ZombiesTest {
    private ZombiesTest() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull EventNode<Event> global) {

        global.addListener(PlayerLoginEvent.class, event -> {
            Instance spawnInstance = event.getSpawningInstance();
            if(spawnInstance != null) {
                ClientBlockHandler tracker = new InstanceClientBlockHandler(spawnInstance, global);
                tracker.setClientBlock(Block.BARRIER, 1, 100, 1);

                Hologram hologram = new InstanceHologram(Vec3D.of(1, 101, 1), 0);
                hologram.add(Component.text("Angery").color(TextColor.color(255, 0, 0)));
                hologram.add(Component.text("Vegetals").color(TextColor.color(0, 255, 0)));
                hologram.setInstance(spawnInstance);
            }
        });

        global.addListener(PlayerUseItemEvent.class, event -> {
            Player player = event.getPlayer();
            Instance instance = player.getInstance();
            if(instance == null) {
                return;
            }

            Vec direction = player.getPosition().direction();
            Point start = player.getPosition().add(0, player.getEyeHeight(), 0).add(direction.mul(2));
            Point end = start.add(direction.mul(1000));

            InterpolationUtils.interpolateLine(VecUtils.toDouble(start), VecUtils.toDouble(end), action -> {
                instance.setBlock(action.getX(), action.getY(), action.getZ(), Block.AMETHYST_BLOCK);
                return false;
            });
        });
    }
}
