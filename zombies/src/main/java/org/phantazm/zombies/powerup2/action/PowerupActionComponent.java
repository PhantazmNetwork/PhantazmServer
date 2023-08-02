package org.phantazm.zombies.powerup2.action;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.function.Function;

public interface PowerupActionComponent extends Function<@NotNull ZombiesScene, @NotNull PowerupAction> {
}
