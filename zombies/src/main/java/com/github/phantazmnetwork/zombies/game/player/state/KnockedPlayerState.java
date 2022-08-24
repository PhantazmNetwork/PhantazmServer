package com.github.phantazmnetwork.zombies.game.player.state;

import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class KnockedPlayerState implements ZombiesPlayerState {

    private final Supplier<? extends ZombiesPlayer> reviverSupplier;
    private final Supplier<? extends ZombiesPlayerState> deathStateSupplier;
    private final Supplier<? extends ZombiesPlayerState> defaultStateSupplier;
    private final Collection<? extends Action> actions;
    private final long deathTime;
    private ZombiesPlayer reviver = null;

    private ZombiesPlayerState cachedDeathState = null;

    private ZombiesPlayerState cachedDefaultState = null;

    private long ticksUntilDeath;
    private long ticksUntilRevive = -1;

    public KnockedPlayerState(@NotNull Supplier<? extends ZombiesPlayerState> deathStateSupplier,
            @NotNull Supplier<? extends ZombiesPlayerState> defaultStateSupplier,
            @NotNull Supplier<? extends ZombiesPlayer> reviverSupplier, @NotNull Collection<? extends Action> actions,
            long deathTime) {
        this.deathStateSupplier = Objects.requireNonNull(deathStateSupplier, "deathStateSupplier");
        this.defaultStateSupplier = Objects.requireNonNull(defaultStateSupplier, "defaultStateSupplier");
        this.reviverSupplier = Objects.requireNonNull(reviverSupplier, "reviverSupplier");
        this.actions = List.copyOf(actions);
        this.deathTime = deathTime;
        this.ticksUntilDeath = deathTime;
    }

    @Override
    public void start() {
        for (Action action : actions) {
            action.start();
        }
    }

    @Override
    public @NotNull Optional<ZombiesPlayerState> tick(long time) {
        if (ticksUntilDeath == 0) {
            return Optional.ofNullable(cachedDeathState)
                    .or(() -> Optional.of(cachedDeathState = deathStateSupplier.get()));
        }
        else if (ticksUntilRevive == 0) {
            return Optional.ofNullable(cachedDefaultState)
                    .or(() -> Optional.of(cachedDefaultState = defaultStateSupplier.get()));
        }

        if (reviver == null) {
            reviver = reviverSupplier.get();
            if (reviver == null) {
                if (ticksUntilDeath-- > 0) {
                    for (Action action : actions) {
                        action.deathTick(time, ticksUntilDeath);
                    }
                }
            }
            else {
                ticksUntilDeath = deathTime;
                reviver.getMeta().setReviving(true);
                ticksUntilRevive = reviver.getReviveTime();
            }
        }
        else if (!reviver.getMeta().isCanRevive() || !reviver.getMeta().isCrouching()) {
            reviver.getMeta().setCanRevive(false);
            reviver = null;
            ticksUntilRevive = -1;
        }
        else if (ticksUntilRevive-- > 0) {
            for (Action action : actions) {
                action.reviveTick(time, reviver, ticksUntilRevive);
            }
        }

        return Optional.empty();
    }

    @Override
    public void end() {
        if (reviver != null) {
            reviver.getMeta().setReviving(false);
        }
        for (Action action : actions) {
            action.end(reviver);
        }
        reviver = null;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.text("KNOCKED", NamedTextColor.YELLOW);
    }

    @Override
    public @NotNull Key key() {
        return ZombiesPlayerStateKeys.KNOCKED;
    }

    public interface Action {

        default void start() {

        }

        default void deathTick(long time, long ticksUntilDeath) {

        }

        default void reviveTick(long time, @NotNull ZombiesPlayer reviver, long ticksUntilRevive) {

        }

        default void end(@Nullable ZombiesPlayer reviver) {

        }

    }
}
