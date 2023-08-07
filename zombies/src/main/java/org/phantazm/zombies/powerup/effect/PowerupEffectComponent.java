package org.phantazm.zombies.powerup.effect;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.function.Function;

public interface PowerupEffectComponent extends Function<@NotNull ZombiesScene, @NotNull PowerupEffect> {
}
