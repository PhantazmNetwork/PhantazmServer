package org.phantazm.mob2;

import com.github.steanky.proxima.node.Node;
import com.github.steanky.vector.Vec3I2ObjectMap;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.proxima.bindings.minestom.InstanceSpaceHandler;

public interface MobCreator {
    @NotNull Mob create(@NotNull Key key, @NotNull Instance instance);

    interface MobData {
        @NotNull EntityType type();
    }

    record InstanceSettings(@NotNull ThreadLocal<Vec3I2ObjectMap<Node>> nodeLocal,
                            @NotNull InstanceSpaceHandler spaceHandler) {

    }
}
