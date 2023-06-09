package org.phantazm.zombies.listener;

import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerDamageMobListener extends PhantazmMobEventListener<EntityDamageEvent> {

    private final Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers;

    public PlayerDamageMobListener(@NotNull Instance instance, @NotNull MobStore mobStore,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers) {
        super(instance, mobStore);
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
    }

    @Override
    public void accept(@NotNull PhantazmMob mob, @NotNull EntityDamageEvent event) {
        if (!(event.getDamageType() instanceof EntityDamage entityDamage)) {
            return;
        }

        UUID uuid = entityDamage.getSource().getUuid();
        ZombiesPlayer zombiesPlayer = zombiesPlayers.get(uuid);
        if (zombiesPlayer == null) {
            return;
        }

        mob.entity().setTag(Tags.LAST_HIT_BY, uuid);
    }
}
