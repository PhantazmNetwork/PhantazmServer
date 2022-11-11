package com.github.phantazmnetwork.zombies.game.powerup;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class Powerup implements Tickable {
    private final PowerupVisual[] visuals;
    private final PowerupAction[] actions;
    private final DeactivationPredicate despawnPredicate;
    private final Vec spawnLocation;

    private boolean spawned;
    private boolean active;

    public Powerup(@NotNull Collection<PowerupVisual> visuals, @NotNull Collection<PowerupAction> actions,
            @NotNull DeactivationPredicate despawnPredicate, @NotNull Vec spawnLocation) {
        this.visuals = visuals.toArray(PowerupVisual[]::new);
        this.actions = actions.toArray(PowerupAction[]::new);
        this.despawnPredicate = Objects.requireNonNull(despawnPredicate, "despawnPredicate");
        this.spawnLocation = Objects.requireNonNull(spawnLocation, "spawnLocation");
    }

    public void spawn() {
        for (PowerupVisual visual : visuals) {
            visual.spawn(spawnLocation.x(), spawnLocation.y(), spawnLocation.z());
        }

        spawned = true;
    }

    public void activate(@NotNull ZombiesPlayer player, long time) {
        if (active) {
            return;
        }

        boolean anyActive = false;
        for (int i = 0; i < actions.length; i++) {
            PowerupAction action = actions[i];
            if (action == null) {
                continue;
            }

            action.activate(player, time);

            if (action.deactivationPredicate().shouldDeactivate(time)) {
                //deactivate immediately if necessary
                action.deactivate();
                actions[i] = null;
            }
            else {
                anyActive = true;
            }
        }

        if (anyActive) {
            active = true;
        }

        spawned = false;
    }

    public @NotNull Vec spawnLocation() {
        return spawnLocation;
    }

    @Override
    public void tick(long time) {
        if (spawned) {
            if (despawnPredicate.shouldDeactivate(time)) {
                for (PowerupVisual visual : visuals) {
                    visual.despawn();
                }

                spawned = false;
                return;
            }

            for (PowerupVisual visual : visuals) {
                visual.tick(time);
            }
        }

        if (!active) {
            return;
        }

        boolean anyActive = false;
        for (int i = 0; i < actions.length; i++) {
            PowerupAction action = actions[i];
            if (action == null) {
                continue;
            }

            DeactivationPredicate deactivationPredicate = action.deactivationPredicate();
            if (deactivationPredicate.shouldDeactivate(time)) {
                action.deactivate();
                actions[i] = null;
            }
            else {
                anyActive = true;
            }
        }

        if (!anyActive) {
            active = false;
        }
    }
}
