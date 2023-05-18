package org.phantazm.zombies.equipment.gun;

import org.jetbrains.annotations.NotNull;

/**
 * The state of a gun. This class is immutable and may be updated with the {@link Builder}.
 * If necessary, {@link GunState} can be stored since it will not mutate with time.
 *
 * @param ticksSinceLastShot   The number of ticks since the last shot began
 * @param ticksSinceLastFire   The number of ticks since the last gun's last fire
 * @param ticksSinceLastReload The number of ticks since the last gun's last reload
 * @param ammo                 The amount of ammo left in the gun
 * @param clip                 The amount of ammo in the gun's clip
 * @param isMainEquipment      Whether the gun is the main equipment
 * @param queuedShots          The number of shots queued to be fired
 */
public record GunState(long ticksSinceLastShot,
                       long ticksSinceLastFire,
                       long ticksSinceLastReload,
                       int ammo,
                       int clip,
                       boolean isMainEquipment,
                       int queuedShots) {

    /**
     * Converts the {@link GunState} to a {@link Builder}.
     *
     * @return A {@link Builder} representation of the {@link GunState}
     */
    public @NotNull GunState.Builder toBuilder() {
        return new Builder().setTicksSinceLastShot(ticksSinceLastShot).setTicksSinceLastFire(ticksSinceLastFire)
                .setTicksSinceLastReload(ticksSinceLastReload).setAmmo(ammo).setClip(clip)
                .setMainEquipment(isMainEquipment).setQueuedShots(queuedShots);
    }

    /**
     * Represents a mutable builder representation of {@link GunState}.
     */
    public static class Builder {

        private long ticksSinceLastShot;

        private long ticksSinceLastFire;

        private long ticksSinceLastReload;

        private int ammo;

        private int clip;

        private boolean isMainEquipment;

        private int queuedShots;

        /**
         * Gets the number of ticks since the last shot began.
         *
         * @return The number of ticks since the last shot began
         */
        public long getTicksSinceLastShot() {
            return ticksSinceLastShot;
        }

        /**
         * Sets the number of ticks since the last shot began.
         *
         * @param ticksSinceLastShot The number of ticks since the last shot began
         * @return This {@link Builder}
         */
        public @NotNull Builder setTicksSinceLastShot(long ticksSinceLastShot) {
            this.ticksSinceLastShot = ticksSinceLastShot;
            return this;
        }

        /**
         * Gets the number of ticks since the last gun's last fire.
         *
         * @return The number of ticks since the last gun's last fire
         */
        public long getTicksSinceLastFire() {
            return ticksSinceLastFire;
        }

        /**
         * Sets the number of ticks since the last gun's last fire.
         *
         * @param ticksSinceLastFire The number of ticks since the last gun's last fire
         * @return This {@link Builder}
         */
        public @NotNull Builder setTicksSinceLastFire(long ticksSinceLastFire) {
            this.ticksSinceLastFire = ticksSinceLastFire;
            return this;
        }

        /**
         * Gets the number of ticks since the last gun's last reload.
         *
         * @return The number of ticks since the last gun's last reload
         */
        public long getTicksSinceLastReload() {
            return ticksSinceLastReload;
        }

        /**
         * Sets the number of ticks since the last gun's last reload.
         *
         * @param ticksSinceLastReload The number of ticks since the last gun's last reload
         * @return This {@link Builder}
         */
        public @NotNull Builder setTicksSinceLastReload(long ticksSinceLastReload) {
            this.ticksSinceLastReload = ticksSinceLastReload;
            return this;
        }

        /**
         * Gets the amount of ammo left in the gun.
         *
         * @return The amount of ammo left in the gun
         */
        public int getAmmo() {
            return ammo;
        }

        /**
         * Sets the amount of ammo left in the gun.
         *
         * @param ammo The amount of ammo left in the gun
         * @return This {@link Builder}
         */
        public @NotNull Builder setAmmo(int ammo) {
            this.ammo = ammo;
            return this;
        }

        /**
         * Gets the amount of ammo in the gun's clip.
         *
         * @return The amount of ammo in the gun's clip
         */
        public int getClip() {
            return clip;
        }

        /**
         * Sets the amount of ammo in the gun's clip.
         *
         * @param clip The amount of ammo in the gun's clip
         * @return This {@link Builder}
         */
        public @NotNull Builder setClip(int clip) {
            this.clip = clip;
            return this;
        }

        /**
         * Gets whether the gun is the main equipment.
         *
         * @return Whether the gun is the main equipment
         */
        public boolean isMainEquipment() {
            return isMainEquipment;
        }

        /**
         * Sets whether the gun is the main equipment.
         *
         * @param isMainWeapon Whether the gun is the main equipment
         * @return This {@link Builder}
         */
        public @NotNull Builder setMainEquipment(boolean isMainWeapon) {
            this.isMainEquipment = isMainWeapon;
            return this;
        }

        /**
         * Gets the number of shots queued to be fired.
         *
         * @return The number of shots queued to be fired
         */
        public int getQueuedShots() {
            return queuedShots;
        }

        /**
         * Sets the number of shots queued to be fired.
         *
         * @param queuedShots The number of shots queued to be fired
         * @return This {@link Builder}
         */
        public @NotNull Builder setQueuedShots(int queuedShots) {
            this.queuedShots = queuedShots;
            return this;
        }

        /**
         * Builds a {@link GunState} from the current {@link Builder}.
         *
         * @return A {@link GunState} representation of the {@link Builder}
         */
        public @NotNull GunState build() {
            return new GunState(ticksSinceLastShot, ticksSinceLastFire, ticksSinceLastReload, ammo, clip,
                    isMainEquipment, queuedShots);
        }

    }

}
