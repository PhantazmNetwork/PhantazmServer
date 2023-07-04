package org.phantazm.zombies.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.entity.damage.EntityProjectileDamage;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.Flags;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.damage.ZombiesDamageType;
import org.phantazm.zombies.event.ZombiesPlayerDeathEvent;
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

        if (zombiesPlayer.flags().hasFlag(Flags.GODMODE)) {
            event.setCancelled(true);
            return;
        }

        if (event.getDamage() < event.getEntity().getHealth()) {
            return;
        }

        event.setCancelled(true);

        Optional<Player> playerOptional = zombiesPlayer.getPlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            ZombiesPlayerDeathEvent deathEvent =
                    new ZombiesPlayerDeathEvent(player, zombiesPlayer, event.getDamageType());
            EventDispatcher.call(deathEvent);

            if (deathEvent.isCancelled()) {
                return;
            }

            player.setHealth(player.getMaxHealth());
        }

        Pos deathPosition = event.getEntity().getPosition();
        Component killer = getKiller(event);
        Component roomName = getRoomName(deathPosition);

        zombiesPlayer.setState(ZombiesPlayerStateKeys.KNOCKED,
                new KnockedPlayerStateContext(event.getInstance(), deathPosition, roomName, killer));
    }

    private Component getEntityName(@NotNull Entity entity) {
        PhantazmMob mob = mapObjects.module().mobStore().getMob(entity.getUuid());
        Optional<Component> displayNameOptional;
        if (mob != null && (displayNameOptional = mob.model().getDisplayName()).isPresent()) {
            return displayNameOptional.get();
        }

        Component message = entity.getCustomName();
        if (message == null) {
            message = Component.translatable(entity.getEntityType().registry().translationKey());
        }

        return message;
    }

    private Component getKiller(@NotNull EntityDamageEvent event) {
        Damage damage = event.getDamage();
        if (damage.getAttacker() != null) {
            return getEntityName(damage.getAttacker());
        } else if (damage.getSource() != null) {
            return getEntityName(damage.getSource());
        } else if (damage.hasTag(Tags.DAMAGE_NAME)) {
            return damage.tagHandler().getTag(Tags.DAMAGE_NAME);
        }

        return null;
    }

    private Component getRoomName(@NotNull Pos deathPosition) {
        return mapObjects.roomTracker().atPoint(deathPosition).map(room -> room.getRoomInfo().displayName())
                .orElse(Component.text("an unknown room"));
    }

}
