package org.phantazm.mob2.validator;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface Validator {
    boolean valid(@NotNull Entity entity);
}
