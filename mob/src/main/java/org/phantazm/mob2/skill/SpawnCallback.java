package org.phantazm.mob2.skill;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;

import java.util.function.Consumer;

public interface SpawnCallback extends Consumer<@NotNull Mob> {
}
