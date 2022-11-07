package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public abstract class PlayerDeathEventListener extends ZombiesPlayerEventListener<EntityDamageEvent> {

    public PlayerDeathEventListener(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers) {
        super(instance, zombiesPlayers);
    }

    @Override
    protected void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull EntityDamageEvent event) {
        if (event.getDamage() >= event.getEntity().getHealth()) {
            event.setCancelled(true);
            onPlayerDeath(zombiesPlayer, event);
        }
    }

    protected abstract void onPlayerDeath(@NotNull ZombiesPlayer zombiesPlayer, @NotNull EntityDamageEvent event);

}
