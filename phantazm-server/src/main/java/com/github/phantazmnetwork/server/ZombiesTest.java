package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.ClientBlockHandler;
import com.github.phantazmnetwork.api.InstanceClientBlockHandler;
import com.github.phantazmnetwork.api.hologram.Hologram;
import com.github.phantazmnetwork.api.hologram.InstanceHologram;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import net.kyori.adventure.text.Component;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
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

                Hologram hologram = new InstanceHologram(spawnInstance, Vec3D.of(1, 101, 1), 0);
                hologram.add(Component.text("Angery"));
                hologram.add(Component.text("Vegetals"));
            }
        });

        global.addListener(PlayerPacketEvent.class, event -> {
            System.out.println(event.getPacket());
        });
    }
}
