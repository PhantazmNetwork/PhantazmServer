package org.phantazm.zombies.equipment.gun.target.tester;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * A {@link TargetTester} that only selects {@link PhantazmMob}s.
 */
@Model("zombies.gun.target_tester.phantazm_mob")
@Cache(false)
public class PhantazmMobTargetTester implements TargetTester {
    private final MobStore mobStore;

    /**
     * Creates a {@link PhantazmMobTargetTester}.
     *
     * @param mobStore The {@link MobStore} to retrieve {@link PhantazmMob}s from
     */
    @FactoryMethod
    public PhantazmMobTargetTester(@NotNull MobStore mobStore) {
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
    }

    @Override
    public boolean useTarget(@NotNull Entity target, @NotNull Collection<UUID> previousHits) {
        return mobStore.getMob(target.getUuid()) != null;
    }
}
