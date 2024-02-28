package org.phantazm.mob2.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;

import java.util.Set;

@Model("mob.validator.mob_type")
@Cache
public class MobTypeValidator implements ValidatorComponent {
    private final Data data;

    @FactoryMethod
    public MobTypeValidator(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull Validator apply(@NotNull ExtensionHolder holder) {
        return new Internal(data);
    }

    @DataObject
    public record Data(@NotNull Set<Key> types,
        boolean blacklist) {

    }

    private record Internal(Data data) implements Validator {
        @Override
        public boolean valid(@NotNull Mob mob, @NotNull Entity entity) {
            if (!(entity instanceof Mob entityAsMob)) {
                return false;
            }

            return data.blacklist != data.types.contains(entityAsMob.data().key());
        }
    }
}