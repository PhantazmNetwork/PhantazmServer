package org.phantazm.mob.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Model("mob.target_validator.distance")
@Cache
public class DistanceValidator implements TargetValidator {
    private final Data data;

    @FactoryMethod
    public DistanceValidator(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public boolean valid(@Nullable Entity targeter, @NotNull Entity entity) {
        if (targeter == null) {
            return false;
        }

        return targeter.getDistanceSquared(entity) < data.distance * data.distance;
    }

    @DataObject
    public record Data(double distance) {
    }
}
