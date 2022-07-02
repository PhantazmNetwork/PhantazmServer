package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.api.ClientBlockTracker;
import com.github.phantazmnetwork.api.InstanceClientBlockTracker;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerLoginEvent;
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
                ClientBlockTracker tracker = new InstanceClientBlockTracker(spawnInstance, global);
                tracker.setClientBlock(Block.BARRIER, 1, 100, 1);
            }
        });
    }
}
