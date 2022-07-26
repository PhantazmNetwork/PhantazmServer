package com.github.phantazmnetwork.zombies.game.player.state.knocked;

import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.game.player.state.ZombiesPlayerState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class KnockedPlayerState implements ZombiesPlayerState {

    private final Reference<Instance> instanceReference;
    private final PlayerView playerView;
    private final CompletableFuture<? extends Component> knockMessageFuture;
    private final Consumer<? super Player> knockedAction;
    private final Supplier<? extends ZombiesPlayer> reviverSupplier;
    private final Supplier<? extends ZombiesPlayerState> deathStateSupplier;
    private final Supplier<? extends ZombiesPlayerState> defaultStateSupplier;
    private final Collection<? extends DeathTickable> deathTickables;
    private final Collection<? extends ReviveTickable> reviveTickables;
    private final long deathTime;
    private final long reviveTime;
    private ZombiesPlayer reviver = null;
    private long ticksUntilDeath;
    private long ticksUntilRevive;

    public KnockedPlayerState(@NotNull Instance instance, @NotNull PlayerView playerView,
            @NotNull CompletableFuture<? extends Component> knockMessageFuture,
            @NotNull Supplier<? extends ZombiesPlayerState> deathStateSupplier,
            @NotNull Supplier<? extends ZombiesPlayerState> defaultStateSupplier,
            @NotNull Consumer<? super Player> knockedAction, @NotNull Supplier<? extends ZombiesPlayer> reviverSupplier,
            @NotNull Collection<? extends DeathTickable> deathTickables,
            @NotNull Collection<? extends ReviveTickable> reviveTickables, long deathTime, long reviveTime) {
        this.instanceReference = new WeakReference<>(Objects.requireNonNull(instance, "instance"));
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.knockMessageFuture = Objects.requireNonNull(knockMessageFuture, "knockMessageFuture");
        this.deathStateSupplier = Objects.requireNonNull(deathStateSupplier, "deathStateSupplier");
        this.defaultStateSupplier = Objects.requireNonNull(defaultStateSupplier, "defaultStateSupplier");
        this.knockedAction = Objects.requireNonNull(knockedAction, "knockedAction");
        this.reviverSupplier = Objects.requireNonNull(reviverSupplier, "reviverSupplier");
        this.deathTickables = Objects.requireNonNull(deathTickables, "deathTickables");
        this.reviveTickables = Objects.requireNonNull(reviveTickables, "reviveTickables");
        this.deathTime = deathTime;
        this.reviveTime = reviveTime;
        this.ticksUntilDeath = deathTime;
        this.ticksUntilRevive = reviveTime;
    }

    @Override
    public void start() {
        knockMessageFuture.thenAccept(knockMessage -> {
            Instance instance = instanceReference.get();
            if (instance != null) {
                instance.sendMessage(knockMessage);
            }
        });
        playerView.getPlayer().ifPresent(knockedAction);
    }

    @Override
    public @NotNull Optional<ZombiesPlayerState> tick(long time) {
        if (ticksUntilDeath == 0) {
            return Optional.of(deathStateSupplier.get());
        }
        else if (ticksUntilRevive == 0) {
            return Optional.of(defaultStateSupplier.get());
        }

        if (reviver == null) {
            reviver = reviverSupplier.get();
            if (reviver == null) {
                if (ticksUntilDeath-- > 0) {
                    for (DeathTickable tickable : deathTickables) {
                        tickable.tick(time, ticksUntilDeath);
                    }
                }
            }
            else {
                ticksUntilDeath = deathTime;
                reviver.setReviving(true);
            }
        }
        else if (reviver.getPlayerView().getPlayer().isEmpty()) {
            reviver = null;
            ticksUntilRevive = reviveTime;
        }
        else if (ticksUntilRevive-- > 0) {
            for (ReviveTickable tickable : reviveTickables) {
                tickable.tick(time, reviver, ticksUntilRevive);
            }
        }

        return Optional.empty();
    }

    @Override
    public void end() {
        for (DeathTickable tickable : deathTickables) {
            tickable.end();
        }
        for (ReviveTickable tickable : reviveTickables) {
            tickable.end();
        }
        if (reviver != null) {
            reviver.setReviving(false);
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.text("KNOCKED", NamedTextColor.YELLOW);
    }

    public interface DeathTickable {

        void tick(long time, long ticksUntilDeath);

        void end();

    }

    public interface ReviveTickable {

        void tick(long time, @NotNull ZombiesPlayer reviver, long ticksUntilRevive);

        void end();

    }
}
