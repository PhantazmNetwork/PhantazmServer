package org.phantazm.zombies.listener;

import net.kyori.adventure.text.Component;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.PhysicsUtils;
import org.phantazm.core.player.PlayerView;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.Flags;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.event.player.ZombiesPlayerDeathEvent;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.player.state.context.KnockedPlayerStateContext;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.phantazm.zombies.stage.StageKeys;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class PlayerDamageEventListener extends ZombiesPlayerEventListener<EntityDamageEvent> {
    private final MapObjects mapObjects;
    private final MapSettingsInfo mapSettingsInfo;

    public PlayerDamageEventListener(@NotNull Instance instance,
        @NotNull Map<PlayerView, ZombiesPlayer> zombiesPlayers, @NotNull MapObjects mapObjects,
        @NotNull MapSettingsInfo mapSettingsInfo, @NotNull Supplier<ZombiesScene> scene) {
        super(instance, zombiesPlayers, scene);
        this.mapObjects = Objects.requireNonNull(mapObjects);
        this.mapSettingsInfo = Objects.requireNonNull(mapSettingsInfo);
    }

    @Override
    protected void accept(@NotNull ZombiesScene scene, @NotNull ZombiesPlayer zombiesPlayer, @NotNull EntityDamageEvent event) {
        if (!zombiesPlayer.canTakeDamage()) {
            event.setCancelled(true);
            return;
        }

        if (zombiesPlayer.flags().hasFlag(Flags.GODMODE)) {
            event.setCancelled(true);
            return;
        }

        if (event.getActualAmount() < event.getEntity().getHealth()) {
            return;
        }

        event.setCancelled(true);

        Optional<Player> playerOptional = zombiesPlayer.getPlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            ZombiesPlayerDeathEvent deathEvent = new ZombiesPlayerDeathEvent(player, zombiesPlayer, event.getDamage());
            EventDispatcher.call(deathEvent);

            if (deathEvent.isCancelled()) {
                return;
            }

            player.setHealth(player.getMaxHealth() * mapSettingsInfo.reviveHealthFactor());
        }

        Pos deathPosition = event.getEntity().getPosition();
        // +2 just to sidestep any block border issues
        double heightAboveBottom = deathPosition.y() - event.getInstance().getDimensionType().getMinY() + 2;
        PhysicsResult collision = CollisionUtils.handlePhysics(event.getEntity(), new Vec(0, -heightAboveBottom, 0));
        if (PhysicsUtils.hasCollision(collision)) {
            deathPosition = collision.newPosition();
            event.getEntity().teleport(deathPosition).join();
        }

        Component killer = getKiller(event);
        Component roomName = getRoomName(deathPosition);

        zombiesPlayer.setState(ZombiesPlayerStateKeys.KNOCKED,
            new KnockedPlayerStateContext(event.getInstance(), deathPosition, roomName, killer));

        boolean anyAlive = false;
        for (ZombiesPlayer player : super.zombiesPlayers.values()) {
            if (player.isAlive()) {
                anyAlive = true;
                break;
            }
        }

        if (!anyAlive) {
            scene.stageTransition().setCurrentStage(StageKeys.END);
        }
    }

    private Component getEntityName(@NotNull Entity entity) {
        if (entity instanceof Mob hitMob) {
            return hitMob.name();
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