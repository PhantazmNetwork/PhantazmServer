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
    private final Collection<? extends Activable> actions;
    private final long deathTime;
    private ZombiesPlayer reviver = null;

    private ZombiesPlayerState cachedDeathState = null;

    private ZombiesPlayerState cachedDefaultState = null;

    private long ticksUntilDeath;
    private long ticksUntilRevive = -1;

    public KnockedPlayerState(@NotNull Supplier<? extends ZombiesPlayerState> deathStateSupplier,
            @NotNull Supplier<? extends ZombiesPlayerState> defaultStateSupplier,
            @NotNull Supplier<? extends ZombiesPlayer> reviverSupplier,
            @NotNull Collection<? extends Activable> activables, long deathTime) {
        this.deathStateSupplier = Objects.requireNonNull(deathStateSupplier, "deathStateSupplier");
        this.defaultStateSupplier = Objects.requireNonNull(defaultStateSupplier, "defaultStateSupplier");
        this.reviverSupplier = Objects.requireNonNull(reviverSupplier, "reviverSupplier");
        this.actions = List.copyOf(activables);
        this.deathTime = deathTime;
        this.ticksUntilDeath = deathTime;
    }

    @Override
    public void start() {
        for (Activable activable : actions) {
            activable.start();
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
                    for (Activable activable : actions) {
                        activable.deathTick(time, ticksUntilDeath);
                    }
                }
            }
            else {
                ticksUntilDeath = deathTime;
                reviver.getModule().getMeta().setReviving(true);
                ticksUntilRevive = reviver.getReviveTime();
            }
        }
        else if (!reviver.getModule().getMeta().isCanRevive() || !reviver.getModule().getMeta().isCrouching()) {
            reviver.getModule().getMeta().setReviving(false);
            reviver = null;
            ticksUntilRevive = -1;
        }
        else if (ticksUntilRevive-- > 0) {
            for (Activable activable : actions) {
                activable.reviveTick(time, reviver, ticksUntilRevive);
            }
        }

        return Optional.empty();
    }

    @Override
    public void end() {
        if (reviver != null) {
            reviver.getModule().getMeta().setReviving(false);
        }
        for (Activable activable : actions) {
            activable.end(reviver);
        }
        reviver = null;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.text("KNOCKED", NamedTextColor.YELLOW);
    }

    @Override
    public @NotNull Key key() {
        return ZombiesPlayerStateKeys.KNOCKED.key();
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

    public interface Activable {

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
