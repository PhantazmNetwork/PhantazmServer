package com.github.phantazmnetwork.zombies.powerup;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.Supplier;

public class BasicPowerupHandler implements PowerupHandler {
    private final Map<Key, PowerupComponents> components;

    private final List<Powerup> spawnedOrActivePowerups;
    private final Collection<Powerup> powerupView;

    public BasicPowerupHandler(@NotNull Map<Key, PowerupComponents> components) {
        this.components = Map.copyOf(components);
        this.spawnedOrActivePowerups = new ArrayList<>(16);
        this.powerupView = Collections.unmodifiableCollection(this.spawnedOrActivePowerups);
    }

    @Override
    public void tick(long time) {
        if (spawnedOrActivePowerups.isEmpty()) {
            return;
        }

        Iterator<Powerup> powerupIterator = spawnedOrActivePowerups.listIterator();
        while (powerupIterator.hasNext()) {
            Powerup next = powerupIterator.next();

            if (!next.active() && !next.spawned()) {
                powerupIterator.remove();
            }

            next.tick(time);
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
