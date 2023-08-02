package org.phantazm.zombies.powerup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.zombies.powerup.action.component.PowerupActionComponent;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicateComponent;
import org.phantazm.zombies.powerup.visual.PowerupVisualComponent;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public record PowerupComponents(@NotNull @Unmodifiable Collection<PowerupVisualComponent> visuals,
                                @NotNull @Unmodifiable Collection<PowerupActionComponent> actions,
                                @NotNull DeactivationPredicateComponent deactivationPredicate) {
    public PowerupComponents(@NotNull Collection<PowerupVisualComponent> visuals,
            @NotNull Collection<PowerupActionComponent> actions,
            @NotNull DeactivationPredicateComponent deactivationPredicate) {
        this.visuals = List.copyOf(visuals);
        this.actions = List.copyOf(actions);
        this.deactivationPredicate = Objects.requireNonNull(deactivationPredicate, "deactivationPredicate");
    }
}
