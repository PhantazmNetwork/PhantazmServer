package org.phantazm.mob2.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;

@Model("mob.validator.mob")
@Cache
public class MobValidator implements ValidatorComponent {
    private static final Internal INSTANCE = new Internal();

    @FactoryMethod
    public MobValidator() {
    }

    @Override
    public @NotNull Validator apply(@NotNull ExtensionHolder holder) {
        return INSTANCE;
    }

    private record Internal() implements Validator {
        @Override
        public boolean valid(@NotNull Mob mob, @NotNull Entity entity) {
            return entity instanceof Mob;
        }
    }
}
