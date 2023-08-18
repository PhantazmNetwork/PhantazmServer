package org.phantazm.mob2;

import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.proxima.node.Node;
import com.github.steanky.vector.Vec3I2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.proxima.bindings.minestom.InstanceSpaceHandler;

import java.util.Map;
import java.util.Optional;

public interface MobCreator {
    interface MobData {
        @NotNull EntityType type();

        @NotNull Map<EquipmentSlot, ItemStack> equipment();

        @NotNull Object2FloatArrayMap<String> attributes();

        @NotNull Optional<Component> hologramDisplayName();

        @NotNull @Unmodifiable ConfigNode extra();

        @NotNull @Unmodifiable Map<String, Object> meta();
    }

    record InstanceSettings(@NotNull ThreadLocal<Vec3I2ObjectMap<Node>> nodeLocal,
                            @NotNull InstanceSpaceHandler spaceHandler) {
    }

    @NotNull Mob create(@NotNull Key key, @NotNull Instance instance);
}
