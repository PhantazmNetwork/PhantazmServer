package org.phantazm.core.npc.supplier;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
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
        return new Entity(data.entityType);
    }

    @DataObject
    public record Data(@NotNull EntityType entityType) {

    }
}
