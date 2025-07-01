package org.phantazm.zombies.equipment.gun.target.tester;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.event.equipment.GunTargetSelectEvent;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Collection;
import java.util.UUID;

@Model("zombies.gun.target_tester.phantazm_mob")
@Cache(false)
public class PhantazmMobTargetTester implements TargetTester {
    private final MapObjects mapObjects;
    private final ZombiesScene zombiesScene;

    @FactoryMethod
    public PhantazmMobTargetTester(@NotNull MapObjects mapObjects, @NotNull ZombiesScene zombiesScene) {
        this.mapObjects = mapObjects;
        this.zombiesScene = zombiesScene;
    }

    @Override
    public boolean useTarget(@NotNull Gun gun, @NotNull Entity target, @NotNull Collection<UUID> previousHits) {
        return mapObjects.module().roundHandlerSupplier().get().currentRound()
            .map(round -> {
                GunTargetSelectEvent selectEvent = new GunTargetSelectEvent(target, gun);
                zombiesScene.broadcastEvent(selectEvent);

                boolean isForceSelected = selectEvent.isForceSelected();

                if (target instanceof Mob mob && mob.data().extra().getBooleanOrDefault("force_deselect", false))
                    return isForceSelected;

                return isForceSelected || round.hasMob(target.getUuid());
            }).orElse(false);
    }
}
