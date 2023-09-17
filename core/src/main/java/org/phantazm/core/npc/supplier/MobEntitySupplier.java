package org.phantazm.core.npc.supplier;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.BasicComponent;
import org.phantazm.commons.InjectionStore;

import java.util.function.Supplier;

@Model("npc.entity.supplier.mob")
@Cache
public class MobEntitySupplier implements BasicComponent<Supplier<Entity>> {
    private final Data data;

    @FactoryMethod
    public MobEntitySupplier(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public Supplier<Entity> apply(@NotNull InjectionStore injectionStore) {
        return new Internal(data);
    }

    private record Internal(Data data) implements Supplier<Entity> {
        @Override
        public Entity get() {
            return new LivingEntity(data.entityType);
        }
    }

    @DataObject
    public record Data(@NotNull EntityType entityType) {

    }
}
