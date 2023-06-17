package org.phantazm.zombies.player;

import org.jetbrains.annotations.Nullable;
import org.phantazm.zombies.corpse.Corpse;

import java.util.Optional;

//TODO: rename
public class ZombiesPlayerMeta {

    private Corpse corpse = null;

    private boolean inGame = false;

    private boolean isReviving = false;

    private int windowRepairAmount = 1;

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

    public boolean isReviving() {
        return isReviving;
    }

    public void setReviving(boolean reviving) {
        isReviving = reviving;
    }

    public int getWindowRepairAmount() {
        return windowRepairAmount;
    }

    public void setWindowRepairAmount(int amount) {
        this.windowRepairAmount = amount;
    }
}
