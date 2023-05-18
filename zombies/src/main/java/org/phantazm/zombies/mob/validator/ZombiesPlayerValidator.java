package org.phantazm.zombies.mob.validator;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.validator.TargetValidator;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

@Model("zombies.mob.target_validator.zombies_player")
@Cache(false)
public class ZombiesPlayerValidator implements TargetValidator {
    private final Supplier<? extends MapObjects> mapObjects;

    @FactoryMethod
    public ZombiesPlayerValidator(@NotNull Supplier<? extends MapObjects> mapObjects) {
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
    }

    @Override
    public boolean valid(@NotNull Entity entity) {
        UUID uuid = entity.getUuid();
        ZombiesPlayer zombiesPlayer = mapObjects.get().module().playerMap().get(uuid);
        if (zombiesPlayer != null) {
            return zombiesPlayer.canBeTargeted();
        }

        return entity.isActive();
    }
}
