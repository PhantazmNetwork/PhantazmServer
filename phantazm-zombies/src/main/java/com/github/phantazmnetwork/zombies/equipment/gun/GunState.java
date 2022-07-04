package com.github.phantazmnetwork.zombies.equipment.gun;

import org.jetbrains.annotations.NotNull;

public record GunState(long ticksSinceLastShot,
                       long ticksSinceLastFire,
                       long ticksSinceLastReload,
                       int ammo,
                       int clip,
                       boolean isMainEquipment,
                       int queuedShots) {

    public @NotNull GunState.Builder toBuilder() {
        return new Builder()
                .setTicksSinceLastShot(ticksSinceLastShot)
                .setTicksSinceLastFire(ticksSinceLastFire)
                .setTicksSinceLastReload(ticksSinceLastReload)
                .setAmmo(ammo)
                .setClip(clip)
                .setMainEquipment(isMainEquipment)
                .setQueuedShots(queuedShots);
    }

    public static class Builder {

        private long ticksSinceLastShot;

        private long ticksSinceLastFire;

        private long ticksSinceLastReload;

        private int ammo;

        private int clip;

        private boolean isMainEquipment;

        private int queuedShots;

        public long getTicksSinceLastShot() {
            return ticksSinceLastShot;
        }

        public @NotNull Builder setTicksSinceLastShot(long ticksSinceLastShot) {
            this.ticksSinceLastShot = ticksSinceLastShot;
            return this;
        }

        public long getTicksSinceLastFire() {
            return ticksSinceLastFire;
        }

        public @NotNull Builder setTicksSinceLastFire(long ticksSinceLastFire) {
            this.ticksSinceLastFire = ticksSinceLastFire;
            return this;
        }

        public long getTicksSinceLastReload() {
            return ticksSinceLastReload;
        }

        public @NotNull Builder setTicksSinceLastReload(long ticksSinceLastReload) {
            this.ticksSinceLastReload = ticksSinceLastReload;
            return this;
        }


        public int getAmmo() {
            return ammo;
        }

        public @NotNull Builder setAmmo(int ammo) {
            this.ammo = ammo;
            return this;
        }

        public int getClip() {
            return clip;
        }

        public @NotNull Builder setClip(int clip) {
            this.clip = clip;
            return this;
        }

        public boolean isMainEquipment() {
            return isMainEquipment;
        }

        public @NotNull Builder setMainEquipment(boolean isMainWeapon) {
            this.isMainEquipment = isMainWeapon;
            return this;
        }

        public int getQueuedShots() {
            return queuedShots;
        }

        public @NotNull Builder setQueuedShots(int queuedShots) {
            this.queuedShots = queuedShots;
            return this;
        }

        public @NotNull GunState build() {
            return new GunState(ticksSinceLastShot, ticksSinceLastFire, ticksSinceLastReload, ammo, clip,
                    isMainEquipment, queuedShots);
        }

    }

}
