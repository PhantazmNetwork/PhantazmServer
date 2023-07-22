package org.phantazm.zombies.player.state.revive;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.Activable;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.action_bar.ZombiesPlayerActionBar;
import org.phantazm.zombies.player.state.ZombiesPlayerState;
import org.phantazm.zombies.player.state.context.AlivePlayerStateContext;
import org.phantazm.zombies.player.state.context.KnockedPlayerStateContext;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ReviveHandler implements Activable {
    private final KnockedPlayerStateContext context;
    private final Collection<? extends ZombiesPlayer> zombiesPlayers;
    private final Function<? super AlivePlayerStateContext, ? extends ZombiesPlayerState> defaultStateCreator;
    private final Supplier<? extends ZombiesPlayerState> deathStateSupplier;
    private final Predicate<? super ZombiesPlayer> reviverPredicate;

    private final long deathTime;

    private ZombiesPlayerState cachedDefaultState = null;
    private ZombiesPlayerState cachedDeathState = null;
    private ZombiesPlayer reviver;
    private long ticksUntilDeath;

    private long ticksUntilRevive = -1;

    public ReviveHandler(@NotNull KnockedPlayerStateContext context,
            @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers,
            @NotNull Function<? super AlivePlayerStateContext, ? extends ZombiesPlayerState> defaultStateCreator,
            @NotNull Supplier<? extends ZombiesPlayerState> deathStateSupplier,
            @NotNull Predicate<? super ZombiesPlayer> reviverPredicate, long deathTime) {
        this.context = Objects.requireNonNull(context, "context");
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.defaultStateCreator = Objects.requireNonNull(defaultStateCreator, "defaultStateCreator");
        this.deathStateSupplier = Objects.requireNonNull(deathStateSupplier, "deathStateSupplier");
        this.reviverPredicate = Objects.requireNonNull(reviverPredicate, "reviverPredicate");
        this.deathTime = deathTime;
        this.ticksUntilDeath = deathTime;
    }

    public @NotNull KnockedPlayerStateContext context() {
        return context;
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
                Component reviverName = null;
                if (reviver != null) {
                    reviverName = reviver.module().getPlayerView().getDisplayNameIfCached().orElse(null);
                }
                cachedDefaultState = defaultStateCreator.apply(
                        AlivePlayerStateContext.revive(reviverName, context.getKnockLocation()));
            }

            if (reviver != null) {
                reviver.module().getStats().setRevives(reviver.module().getStats().getRevives() + 1);
                clearReviverState();
            }

            reviver = null;
            return;
        }

        if (reviver == null) {
            for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
                if (!zombiesPlayer.module().getMeta().isReviving() && reviverPredicate.test(zombiesPlayer)) {
                    reviver = zombiesPlayer;
                    break;
                }
            }
            if (reviver != null) {
                ticksUntilDeath = deathTime;
                reviver.module().getMeta().setReviving(true);
                ticksUntilRevive = reviver.getReviveTime();
            }
            else {
                --ticksUntilDeath;
            }
        }
        else if (!reviverPredicate.test(reviver)) {
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
            reviver.module().getActionBar()
                    .sendActionBar(Component.empty(), ZombiesPlayerActionBar.REVIVE_MESSAGE_CLEAR_PRIORITY);
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
