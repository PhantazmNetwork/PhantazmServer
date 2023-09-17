package org.phantazm.zombies.equipment.gun2.shoot.wallshooting;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.Flags;
import org.phantazm.zombies.equipment.gun2.Keys;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

public class MapObjectsWallshootingChecker implements PlayerComponent<WallshootingChecker> {

    @Override
    public @NotNull WallshootingChecker forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        MapObjects mapObjects = injectionStore.get(Keys.GUN_MODULE).mapObjects();
        return () -> mapObjects.module().flags().hasFlag(Flags.WALLSHOOTING_ENABLED);
    }
}
