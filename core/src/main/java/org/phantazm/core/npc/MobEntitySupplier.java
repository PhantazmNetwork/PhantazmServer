package org.phantazm.core.npc;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.EntityMeta;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@Model("npc.entity.supplier.mob")
@Cache
public class MobEntitySupplier implements Supplier<Entity> {
    private final Data data;

    @FactoryMethod
    public MobEntitySupplier(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public Entity get() {
        Entity entity = new Entity(data.entityType);
        if (!data.displayName.equals(Component.empty())) {
            EntityMeta meta = entity.getEntityMeta();
            meta.setCustomNameVisible(true);
            meta.setCustomName(data.displayName);
        }

        return entity;
    }

    @DataObject
    public record Data(@NotNull EntityType entityType, @NotNull Component displayName) {
        @Default("displayName")
        public static @NotNull ConfigElement defaultDisplayName() {
            return ConfigPrimitive.of("");
        }
    }
}
