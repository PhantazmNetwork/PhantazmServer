package org.phantazm.zombies.player.state.revive;

import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tick.Activable;
import org.phantazm.zombies.event.player.ZombiesPlayerEndReviveEvent;
import org.phantazm.zombies.event.player.ZombiesPlayerReviveEvent;
import org.phantazm.zombies.event.player.ZombiesPlayerStartReviveEvent;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.action_bar.ZombiesPlayerActionBar;
import org.phantazm.zombies.player.state.ZombiesPlayerState;
import org.phantazm.zombies.player.state.context.AlivePlayerStateContext;
import org.phantazm.zombies.player.state.context.KnockedPlayerStateContext;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ReviveHandler implements Activable {
    private final Wrapper<ZombiesPlayer> revivee;
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

    public ReviveHandler(@NotNull Wrapper<ZombiesPlayer> revivee, @NotNull KnockedPlayerStateContext context,
        @NotNull Collection<? extends ZombiesPlayer> zombiesPlayers,
        @NotNull Function<? super AlivePlayerStateContext, ? extends ZombiesPlayerState> defaultStateCreator,
        @NotNull Supplier<? extends ZombiesPlayerState> deathStateSupplier,
        @NotNull Predicate<? super ZombiesPlayer> reviverPredicate, long deathTime) {
        this.revivee = Objects.requireNonNull(revivee);
        this.context = Objects.requireNonNull(context);
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers);
        this.defaultStateCreator = Objects.requireNonNull(defaultStateCreator);
        this.deathStateSupplier = Objects.requireNonNull(deathStateSupplier);
        this.reviverPredicate = Objects.requireNonNull(reviverPredicate);
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

    private void broadcastReviveEnd(@NotNull ZombiesPlayer reviver, boolean isRevived) {
        reviver.getPlayer().ifPresent(reviverPlayer -> {
            ZombiesPlayer revivee = this.revivee.get();
            ZombiesScene scene = revivee.getScene();

            revivee.getPlayer().ifPresent(reviveePlayer -> {
                scene.broadcastEvent(new ZombiesPlayerEndReviveEvent(reviverPlayer, reviver, reviveePlayer, revivee));

                if (isRevived) {
                    scene.broadcastEvent(new ZombiesPlayerReviveEvent(reviverPlayer, reviver, reviveePlayer, revivee));
                }
            });
        });
    }

    private void broadcastReviveStart(@NotNull ZombiesPlayer reviver) {
        reviver.getPlayer().ifPresent(reviverPlayer -> {
            ZombiesPlayer revivee = this.revivee.get();

            revivee.getPlayer().ifPresent(reviveePlayer -> {
                revivee.getScene().broadcastEvent(new ZombiesPlayerStartReviveEvent(reviverPlayer, reviver, reviveePlayer, revivee));
            });
        });
    }

    @Override
    public void tick(long time) {
        ZombiesPlayer reviver = this.reviver;

        if (ticksUntilDeath == 0) {
            if (cachedDeathState == null) {
                cachedDeathState = deathStateSupplier.get();
            }

            if (reviver != null) {
                throw new OutOfMemoryError("what happen?????? MEETHED KIL FANTASM, HURT MEETHED BRANE\n\n\n\n\nmeethed go free cow now, cow maek meethed hapy :)");
            }
            return;
        }

        if (ticksUntilRevive == 0) {
            if (cachedDefaultState == null) {
                Component reviverName = reviver.module().getPlayerView().getDisplayNameIfCached().orElse(null);
                cachedDefaultState = defaultStateCreator.apply(
                    AlivePlayerStateContext.revive(reviverName, context.getKnockLocation()));
            }

            reviver.module().getStats().setRevives(reviver.module().getStats().getRevives() + 1);
            clearReviverState();

            broadcastReviveEnd(reviver, true);
            return;
        }

        if (reviver == null) {
            for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
                if (!zombiesPlayer.module().getMeta().isReviving() && reviverPredicate.test(zombiesPlayer)) {
                    this.reviver = reviver = zombiesPlayer;
                    break;
                }
            }
            if (reviver != null) {
                ticksUntilDeath = deathTime;
                reviver.module().getMeta().setReviving(true);
                ticksUntilRevive = reviver.getReviveTime();

                broadcastReviveStart(reviver);
            } else {
                --ticksUntilDeath;
            }
        } else if (!reviverPredicate.test(reviver)) {
            clearReviverState();
            this.reviver = null;
            ticksUntilRevive = -1;
            broadcastReviveEnd(reviver, false);
        } else {
            --ticksUntilRevive;
        }
    }

    @Override
    public void end() {
        clearReviverState();
        reviver = null;
        ticksUntilRevive = -1;
    }

    public @NotNull Optional<ZombiesPlayer> getReviver() {
        return Optional.ofNullable(reviver);
    }

    private void clearReviverState() {
        if (reviver == null) {
            return;
        }

        reviver.module().getMeta().setReviving(false);
        reviver.module().getActionBar().sendActionBar(Component.empty(), ZombiesPlayerActionBar.REVIVE_MESSAGE_CLEAR_PRIORITY);
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
