package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.zombies.game.event.PhantazmMobEvent;
import com.github.phantazmnetwork.zombies.game.map.Round;
import net.minestom.server.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public class PhantazmMobDeathListener implements PhantazmMobEventListener<EntityDeathEvent> {

    private final Supplier<? extends Round> roundSupplier;

    public PhantazmMobDeathListener(@NotNull Supplier<? extends Round> roundSupplier) {
        this.roundSupplier = Objects.requireNonNull(roundSupplier, "roundSupplier");
    }

    @Override
    public void accept(@NotNull PhantazmMobEvent<EntityDeathEvent> event) {
        Round round = roundSupplier.get();
        if (round != null) {
            round.removeMob(event.mob());
        }
    }
}
