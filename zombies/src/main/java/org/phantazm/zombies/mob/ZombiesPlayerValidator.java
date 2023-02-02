package org.phantazm.zombies.mob;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.validator.TargetValidator;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Model("zombies.mob.target_validator.zombies_player")
@Cache
public class ZombiesPlayerValidator implements TargetValidator {
    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;

    @FactoryMethod
    public ZombiesPlayerValidator(@NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap) {
        this.playerMap = Objects.requireNonNull(playerMap, "playerMap");
    }

    @Override
    public boolean valid(@NotNull Entity entity) {
        UUID uuid = entity.getUuid();
        ZombiesPlayer zombiesPlayer = playerMap.get(uuid);
        if (zombiesPlayer != null) {
            return zombiesPlayer.canBeTargeted();
        }

        return entity.isActive();
    }
}
