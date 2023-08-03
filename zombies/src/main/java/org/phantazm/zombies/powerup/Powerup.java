package org.phantazm.zombies.powerup;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.powerup.predicate.DeactivationPredicate;
import org.phantazm.zombies.powerup.visual.PowerupVisual;

import java.util.*;

public class Powerup implements Tickable, Keyed {
    private final Key type;
    private final List<PowerupVisual> visuals;
    private final List<PowerupAction> actions;
    private final DeactivationPredicate despawnPredicate;
    private final Point spawnLocation;

    private boolean spawned;
    private boolean active;
    private ZombiesPlayer activatingPlayer;

    public Powerup(@NotNull Key type, @NotNull Collection<PowerupVisual> visuals,
            @NotNull Collection<PowerupAction> actions, @NotNull DeactivationPredicate despawnPredicate,
            @NotNull Point spawnLocation) {
        this.type = Objects.requireNonNull(type, "type");
        this.visuals = new ArrayList<>(visuals);
        this.actions = new ArrayList<>(actions);
        this.despawnPredicate = Objects.requireNonNull(despawnPredicate, "despawnPredicate");
        this.spawnLocation = Objects.requireNonNull(spawnLocation, "spawnLocation");

        Collections.reverse(this.actions);
    }

    public void spawn() {
        if (spawned) {
            return;
        }

        for (PowerupVisual visual : visuals) {
            visual.spawn(spawnLocation.x(), spawnLocation.y(), spawnLocation.z());
        }

        despawnPredicate.activate(System.currentTimeMillis());
        spawned = true;
    }

    public boolean spawned() {
        return spawned;
    }

    public boolean active() {
        return active;
    }

    public void activate(@NotNull ZombiesPlayer player, long time) {
        if (active) {
            return;
        }

        boolean anyActive = false;
        for (int i = actions.size() - 1; i >= 0; i--) {
            PowerupAction action = actions.get(i);

            if (!action.activate(this, player, time)) {
                return;
            }

            if (action.deactivationPredicate().shouldDeactivate(time)) {
                action.deactivate(player);
                actions.remove(i);
            }
            else {
                anyActive = true;
            }
        }

        for (PowerupVisual visual : visuals) {
            visual.despawn();
        }

        if (anyActive) {
            active = true;
            activatingPlayer = player;
        }

        spawned = false;
    }

    public @NotNull Point spawnLocation() {
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
        for (int i = actions.size() - 1; i >= 0; i--) {
            PowerupAction action = actions.get(i);

            DeactivationPredicate deactivationPredicate = action.deactivationPredicate();
            if (deactivationPredicate.shouldDeactivate(time)) {
                action.deactivate(activatingPlayer);
                actions.remove(i);
            }
            else {
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
