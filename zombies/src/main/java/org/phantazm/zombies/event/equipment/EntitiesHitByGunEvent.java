package org.phantazm.zombies.event.equipment;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.event.trait.GunEvent;

import java.util.Collection;
import java.util.Objects;

/**
 * Event raised when one or more entities are hit by a gun's shot. Can be cancelled to prevent the gun's handlers from
 * being run on any of the targets.
 */
public class EntitiesHitByGunEvent implements CancellableEvent, GunEvent {
    private final Gun gun;
    private final Collection<GunHit> targets;
    private final Entity shooter;

    private boolean cancelled;

    public EntitiesHitByGunEvent(@NotNull Gun gun, @NotNull Collection<GunHit> targets, @NotNull Entity shooter) {
        this.gun = Objects.requireNonNull(gun);
        this.targets = Objects.requireNonNull(targets);
        this.shooter = Objects.requireNonNull(shooter);
    }

    public @NotNull Gun gun() {
        return gun;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public @NotNull Collection<GunHit> targets() {
        return targets;
    }

    public @NotNull Entity getShooter() {
        return shooter;
    }
}
