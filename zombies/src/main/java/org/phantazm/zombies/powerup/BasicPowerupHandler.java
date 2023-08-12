package org.phantazm.zombies.powerup;

import com.github.steanky.vector.Vec3D;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.EntityTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.powerup.action.component.PowerupActionComponent;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicateComponent;
import org.phantazm.zombies.powerup.predicate.PickupPredicateComponent;
import org.phantazm.zombies.powerup.visual.PowerupVisual;
import org.phantazm.zombies.powerup.visual.PowerupVisualComponent;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.*;
import java.util.function.Supplier;

public class BasicPowerupHandler implements PowerupHandler {
    private static final int PICKUP_CHECK_INTERVAL = 100; //check every 2 ticks for powerup pickups

    private final Supplier<? extends @NotNull ZombiesScene> scene;
    private final Map<Key, PowerupComponents> components;

    private final List<Powerup> spawnedOrActivePowerups;
    private final Collection<Powerup> powerupView;

    private long lastPickupCheck = 0L;

    public BasicPowerupHandler(@NotNull Supplier<? extends @NotNull ZombiesScene> scene,
            @NotNull Map<Key, PowerupComponents> components) {
        this.scene = Objects.requireNonNull(scene, "scene");
        this.components = Map.copyOf(components);
        this.spawnedOrActivePowerups = new ArrayList<>(16);
        this.powerupView = Collections.unmodifiableCollection(this.spawnedOrActivePowerups);
    }

    @Override
    public void tick(long time) {
        if (spawnedOrActivePowerups.isEmpty()) {
            return;
        }

        //don't check for pickups too many times
        boolean pickupCheckTick = time - lastPickupCheck >= PICKUP_CHECK_INTERVAL;
        if (pickupCheckTick) {
            lastPickupCheck = time;
        }

        for (int i = spawnedOrActivePowerups.size() - 1; i >= 0; i--) {
            Powerup powerup = spawnedOrActivePowerups.get(i);

            boolean active = powerup.active();
            boolean spawned = powerup.spawned();

            if (!active && !spawned) {
                spawnedOrActivePowerups.remove(i);
            }
            else {
                if (spawned && pickupCheckTick) {
                    maybePickup(powerup, time);
                }

                powerup.tick(time);
            }
        }
    }

    private void maybePickup(Powerup powerup, long time) {
        ZombiesScene scene = this.scene.get();

        double powerupPickupRadius = scene.getMapSettingsInfo().powerupPickupRadius();
        scene.instance().getEntityTracker()
                .nearbyEntitiesUntil(powerup.spawnLocation(), powerupPickupRadius + 2, EntityTracker.Target.PLAYERS,
                        player -> {
                            Point powerupSpawnLocation = powerup.spawnLocation();
                            Pos playerPosition = player.getPosition();
                            double horizontalDistanceSquared =
                                    Vec3D.distanceSquared(playerPosition.x(), 0, playerPosition.z(),
                                            powerupSpawnLocation.x(), 0, powerupSpawnLocation.z());
                            if (horizontalDistanceSquared > powerupPickupRadius * powerupPickupRadius) {
                                return false;
                            }

                            ZombiesPlayer zombiesPlayer = scene.getZombiesPlayers().get(player.getUuid());
                            if (zombiesPlayer != null && zombiesPlayer.canPickupPowerup(powerup)) {
                                powerup.activate(zombiesPlayer, time);
                                return true;
                            }

                            return false;
                        });
    }

    @Override
    public @NotNull Powerup spawn(@NotNull Key powerupType, double x, double y, double z) {
        PowerupComponents component = components.get(powerupType);
        if (component == null) {
            throw new IllegalArgumentException("Unknown powerup type " + powerupType);
        }

        ZombiesScene scene = this.scene.get();

        Collection<PowerupVisualComponent> visualComponents = component.visuals();
        Collection<PowerupActionComponent> actionComponents = component.actions();
        DeactivationPredicateComponent deactivationPredicateComponent = component.deactivationPredicate();
        PickupPredicateComponent pickupPredicateComponent = component.pickupPredicateComponent();

        Collection<PowerupVisual> visuals = new ArrayList<>(visualComponents.size());
        Collection<PowerupAction> actions = new ArrayList<>(actionComponents.size());

        for (PowerupVisualComponent supplier : visualComponents) {
            visuals.add(supplier.apply(scene));
        }

        for (PowerupActionComponent supplier : actionComponents) {
            actions.add(supplier.apply(scene));
        }

        Powerup newPowerup = new Powerup(powerupType, visuals, actions, deactivationPredicateComponent.apply(scene),
                pickupPredicateComponent.apply(scene), new Vec(x, y, z));
        newPowerup.spawn();

        spawnedOrActivePowerups.add(newPowerup);
        return newPowerup;
    }

    @Override
    public void assignPowerup(@NotNull LivingEntity livingEntity, @NotNull Key powerupKey) {
        PowerupComponents components = this.components.get(powerupKey);
        if (components == null) {
            return;
        }

        livingEntity.tagHandler().updateTag(Tags.POWERUP_TAG, currentValue -> {
            List<String> mutableCopy = new ArrayList<>(currentValue);
            mutableCopy.add(powerupKey.asString());
            return List.copyOf(mutableCopy);
        });

        components.powerupEffectComponent().apply(scene.get()).apply(livingEntity);
    }

    @Override
    public boolean canSpawnType(@NotNull Key powerupType) {
        return components.containsKey(powerupType);
    }

    @Override
    public void end() {
        for (Powerup powerup : spawnedOrActivePowerups) {
            powerup.deactivate();
        }

        spawnedOrActivePowerups.clear();
    }

    @Override
    public @NotNull @UnmodifiableView Collection<Powerup> spawnedOrActivePowerups() {
        return powerupView;
    }
}
