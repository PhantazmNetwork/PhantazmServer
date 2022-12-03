package com.github.phantazmnetwork.zombies.listener;

import com.github.phantazmnetwork.core.VecUtils;
import com.github.phantazmnetwork.zombies.map.objects.MapObjects;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.player.state.ZombiesPlayerStateKeys;
import com.github.phantazmnetwork.zombies.player.state.context.KnockedPlayerStateContext;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.entity.damage.EntityProjectileDamage;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerDeathEventListener extends ZombiesPlayerEventListener<EntityDamageEvent> {

    private final MapObjects mapObjects;

    public PlayerDeathEventListener(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, @NotNull MapObjects mapObjects) {
        super(instance, zombiesPlayers);
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
    }

    @Override
    protected void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull EntityDamageEvent event) {
        if (!zombiesPlayer.isAlive() || event.getDamage() < event.getEntity().getHealth()) {
            return;
        }

        event.setCancelled(true);

        Pos deathPosition = event.getEntity().getPosition();
        Component killer = getKiller(event);
        Component roomName = getRoomName(deathPosition);

        zombiesPlayer.setState(ZombiesPlayerStateKeys.KNOCKED,
                new KnockedPlayerStateContext(deathPosition, roomName, killer));
    }

    private Component getEntityName(@NotNull Entity entity) {
        Component message = entity.getCustomName();
        if (message == null) {
            message = Component.translatable(entity.getEntityType().registry().translationKey());
        }

        return message;
    }

    private Component getKiller(@NotNull EntityDamageEvent event) {
        DamageType damageType = event.getDamageType();
        if (damageType instanceof EntityDamage entityDamage) {
            return getEntityName(entityDamage.getSource());
        }
        else if (damageType instanceof EntityProjectileDamage projectileDamage) {
            Entity shooter = projectileDamage.getShooter();
            return getEntityName(Objects.requireNonNullElseGet(shooter, projectileDamage::getProjectile));
        }

        return null;
    }

    private Component getRoomName(@NotNull Pos deathPosition) {
        return mapObjects.roomAt(deathPosition).map(room -> room.getRoomInfo().displayName()).orElse(null);
    }

}
