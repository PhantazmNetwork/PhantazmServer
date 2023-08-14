package org.phantazm.mob2.validator;

import com.github.steanky.element.core.annotation.FactoryMethod;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Keys;
import org.phantazm.mob2.Mob;

public class LineOfSightValidator implements ValidatorComponent {
    @FactoryMethod
    public LineOfSightValidator() {
    }

    @Override
    public @NotNull Validator apply(@NotNull InjectionStore injectionStore) {
        return new Internal(injectionStore.get(Keys.MOB_KEY));
    }

    private record Internal(Mob self) implements Validator {
        @Override
        public boolean valid(@NotNull Entity entity) {
            return self.hasLineOfSight(entity);
        }
    }
}
