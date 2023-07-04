package org.phantazm.mob.validator;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TargetValidator {
    boolean valid(@Nullable Entity targeter, @NotNull Entity entity);
}
