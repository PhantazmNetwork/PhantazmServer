package org.phantazm.zombies.listener;

import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.map.Round;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class PhantazmMobDeathListener extends PhantazmMobEventListener<EntityDeathEvent> {

    private final Supplier<? extends Optional<Round>> roundSupplier;

    public PhantazmMobDeathListener(@NotNull Instance instance, @NotNull MobStore mobStore,
            @NotNull Supplier<? extends Optional<Round>> roundSupplier) {
        super(instance, mobStore);
        this.roundSupplier = Objects.requireNonNull(roundSupplier, "roundSupplier");
    }

    @Override
    public void accept(@NotNull PhantazmMob mob, @NotNull EntityDeathEvent event) {
        roundSupplier.get().ifPresent(round -> {
            round.removeMob(mob);
        });

        getMobStore().onMobDeath(event);
    }
}
