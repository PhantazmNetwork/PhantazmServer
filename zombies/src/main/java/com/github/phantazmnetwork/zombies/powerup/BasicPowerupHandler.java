package com.github.phantazmnetwork.zombies.powerup;

import com.github.phantazmnetwork.zombies.map.MapSettingsInfo;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.player.state.ZombiesPlayerStateKeys;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.Supplier;

public class BasicPowerupHandler implements PowerupHandler {
    private final Map<Key, PowerupComponents> components;
    private final Map<UUID, ZombiesPlayer> playerMap;
    private final double powerupPickupRadiusSquared;

    private final List<Powerup> spawnedOrActivePowerups;
    private final Collection<Powerup> powerupView;

    public BasicPowerupHandler(@NotNull Map<Key, PowerupComponents> components,
            @NotNull Map<UUID, ZombiesPlayer> playerMap, double powerupPickupRadius) {
        this.components = Map.copyOf(components);
        this.spawnedOrActivePowerups = new ArrayList<>(16);
        this.powerupView = Collections.unmodifiableCollection(this.spawnedOrActivePowerups);
        this.playerMap = Objects.requireNonNull(playerMap);
        this.powerupPickupRadiusSquared = powerupPickupRadius * powerupPickupRadius;
    }

    @Override
    public void tick(long time) {
        if (spawnedOrActivePowerups.isEmpty()) {
            return;
        }

        for (int i = spawnedOrActivePowerups.size() - 1; i >= 0; i--) {
            Powerup powerup = spawnedOrActivePowerups.get(i);

            boolean active = powerup.active();
            boolean spawned = powerup.spawned();

            if (!active && !spawned) {
                spawnedOrActivePowerups.remove(i);
            }
            else {
                if (spawned) {
                    maybePickup(powerup, time);
                }

                powerup.tick(time);
            }
        }
    }

    private void maybePickup(Powerup powerup, long time) {
        for (ZombiesPlayer player : playerMap.values()) {
            if (player.isState(ZombiesPlayerStateKeys.ALIVE)) {
                Optional<Player> playerOptional = player.getPlayer();
                if (playerOptional.isPresent()) {
                    Player actualPlayer = playerOptional.get();

                    if (actualPlayer.getPosition().distanceSquared(powerup.spawnLocation()) <=
                            powerupPickupRadiusSquared) {
                        powerup.activate(player, time);
                        return;
                    }
                }
            }
        }
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
