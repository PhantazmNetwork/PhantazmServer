package org.phantazm.zombies.listener;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.entity.damage.EntityProjectileDamage;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.event.PlayerDeathEvent;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.KnockedPlayerStateContext;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PlayerDamageEventListener extends ZombiesPlayerEventListener<EntityDamageEvent> {

    private final MapObjects mapObjects;

    public PlayerDamageEventListener(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, @NotNull MapObjects mapObjects) {
        super(instance, zombiesPlayers);
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
    }

    @Override
    protected void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull EntityDamageEvent event) {
        if (!zombiesPlayer.canTakeDamage()) {
            event.setCancelled(true);
            return;
        }

        if (event.getDamage() < event.getEntity().getHealth()) {
            return;
        }

        event.setCancelled(true);

        Optional<Player> playerOptional = zombiesPlayer.getPlayer();
        if (playerOptional.isPresent()) {
            PlayerDeathEvent deathEvent =
                    new PlayerDeathEvent(playerOptional.get(), zombiesPlayer, event.getDamageType());
            EventDispatcher.call(deathEvent);

            if (deathEvent.isCancelled()) {
                return;
            }
        }

        zombiesPlayer.getPlayer().ifPresent(player -> player.setHealth(player.getMaxHealth()));

        Pos deathPosition = event.getEntity().getPosition();
        Component killer = getKiller(event);
        Component roomName = getRoomName(deathPosition);

        zombiesPlayer.setState(ZombiesPlayerStateKeys.KNOCKED,
                new KnockedPlayerStateContext(event.getInstance(), deathPosition, roomName, killer));
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
        return mapObjects.roomTracker().atPoint(deathPosition).map(room -> room.getRoomInfo().displayName())
                .orElse(Component.text("an unknown room"));
    }

}
