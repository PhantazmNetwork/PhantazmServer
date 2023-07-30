package org.phantazm.mob.target;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;

import java.util.Optional;

@Model("mob.selector.ai_target")
@Cache
public class AITargetSelector implements TargetSelector<Entity> {
    @FactoryMethod
    public AITargetSelector() {

    }

    @Override
    public @NotNull Optional<Entity> selectTarget(@NotNull PhantazmMob self) {
        return Optional.ofNullable(self.entity().getTargetEntity());
    }
}
