package com.github.phantazmnetwork.zombies.powerup;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public class BasicPowerupHandler implements PowerupHandler {
    private final Map<Key, ConfigNode> powerupData;
    private final ContextManager contextManager;
    private final DependencyProvider mapProvider;

    private final List<Powerup> activePowerups;
    private final Collection<Powerup> powerupView;


    public BasicPowerupHandler(@NotNull Map<Key, ConfigNode> powerupData, @NotNull ContextManager contextManager,
            @NotNull DependencyProvider mapProvider) {
        this.powerupData = Map.copyOf(powerupData);
        this.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        this.mapProvider = Objects.requireNonNull(mapProvider, "mapDependencyProvider");
        this.activePowerups = new ArrayList<>();
        this.powerupView = Collections.unmodifiableCollection(this.activePowerups);
    }

    @Override
    public @NotNull Powerup spawn(@NotNull Key powerupType, double x, double y, double z) {
        ConfigNode data = powerupData.get(powerupType);
        if (data == null) {
            throw new IllegalArgumentException("Unrecognized powerup type '" + powerupType + "'");
        }

        ElementContext context = contextManager.makeContext(data);
        Collection<PowerupVisual> visuals = context.provideCollection("visuals", mapProvider, false);
        Collection<PowerupAction> actions = context.provideCollection("actions", mapProvider, false);
        DeactivationPredicate predicate = context.provide("deactivationPredicate", mapProvider, false);


        Powerup powerup = new Powerup(powerupType, visuals, actions, predicate, new Vec(x, y, z));
        powerup.spawn();

        activePowerups.add(powerup);
        return powerup;
    }

    @Override
    public @NotNull @UnmodifiableView Collection<Powerup> spawnedOrActivePowerups() {
        return powerupView;
    }

    @Override
    public void tick(long time) {
        if (activePowerups.isEmpty()) {
            return;
        }

        Iterator<Powerup> iterator = activePowerups.iterator();
        while (iterator.hasNext()) {
            Powerup powerup = iterator.next();
            if (!powerup.spawned() && !powerup.active()) {
                iterator.remove();
            }

            powerup.tick(time);
        }
    }
}
