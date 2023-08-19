package org.phantazm.zombies.mob2.validator;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.validator.Validator;
import org.phantazm.mob2.validator.ValidatorComponent;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.mob2.Keys;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.Set;

public class ZombiesPlayerValidator implements ValidatorComponent {
    private final Data data;

    @FactoryMethod
    public ZombiesPlayerValidator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull Validator apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(data, injectionStore.get(Keys.MAP_OBJECTS));
    }

    @DataObject
    public record Data(@NotNull Set<Key> states,
        boolean blacklist) {
    }

    private static class Internal implements Validator {
        private final Data data;
        private final MapObjects mapObjects;

        private Internal(Data data, MapObjects mapObjects) {
            this.data = data;
            this.mapObjects = mapObjects;
        }

        @Override
        public boolean valid(@NotNull Entity entity) {
            ZombiesPlayer player = mapObjects.module().playerMap().get(entity.getUuid());
            if (player != null) {
                Key currentState = player.module().getStateSwitcher().getState().key();
                if (data.blacklist) {
                    return !data.states.contains(currentState);
                }

                return data.states.contains(currentState);
            }

            return false;
        }
    }
}
