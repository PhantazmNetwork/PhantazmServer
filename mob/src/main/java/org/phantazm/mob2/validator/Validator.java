package org.phantazm.mob2.validator;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;

public interface Validator {
    boolean valid(@NotNull Mob mob, @NotNull Entity entity);
}
