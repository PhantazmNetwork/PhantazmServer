package com.github.phantazmnetwork.zombies.equipment.gun.state;

import org.jetbrains.annotations.NotNull;

public record GunState(long ticksSinceLastShot,
                       long ticksSinceLastReload,
                       int ammo,
                       int clip,
                       boolean isMainEquipment) {

    public @NotNull GunState.Builder toBuilder() {
        return new Builder()
                .setTicksSinceLastShot(ticksSinceLastShot)
                .setTicksSinceLastReload(ticksSinceLastReload)
                .setAmmo(ammo)
                .setClip(clip)
                .setMainEquipment(isMainEquipment);
    }

    public static class Builder {

        private long ticksSinceLastShot;

        private long ticksSinceLastReload;

        private int ammo;

        private int clip;

        private boolean isMainEquipment;

        public long getTicksSinceLastShot() {
            return ticksSinceLastShot;
        }

        public @NotNull Builder setTicksSinceLastShot(long ticksSinceLastShot) {
            this.ticksSinceLastShot = ticksSinceLastShot;
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

        public @NotNull GunState build() {
            return new GunState(ticksSinceLastShot, ticksSinceLastReload, ammo, clip, isMainEquipment);
        }

    }

}
