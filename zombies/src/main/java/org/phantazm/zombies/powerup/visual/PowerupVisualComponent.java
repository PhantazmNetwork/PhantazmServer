package org.phantazm.zombies.powerup.visual;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.function.Function;

public interface PowerupVisualComponent extends Function<@NotNull ZombiesScene, @NotNull PowerupVisual> {
}
