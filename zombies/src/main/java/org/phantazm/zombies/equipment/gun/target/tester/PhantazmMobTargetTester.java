package org.phantazm.zombies.equipment.gun.target.tester;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.objects.MapObjects;

import java.util.Collection;
import java.util.UUID;

@Model("zombies.gun.target_tester.phantazm_mob")
@Cache(false)
public class PhantazmMobTargetTester implements TargetTester {
    private final MapObjects mapObjects;

    @FactoryMethod
    public PhantazmMobTargetTester(@NotNull MapObjects mapObjects) {
        this.mapObjects = mapObjects;
    }

    @Override
    public boolean useTarget(@NotNull Entity target, @NotNull Collection<UUID> previousHits) {
        return mapObjects.module().roundHandlerSupplier().get().currentRound()
            .map(round -> round.hasMob(target.getUuid())).orElse(false);
    }
}
