package org.phantazm.mob.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Model("mob.target_validator.always_valid")
@Cache
public class AlwaysValid implements TargetValidator {
    @FactoryMethod
    public AlwaysValid() {
    }

    @Override
    public boolean valid(@Nullable Entity targeter, @NotNull Entity entity) {
        return true;
    }
}
