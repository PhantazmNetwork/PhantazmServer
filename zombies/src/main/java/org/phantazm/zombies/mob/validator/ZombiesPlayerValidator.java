package org.phantazm.zombies.mob.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.validator.TargetValidator;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Set;
import java.util.function.Supplier;

@Model("zombies.mob.target_validator.zombies_player")
@Cache(false)
public class ZombiesPlayerValidator implements TargetValidator {
    private final Data data;
    private final Supplier<? extends MapObjects> mapObjects;

    @FactoryMethod
    public ZombiesPlayerValidator(@NotNull Data data, @NotNull Supplier<? extends MapObjects> mapObjects) {
        this.data = data;
        this.mapObjects = mapObjects;
    }

    @Override
    public boolean valid(@NotNull Entity targeter, @NotNull Entity entity) {
        MapObjects mapObjects = this.mapObjects.get();
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

    @DataObject
    public record Data(@NotNull Set<Key> states, boolean blacklist) {
    }
}
