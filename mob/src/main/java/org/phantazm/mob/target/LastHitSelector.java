package org.phantazm.mob.target;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;

import java.util.Optional;

public class LastHitSelector<TTarget> implements TargetSelector<TTarget> {

    private TTarget lastHit;

    @Override
    public @NotNull Optional<TTarget> selectTarget(@NotNull PhantazmMob self) {
        return Optional.ofNullable(lastHit);
    }

    public void setLastHit(TTarget lastHit) {
        this.lastHit = lastHit;
    }
}
