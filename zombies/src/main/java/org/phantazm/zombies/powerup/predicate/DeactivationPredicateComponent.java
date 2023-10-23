package org.phantazm.zombies.powerup.predicate;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.function.Function;

public interface DeactivationPredicateComponent
    extends Function<@NotNull ZombiesScene, @NotNull DeactivationPredicate> {
}
