package org.phantazm.zombies.equipment.gun.shoot.wallshooting;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.Flags;
import org.phantazm.zombies.map.objects.MapObjects;

import java.util.Objects;

@Model("zombies.gun.wallshooting.wallshooting_checker.map_objects")
@Cache(false)
public class MapObjectsWallshootingChecker implements WallshootingChecker {

    private final MapObjects mapObjects;

    @FactoryMethod
    public MapObjectsWallshootingChecker(@NotNull MapObjects mapObjects) {
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
    }

    @Override
    public boolean canWallshoot() {
        return mapObjects.module().flags().hasFlag(Flags.WALLSHOOTING_ENABLED);
    }
}
