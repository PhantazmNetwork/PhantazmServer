package org.phantazm.zombies.player.upgrade.validator;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Model("zombies.upgrade.validator.boolean")
@Cache
public class BooleanValidator implements ValidatorComponent {
    private final Data data;
    private final List<ValidatorComponent> validators;

    @FactoryMethod
    public BooleanValidator(@NotNull Data data, @NotNull @Child("validators") List<ValidatorComponent> validators) {
        this.data = Objects.requireNonNull(data);
        this.validators = validators;
    }

    @Override
    public @NotNull Validator apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        List<Validator> validators = new ArrayList<>(this.validators.size());
        for (ValidatorComponent component : this.validators) {
            validators.add(component.apply(injectionStore, zombiesPlayer));
        }

        return new Internal(data, validators);
    }

    private record Internal(Data data,
        List<Validator> validators) implements Validator {
        @Override
        public boolean test(Entity entity) {
            return switch (data.operation) {
                case AND -> {
                    for (Validator validator : validators) {
                        if (!validator.test(entity)) yield false;
                    }

                    yield true;
                }
                case OR -> {
                    for (Validator validator : validators) {
                        if (validator.test(entity)) yield true;
                    }

                    yield validators.isEmpty();
                }
            };
        }
    }

    public enum BooleanOperation {
        AND,
        OR
    }

    @DataObject
    public record Data(@NotNull BooleanOperation operation) {
    }
}
