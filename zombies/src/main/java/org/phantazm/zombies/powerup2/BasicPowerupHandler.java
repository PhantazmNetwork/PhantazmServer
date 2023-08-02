package org.phantazm.zombies.powerup2;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup2.action.PowerupAction;
import org.phantazm.zombies.powerup2.action.PowerupActionComponent;
import org.phantazm.zombies.powerup2.predicate.DeactivationPredicateComponent;
import org.phantazm.zombies.powerup2.visual.PowerupVisual;
import org.phantazm.zombies.powerup2.visual.PowerupVisualComponent;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.*;

public class BasicPowerupHandler implements PowerupHandler {
    private static final int PICKUP_CHECK_INTERVAL = 100; //check every 2 ticks for powerup pickups

    private final ZombiesScene scene;
    private final Instance instance;
    private final Map<Key, PowerupComponents> components;
    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;
    private final double powerupPickupRadius;

    private final List<Powerup> spawnedOrActivePowerups;
    private final Collection<Powerup> powerupView;

    private long lastPickupCheck = 0L;

    public BasicPowerupHandler(@NotNull ZombiesScene scene, @NotNull Map<Key, PowerupComponents> components) {
        this.scene = Objects.requireNonNull(scene, "scene");
        this.instance = scene.instance();
        this.components = Map.copyOf(components);
        this.spawnedOrActivePowerups = new ArrayList<>(16);
        this.powerupView = Collections.unmodifiableCollection(this.spawnedOrActivePowerups);
        this.playerMap = scene.getZombiesPlayers();
        this.powerupPickupRadius = scene.getMapSettingsInfo().powerupPickupRadius();
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
        instance.getEntityTracker()
                .nearbyEntitiesUntil(powerup.spawnLocation(), powerupPickupRadius, EntityTracker.Target.PLAYERS,
                        player -> {
                            ZombiesPlayer zombiesPlayer = playerMap.get(player.getUuid());
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

        Collection<PowerupVisualComponent> visualComponents = component.visuals();
        Collection<PowerupActionComponent> actionComponents = component.actions();
        DeactivationPredicateComponent deactivationPredicateComponent = component.deactivationPredicate();

        Collection<PowerupVisual> visuals = new ArrayList<>(visualComponents.size());
        Collection<PowerupAction> actions = new ArrayList<>(actionComponents.size());

        for (PowerupVisualComponent supplier : visualComponents) {
            visuals.add(supplier.apply(scene));
        }

        for (PowerupActionComponent supplier : actionComponents) {
            actions.add(supplier.apply(scene));
        }

        Powerup newPowerup = new Powerup(powerupType, visuals, actions, deactivationPredicateComponent.apply(scene),
                new Vec(x, y, z));
        newPowerup.spawn();

        spawnedOrActivePowerups.add(newPowerup);
        return newPowerup;
    }

    @Override
    public boolean typeExists(@NotNull Key powerupType) {
        return components.containsKey(powerupType);
    }

    @Override
    public @NotNull @UnmodifiableView Collection<Powerup> spawnedOrActivePowerups() {
        return powerupView;
    }
}
