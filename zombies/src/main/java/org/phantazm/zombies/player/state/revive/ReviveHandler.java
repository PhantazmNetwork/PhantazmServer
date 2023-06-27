package org.phantazm.zombies.player.state.revive;

import net.kyori.adventure.text.Component;
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

            if (reviver != null) {
                clearReviverState();
            }

            reviver = null;
            return;
        }
        if (ticksUntilRevive == 0) {
            if (cachedDefaultState == null) {
                cachedDefaultState = defaultStateSupplier.get();
            }

            if (reviver != null) {
                reviver.module().getStats().setRevives(reviver.module().getStats().getRevives() + 1);
                clearReviverState();
            }

            reviver = null;
            return;
        }

        if (reviver == null) {
            reviver = reviverFinder.get();
            if (reviver != null) {
                ticksUntilDeath = deathTime;
                reviver.module().getMeta().setReviving(true);
                ticksUntilRevive = reviver.getReviveTime();
            }
            else {
                --ticksUntilDeath;
            }
        }
        else if (!reviver.canRevive()) {
            clearReviverState();
            reviver = null;
            ticksUntilRevive = -1;
        }
        else {
            --ticksUntilRevive;
        }
    }

    @Override
    public void end() {
        clearReviverState();
        reviver = null;
    }

    public @NotNull Optional<ZombiesPlayer> getReviver() {
        return Optional.ofNullable(reviver);
    }

    public void setReviver(@Nullable ZombiesPlayer reviver) {
        if (this.reviver == reviver) {
            return;
        }

        clearReviverState();
        this.reviver = reviver;
        if (reviver != null) {
            ticksUntilDeath = deathTime;
            reviver.module().getMeta().setReviving(true);
            ticksUntilRevive = reviver.getReviveTime();
        }
        else {
            ticksUntilRevive = -1;
        }
    }

    private void clearReviverState() {
        if (reviver != null) {
            reviver.module().getMeta().setReviving(false);
            reviver.module().getActionBar().sendActionBar(Component.empty(), 2);
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
