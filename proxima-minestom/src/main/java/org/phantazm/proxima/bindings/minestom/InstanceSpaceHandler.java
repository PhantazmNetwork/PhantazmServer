package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.proxima.space.Space;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface InstanceSpaceHandler {
    @NotNull Space space();

    @Nullable Instance instance();
}
