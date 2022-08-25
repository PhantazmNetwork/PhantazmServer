package com.github.phantazmnetwork.zombies.game.player;

import com.github.phantazmnetwork.zombies.game.corpse.Corpse;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

//TODO: rename
public class ZombiesPlayerMeta {

    private Corpse corpse = null;

    private boolean inGame = false;

    private boolean isCrouching = false;

    private boolean canRevive = false;

    private boolean isReviving = false;

    private boolean canTriggerSLA = false;

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

    public boolean isCanTriggerSLA() {
        return canTriggerSLA;
    }

    public void setCanTriggerSLA(boolean canTriggerSLA) {
        this.canTriggerSLA = canTriggerSLA;
    }
}
