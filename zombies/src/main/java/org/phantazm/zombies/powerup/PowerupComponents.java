package org.phantazm.zombies.powerup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;
import org.phantazm.zombies.powerup.visual.PowerupVisual;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public record PowerupComponents(@NotNull @Unmodifiable Collection<Supplier<PowerupVisual>> visuals,
                                @NotNull @Unmodifiable Collection<Supplier<PowerupAction>> actions,
                                @NotNull Supplier<DeactivationPredicate> deactivationPredicate) {
    public PowerupComponents(@NotNull Collection<Supplier<PowerupVisual>> visuals,
            @NotNull Collection<Supplier<PowerupAction>> actions,
            @NotNull Supplier<DeactivationPredicate> deactivationPredicate) {
        this.visuals = List.copyOf(visuals);
        this.actions = List.copyOf(actions);
        this.deactivationPredicate = Objects.requireNonNull(deactivationPredicate, "deactivationPredicate");
    }
}
