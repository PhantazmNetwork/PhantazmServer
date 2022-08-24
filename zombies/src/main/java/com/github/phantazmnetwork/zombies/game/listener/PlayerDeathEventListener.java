package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerDeathEventListener extends ZombiesPlayerEventListener<EntityDamageEvent> {

    private final Key knockedStateKey;

    public PlayerDeathEventListener(@NotNull Instance instance, @NotNull Map<UUID, ZombiesPlayer> zombiesPlayers,
            @NotNull Key knockedStateKey) {
        super(instance, zombiesPlayers);
        this.knockedStateKey = Objects.requireNonNull(knockedStateKey, "knockedStateKey");
    }

    @Override
    protected void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull EntityDamageEvent event) {
        if (event.getDamage() >= event.getEntity().getHealth()) {
            event.setCancelled(true);
            zombiesPlayer.setState(knockedStateKey);
        }
    }
}
