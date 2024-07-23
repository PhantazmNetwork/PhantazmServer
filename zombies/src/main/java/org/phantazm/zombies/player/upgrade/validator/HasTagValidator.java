package org.phantazm.zombies.player.upgrade.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.Set;

@Model("zombies.upgrade.validator.has_tag")
@Cache
public class HasTagValidator implements ValidatorComponent {
    private final Data data;

    @FactoryMethod
    public HasTagValidator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull Validator apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data);
    }

    private record Internal(Data data) implements Validator {
        @Override
        public boolean test(Entity entity) {
            if (!(entity instanceof Mob mob)) {
                return false;
            }

            for (Key tag : data.tags) {
                if (mob.data().tags().contains(tag) != data.blacklist) {
                    return false;
                }
            }

            return data.blacklist;
        }
    }

    @DataObject
    public record Data(@NotNull Set<Key> tags,
        boolean blacklist) {
    }
}
