package org.phantazm.proxima.bindings.minestom;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface InstanceSpaceHandler {
    @NotNull InstanceSpace space();

    @Nullable Instance instance();
}
