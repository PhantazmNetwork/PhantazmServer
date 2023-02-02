package org.phantazm.mob.target;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link TargetSelector} that selects itself.
 */
@Model("mob.selector.entity")
@Cache(false)
public class EntitySelector implements TargetSelector<Entity> {

    private final Entity entity;

    @FactoryMethod
    public EntitySelector(@NotNull Entity entity) {
        this.entity = Objects.requireNonNull(entity, "entity");
    }

    @Override
    public @NotNull Optional<Entity> selectTarget() {
        return Optional.of(entity);
    }
}
