package org.phantazm.mob2.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;

import java.util.Objects;

@Model("mob.validator.distance")
@Cache
public class DistanceValidator implements ValidatorComponent {
    private final Data data;

    @FactoryMethod
    public DistanceValidator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull Validator apply(@NotNull ExtensionHolder holder) {
        return new Internal(data);
    }

    @DataObject
    public record Data(double distance) {
    }

    private record Internal(Data data) implements Validator {
        @Override
        public boolean valid(@NotNull Mob mob, @NotNull Entity entity) {
            return mob.getDistanceSquared(entity) < data.distance * data.distance;
        }
    }
}
