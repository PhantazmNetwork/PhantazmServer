package org.phantazm.zombies.player;

import org.jetbrains.annotations.Nullable;
import org.phantazm.zombies.corpse.Corpse;

import java.util.Optional;

//TODO: rename
public class ZombiesPlayerMeta {

    private Corpse corpse = null;

    private boolean inGame = false;

    private boolean isCrouching = false;

    private boolean canRevive = false;

    private boolean isReviving = false;

    private boolean canTriggerSLA = true;

    private int windowRepairAmount = 1;

    private long ticksPerHeal = 20;

    public Optional<Corpse> getCorpse() {
        return Optional.ofNullable(corpse);
    }

    public void setCorpse(@Nullable Corpse corpse) {
        this.corpse = corpse;
    }

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

    public boolean canTriggerSLA() {
        return canTriggerSLA;
    }

    public void setCanTriggerSLA(boolean canTriggerSLA) {
        this.canTriggerSLA = canTriggerSLA;
    }

    public int getWindowRepairAmount() {
        return windowRepairAmount;
    }

    public void setWindowRepairAmount(int amount) {
        this.windowRepairAmount = amount;
    }

    public long getTicksPerHeal() {
        return ticksPerHeal;
    }

    public void setTicksPerHeal(long ticksPerHeal) {
        this.ticksPerHeal = ticksPerHeal;
    }
}
