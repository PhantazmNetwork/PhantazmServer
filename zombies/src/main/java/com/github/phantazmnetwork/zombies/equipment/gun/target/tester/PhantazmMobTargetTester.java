package com.github.phantazmnetwork.zombies.equipment.gun.target.tester;

import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * A {@link TargetTester} that only selects {@link PhantazmMob}s.
 */
@Model("gun.target_tester.phantazm_mob")
@Cache(false)
public class PhantazmMobTargetTester implements TargetTester {

    private final Data data;
    private final MobStore mobStore;

    /**
     * Creates a {@link PhantazmMobTargetTester}.
     *
     * @param data     The {@link PhantazmMobTargetTester}'s {@link Data}
     * @param mobStore The {@link MobStore} to retrieve {@link PhantazmMob}s from
     */
    @FactoryMethod
    public PhantazmMobTargetTester(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.mob.store") MobStore mobStore) {
        this.data = Objects.requireNonNull(data, "data");
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
    }

    @Override
    public boolean useTarget(@NotNull Entity target, @NotNull Collection<UUID> previousHits) {
        UUID uuid = target.getUuid();
        return !(data.ignorePreviousHits() && previousHits.contains(target.getUuid())) && mobStore.getMob(uuid) != null;
    }

    /**
     * Data for a {@link PhantazmMobTargetTester}.
     *
     * @param ignorePreviousHits Whether to ignore previously hit {@link UUID}s
     */
    @DataObject
    public record Data(boolean ignorePreviousHits) {

    }
}
