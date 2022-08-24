package com.github.phantazmnetwork.zombies.game.player;

public class ZombiesPlayerMeta {

    private boolean inGame = false;

    private boolean isCrouching = false;

    private boolean canRevive = false;

    private boolean isReviving = false;

    private boolean canTriggerSLA = false;

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public boolean isCrouching() {
        return isCrouching;
    }

    public void setCrouching(boolean crouching) {
        isCrouching = crouching;
    }

    public boolean isCanRevive() {
        return canRevive;
    }

    public void setCanRevive(boolean canRevive) {
        this.canRevive = canRevive;
    }

    public boolean isReviving() {
        return isReviving;
    }

    public void setReviving(boolean reviving) {
        isReviving = reviving;
    }

    public boolean isCanTriggerSLA() {
        return canTriggerSLA;
    }

    public void setCanTriggerSLA(boolean canTriggerSLA) {
        this.canTriggerSLA = canTriggerSLA;
    }
}
