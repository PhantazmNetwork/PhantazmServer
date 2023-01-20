package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.proxima.path.PathSettings;
import com.github.steanky.vector.Vec3IBiPredicate;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface PathSettingsProvider {
    @NotNull PathSettings groundSettings(@NotNull String name, @NotNull Entity entity, float jumpHeight,
            float fallTolerance, @NotNull Vec3IBiPredicate successPredicate);
}
