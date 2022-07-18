package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.zombies.game.event.ZombiesPlayerEvent;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlayerDamageMobListener implements ZombiesPlayerListener<EntityDamageEvent> {

    private final MobStore mobStore;

    public PlayerDamageMobListener(@NotNull MobStore mobStore) {
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
    }

    @Override
    public void accept(@NotNull ZombiesPlayerEvent<EntityDamageEvent> event) {
        EntityDamageEvent damageEvent = event.event();
        LivingEntity entity = damageEvent.getEntity();
        if (damageEvent.getDamage() >= entity.getHealth() && mobStore.hasMob(damageEvent.getEntity().getUuid())) {
            ZombiesPlayer zombiesPlayer = event.zombiesPlayer();
            // TODO: increment kills
        }
    }
}
