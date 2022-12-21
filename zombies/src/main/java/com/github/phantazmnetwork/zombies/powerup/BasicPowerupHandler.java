package com.github.phantazmnetwork.zombies.powerup;

import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.Supplier;

public class BasicPowerupHandler implements PowerupHandler {
    private static final int PICKUP_CHECK_INTERVAL = 100; //check every 2 ticks for powerup pickups

    private final Instance instance;
    private final Map<Key, PowerupComponents> components;
    private final Map<? super UUID, ? extends ZombiesPlayer> playerMap;
    private final double powerupPickupRadius;

    private final List<Powerup> spawnedOrActivePowerups;
    private final Collection<Powerup> powerupView;

    private long lastPickupCheck = 0L;

    public BasicPowerupHandler(@NotNull Instance instance, @NotNull Map<Key, PowerupComponents> components,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> playerMap, double powerupPickupRadius) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.components = Map.copyOf(components);
        this.spawnedOrActivePowerups = new ArrayList<>(16);
        this.powerupView = Collections.unmodifiableCollection(this.spawnedOrActivePowerups);
        this.playerMap = Objects.requireNonNull(playerMap);
        this.powerupPickupRadius = powerupPickupRadius;
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
                            if (zombiesPlayer != null && zombiesPlayer.isAlive()) {
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
            throw new IllegalArgumentException("Powerup type '" + powerupType + "' unknown");
        }

        Collection<Supplier<PowerupVisual>> visualSuppliers = component.visuals();
        Collection<Supplier<PowerupAction>> actionSuppliers = component.actions();
        Supplier<DeactivationPredicate> deactivationPredicateSupplier = component.deactivationPredicate();

        Collection<PowerupVisual> visuals = new ArrayList<>(visualSuppliers.size());
        Collection<PowerupAction> actions = new ArrayList<>(actionSuppliers.size());

        for (Supplier<PowerupVisual> supplier : visualSuppliers) {
            visuals.add(supplier.get());
        }

        for (Supplier<PowerupAction> supplier : actionSuppliers) {
            actions.add(supplier.get());
        }

        Powerup newPowerup =
                new Powerup(powerupType, visuals, actions, deactivationPredicateSupplier.get(), new Vec(x, y, z));
        newPowerup.spawn();

        spawnedOrActivePowerups.add(newPowerup);
        return newPowerup;
    }

    @Override
    public @NotNull @UnmodifiableView Collection<Powerup> spawnedOrActivePowerups() {
        return powerupView;
    }
}
