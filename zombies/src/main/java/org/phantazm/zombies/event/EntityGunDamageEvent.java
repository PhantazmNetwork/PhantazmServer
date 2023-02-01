package org.phantazm.zombies.event;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;

import java.util.Objects;

public class EntityGunDamageEvent implements EntityInstanceEvent, CancellableEvent {
    private final Gun gun;
    private final Entity damagedEntity;
    private final Entity shooter;
    private final boolean isHeadshot;
    private final boolean isInstakill;
    private final float damage;

    private boolean cancelled;

    public EntityGunDamageEvent(@NotNull Gun gun, @NotNull Entity damagedEntity, @NotNull Entity shooter,
            boolean isHeadshot, boolean isInstakill, float damage) {
        this.gun = Objects.requireNonNull(gun, "gun");
        this.damagedEntity = Objects.requireNonNull(damagedEntity, "damagedEntity");
        this.shooter = Objects.requireNonNull(shooter, "shooter");
        this.isHeadshot = isHeadshot;
        this.isInstakill = isInstakill;
        this.damage = damage;
    }

    public @NotNull Gun gun() {
        return gun;
    }

    @Override
    public @NotNull Entity getEntity() {
        return damagedEntity;
    }

    public @NotNull Entity getShooter() {
        return shooter;
    }

    public boolean isHeadshot() {
        return isHeadshot;
    }

    public boolean isInstakill() {
        return isInstakill;
    }

    public float getDamage() {
        return damage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
