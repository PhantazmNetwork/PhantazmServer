package org.phantazm.mob2.validator;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Keys;
import org.phantazm.mob2.Mob;

public class DistanceValidator implements ValidatorComponent {
    private final Data data;

    @FactoryMethod
    public DistanceValidator(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull Validator apply(@NotNull InjectionStore injectionStore) {
        return new Internal(injectionStore.get(Keys.MOB_KEY), data);
    }

    @DataObject
    public record Data(double distance) {
    }

    private record Internal(Mob self, Data data) implements Validator {
        @Override
        public boolean valid(@NotNull Entity entity) {
            return self.getDistanceSquared(entity) < data.distance * data.distance;
        }
    }
}
