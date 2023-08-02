package org.phantazm.zombies.powerup.action.component;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.function.Function;

public interface PowerupActionComponent extends Function<@NotNull ZombiesScene, @NotNull PowerupAction> {
}
