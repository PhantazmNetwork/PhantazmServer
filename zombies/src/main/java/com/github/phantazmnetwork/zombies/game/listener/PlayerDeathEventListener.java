package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.core.VecUtils;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.game.player.state.ZombiesPlayerState;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;

public class PlayerDeathEventListener extends ZombiesPlayerEventListener<EntityDamageEvent> {

    private final BiFunction<ZombiesPlayer, Vec3I, ZombiesPlayerState> deathStateCreator;

    public PlayerDeathEventListener(@NotNull Instance instance, @NotNull Map<UUID, ZombiesPlayer> zombiesPlayers,
            @NotNull BiFunction<ZombiesPlayer, Vec3I, ZombiesPlayerState> deathStateCreator) {
        super(instance, zombiesPlayers);
        this.deathStateCreator = Objects.requireNonNull(deathStateCreator, "deathStateCreator");
    }

    @Override
    protected void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull EntityDamageEvent event) {
        if (event.getDamage() >= event.getEntity().getHealth()) {
            event.setCancelled(true);
            zombiesPlayer.getStateSwitcher().setState(
                    deathStateCreator.apply(zombiesPlayer, VecUtils.toBlockInt(event.getEntity().getPosition())));
        }
    }
}
