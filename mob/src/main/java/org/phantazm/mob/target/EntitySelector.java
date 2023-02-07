package org.phantazm.mob.target;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link TargetSelector} that selects itself.
 */
@Model("mob.selector.entity")
@Cache
public class EntitySelector implements TargetSelector<Entity> {
    @FactoryMethod
    public EntitySelector() {

    }

    @Override
    public @NotNull Optional<Entity> selectTarget(@NotNull PhantazmMob self) {
        return Optional.of(self.entity());
    }
}
