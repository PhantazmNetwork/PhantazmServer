package org.phantazm.zombies.equipment.gun2.shoot.blockiteration;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.equipment.gun2.Keys;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

public class WindowBlockIteration implements PlayerComponent<BlockIteration> {

    @Override
    public @NotNull BlockIteration forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        MapObjects mapObjects = injectionStore.get(Keys.GUN_MODULE).mapObjects();
        return () -> new Context(mapObjects);
    }

    private static class Context implements BlockIteration.Context {

        private final MapObjects mapObjects;

        public Context(MapObjects mapObjects) {
            this.mapObjects = mapObjects;
        }

        @Override
        public boolean isValidEndpoint(@NotNull Point blockLocation, @NotNull Block block) {
            return mapObjects.windowTracker().atPoint(blockLocation).isEmpty();
        }

        @Override
        public boolean acceptRaytracedBlock(@NotNull Vec intersection, @NotNull Block block) {
            return true;
        }
    }

}
