package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.game.map.Round;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public class PhantazmMobDeathListener extends PhantazmMobEventListener<EntityDeathEvent> {

    private final Supplier<? extends Round> roundSupplier;

    public PhantazmMobDeathListener(@NotNull Instance instance, @NotNull MobStore mobStore,
            @NotNull Supplier<? extends Round> roundSupplier) {
        super(instance, mobStore);
        this.roundSupplier = Objects.requireNonNull(roundSupplier, "roundSupplier");
    }

    @Override
    public void accept(@NotNull PhantazmMob mob, @NotNull EntityDeathEvent event) {
        Round round = roundSupplier.get();
        if (round != null) {
            round.removeMob(mob);
        }
    }
}
