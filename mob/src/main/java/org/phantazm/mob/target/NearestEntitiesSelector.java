package org.phantazm.mob.target;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.validator.TargetValidator;

import java.util.Optional;

@Model("mob.selector.nearest_entities")
@Cache(false)
public class NearestEntitiesSelector extends NearestEntitiesSelectorAbstract<Entity> {

    @FactoryMethod
    public NearestEntitiesSelector(@NotNull Data data, @NotNull @Child("validator") TargetValidator targetValidator) {
        super(data.range, data.targetLimit, targetValidator);
    }

    @Override
    protected @NotNull Optional<Entity> mapTarget(@NotNull Entity entity) {
        return Optional.of(entity);
    }

    @DataObject
    public record Data(double range,
        int targetLimit,
        @NotNull @ChildPath("validator") String targetValidator) {
    }
}
