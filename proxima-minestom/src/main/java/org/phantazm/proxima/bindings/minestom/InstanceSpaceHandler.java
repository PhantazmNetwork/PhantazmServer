package org.phantazm.proxima.bindings.minestom;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public interface InstanceSpaceHandler {
    @NotNull InstanceSpace space();

    @NotNull Instance instance();
}
