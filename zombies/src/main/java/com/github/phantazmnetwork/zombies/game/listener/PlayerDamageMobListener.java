package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerDamageMobListener extends PhantazmMobEventListener<EntityDamageEvent> {

    private final Map<UUID, ZombiesPlayer> zombiesPlayers;

    public PlayerDamageMobListener(@NotNull Instance instance, @NotNull MobStore mobStore,
            @NotNull Map<UUID, ZombiesPlayer> zombiesPlayers) {
        super(instance, mobStore);
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
    }

    @Override
    public void accept(@NotNull PhantazmMob mob, @NotNull EntityDamageEvent event) {
        if (!(event.getDamageType() instanceof EntityDamage entityDamage)) {
            return;
        }
        ZombiesPlayer zombiesPlayer = zombiesPlayers.get(entityDamage.getSource().getUuid());
        if (zombiesPlayer == null) {
            return;
        }

        if (event.getDamage() >= event.getEntity().getHealth()) {
            zombiesPlayer.getModule().getKills().onKill(mob);
        }
    }
}
