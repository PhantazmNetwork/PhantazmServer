package org.phantazm.zombies.equipment.gun2;

public class GunState {

    private boolean held;

    private int ammo;

    private int clip;

    private int queuedShots;

    private long ticksSinceLastShot;

    private long ticksSinceLastFire;

    private long ticksSinceLastReload;

    private boolean reloadComplete;

    public GunState() {

    }

    public boolean isHeld() {
        return held;
    }

    public void setHeld(boolean held) {
        this.held = held;
    }

    public int getAmmo() {
        return ammo;
    }

    public void setAmmo(int ammo) {
        this.ammo = ammo;
    }

    public int getClip() {
        return clip;
    }

    public void setClip(int clip) {
        this.clip = clip;
    }

    public int getQueuedShots() {
        return queuedShots;
    }

    public void setQueuedShots(int queuedShots) {
        this.queuedShots = queuedShots;
    }

    public long getTicksSinceLastShot() {
        return ticksSinceLastShot;
    }

    public void setTicksSinceLastShot(long ticksSinceLastShot) {
        this.ticksSinceLastShot = ticksSinceLastShot;
    }

    public long getTicksSinceLastFire() {
        return ticksSinceLastFire;
    }

    public void setTicksSinceLastFire(long ticksSinceLastFire) {
        this.ticksSinceLastFire = ticksSinceLastFire;
    }

    public long getTicksSinceLastReload() {
        return ticksSinceLastReload;
    }

    public void setTicksSinceLastReload(long ticksSinceLastReload) {
        this.ticksSinceLastReload = ticksSinceLastReload;
    }

    public boolean isReloadComplete() {
        return reloadComplete;
    }

    public void setReloadComplete(boolean reloadComplete) {
        this.reloadComplete = reloadComplete;
    }
}
