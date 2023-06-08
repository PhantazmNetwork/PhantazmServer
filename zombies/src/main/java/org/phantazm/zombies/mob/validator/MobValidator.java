package org.phantazm.zombies.mob.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.validator.TargetValidator;

@Model("zombies.mob.target_validator.mob_validator")
@Cache(false)
public class MobValidator implements TargetValidator {
    private final MobStore mobStore;

    @FactoryMethod
    public MobValidator(@NotNull MobStore mobStore) {
        this.mobStore = mobStore;
    }

    @Override
    public boolean valid(@NotNull Entity entity) {
        return mobStore.hasMob(entity.getUuid());
    }
}
