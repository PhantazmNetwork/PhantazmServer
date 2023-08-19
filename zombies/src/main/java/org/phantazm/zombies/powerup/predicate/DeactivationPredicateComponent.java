package org.phantazm.zombies.powerup.predicate;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.function.Function;

public interface DeactivationPredicateComponent
    extends Function<@NotNull ZombiesScene, @NotNull DeactivationPredicate> {
}
