package org.phantazm.mob.validator;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface TargetValidator {
    boolean valid(@NotNull Entity targeter, @NotNull Entity entity);
}
