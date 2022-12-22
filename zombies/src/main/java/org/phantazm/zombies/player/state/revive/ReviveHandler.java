package org.phantazm.zombies.player.state.revive;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.Activable;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.state.ZombiesPlayerState;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class ReviveHandler implements Activable {

    private final Supplier<? extends ZombiesPlayerState> defaultStateSupplier;

    private final Supplier<? extends ZombiesPlayerState> deathStateSupplier;

    private final Supplier<? extends ZombiesPlayer> reviverFinder;
    private final long deathTime;
    private ZombiesPlayerState cachedDefaultState = null;
    private ZombiesPlayerState cachedDeathState = null;
    private ZombiesPlayer reviver;
    private long ticksUntilDeath;

    private long ticksUntilRevive = -1;

    public ReviveHandler(@NotNull Supplier<? extends ZombiesPlayerState> defaultStateSupplier,
            @NotNull Supplier<? extends ZombiesPlayerState> deathStateSupplier,
            @NotNull Supplier<? extends ZombiesPlayer> reviverFinder, long deathTime) {
        this.defaultStateSupplier = Objects.requireNonNull(defaultStateSupplier, "defaultStateSupplier");
        this.deathStateSupplier = Objects.requireNonNull(deathStateSupplier, "deathStateSupplier");
        this.reviverFinder = Objects.requireNonNull(reviverFinder, "reviverFinder");
        this.deathTime = deathTime;
        this.ticksUntilDeath = deathTime;
    }

    public @NotNull Optional<ZombiesPlayerState> getSuggestedState() {
        if (cachedDeathState != null) {
            return Optional.of(cachedDeathState);
        }
        if (cachedDefaultState != null) {
            return Optional.of(cachedDefaultState);
        }

        return Optional.empty();
    }

    @Override
    public void tick(long time) {
        if (ticksUntilDeath == 0) {
            if (cachedDeathState == null) {
                cachedDeathState = deathStateSupplier.get();
            }
            reviver = null;
            return;
        }
        if (ticksUntilRevive == 0) {
            if (cachedDefaultState == null) {
                cachedDefaultState = defaultStateSupplier.get();
            }
            reviver = null;
            return;
        }

        if (reviver == null) {
            reviver = reviverFinder.get();
            if (reviver != null) {
                ticksUntilDeath = deathTime;
                reviver.getModule().getMeta().setReviving(true);
                ticksUntilRevive = reviver.getReviveTime();
            }
            else {
                --ticksUntilDeath;
            }
        }
        else if (!reviver.getModule().getMeta().isCanRevive() || !reviver.getModule().getMeta().isCrouching()) {
            reviver.getModule().getMeta().setReviving(false);
            reviver = null;
            ticksUntilRevive = -1;
        }
        else {
            --ticksUntilRevive;
        }
    }

    @Override
    public void end() {
        reviver = null;
    }

    public @NotNull Optional<ZombiesPlayer> getReviver() {
        return Optional.ofNullable(reviver);
    }

    public void setReviver(@Nullable ZombiesPlayer reviver) {
        if (this.reviver == reviver) {
            return;
        }

        if (this.reviver != null) {
            this.reviver.getModule().getMeta().setReviving(false);
        }
        this.reviver = reviver;
        if (reviver != null) {
            ticksUntilDeath = deathTime;
            reviver.getModule().getMeta().setReviving(true);
            ticksUntilRevive = reviver.getReviveTime();
        }
        else {
            ticksUntilRevive = -1;
        }
    }

    public boolean isReviving() {
        return reviver != null;
    }

    public long getTicksUntilDeath() {
        return ticksUntilDeath;
    }

    public long getTicksUntilRevive() {
        return ticksUntilRevive;
    }
}
