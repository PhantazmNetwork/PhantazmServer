package org.phantazm.zombies.powerup;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.Tickable;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;
import org.phantazm.zombies.powerup.predicate.PickupPredicate;
import org.phantazm.zombies.powerup.visual.PowerupVisual;

import java.util.*;

public class Powerup implements Tickable, Keyed {
    private final Key type;
    private final List<PowerupVisual> visuals;
    private final List<PowerupAction> actions;
    private final boolean[] activatedActions;
    private final DeactivationPredicate despawnPredicate;
    private final PickupPredicate pickupPredicate;
    private final Point spawnLocation;

    private boolean spawned;
    private boolean active;
    private ZombiesPlayer activatingPlayer;

    public Powerup(@NotNull Key type, @NotNull Collection<PowerupVisual> visuals,
        @NotNull Collection<PowerupAction> actions, @NotNull DeactivationPredicate despawnPredicate,
        @NotNull PickupPredicate pickupPredicate, @NotNull Point spawnLocation) {
        this.type = Objects.requireNonNull(type);
        this.visuals = List.copyOf(visuals);
        this.actions = List.copyOf(actions);
        this.activatedActions = new boolean[this.actions.size()];
        this.despawnPredicate = Objects.requireNonNull(despawnPredicate);
        this.pickupPredicate = Objects.requireNonNull(pickupPredicate);
        this.spawnLocation = Objects.requireNonNull(spawnLocation);
    }

    public void spawn() {
        if (spawned) {
            return;
        }

        for (PowerupVisual visual : visuals) {
            visual.spawn(spawnLocation.x(), spawnLocation.y(), spawnLocation.z());
        }

        despawnPredicate.activate(this, null, System.currentTimeMillis());
        spawned = true;
    }

    public boolean spawned() {
        return spawned;
    }

    public boolean active() {
        return active;
    }

    public void activate(@NotNull ZombiesPlayer player, long time) {
        if (active || !pickupPredicate.canPickup(player, this)) return;

        boolean anyActive = false;

        for (int i = 0; i < actions.size(); i++) {
            PowerupAction action = actions.get(i);
            action.activate(this, player, time);

            if (action.deactivationPredicate().shouldDeactivate(time)) {
                action.deactivate(player);
                activatedActions[i] = false;
            } else {
                anyActive = true;
                activatedActions[i] = true;
            }
        }

        if (spawned) for (PowerupVisual visual : visuals) visual.despawn();

        if (anyActive) {
            active = true;
            activatingPlayer = player;
        }

        spawned = false;
    }

    public void deactivate() {
        if (spawned) {
            for (PowerupVisual visual : visuals) {
                visual.despawn();
            }

            spawned = false;
        }

        if (!active) return;

        for (int i = 0; i < actions.size(); i++) {
            if (activatedActions[i]) actions.get(i).deactivate(activatingPlayer);
        }

        active = false;
        activatingPlayer = null;
    }

    public @NotNull Point spawnLocation() {
        return spawnLocation;
    }

    public @NotNull @Unmodifiable List<PowerupAction> actions() {
        return this.actions;
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
        for (int i = 0; i < actions.size(); i++) {
            if (!activatedActions[i]) continue;

            PowerupAction action = actions.get(i);

            DeactivationPredicate deactivationPredicate = action.deactivationPredicate();
            if (deactivationPredicate.shouldDeactivate(time)) {
                action.deactivate(activatingPlayer);
                activatedActions[i] = false;
                continue;
            } else {
                anyActive = true;
            }

            action.tick(time);
        }

        if (!anyActive) {
            active = false;
            activatingPlayer = null;
        }
    }

    @Override
    public @NotNull Key key() {
        return type;
    }
}
