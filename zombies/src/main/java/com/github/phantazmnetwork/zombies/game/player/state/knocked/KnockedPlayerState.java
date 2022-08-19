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
import java.util.List;
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
    private final Collection<? extends Tickable> tickables;
    private final long deathTime;
    private ZombiesPlayer reviver = null;
    private long ticksUntilDeath;
    private long ticksUntilRevive = -1;

    public KnockedPlayerState(@NotNull Instance instance, @NotNull PlayerView playerView,
            @NotNull CompletableFuture<? extends Component> knockMessageFuture,
            @NotNull Supplier<? extends ZombiesPlayerState> deathStateSupplier,
            @NotNull Supplier<? extends ZombiesPlayerState> defaultStateSupplier,
            @NotNull Consumer<? super Player> knockedAction, @NotNull Supplier<? extends ZombiesPlayer> reviverSupplier,
            @NotNull Collection<? extends Tickable> tickables, long deathTime) {
        this.instanceReference = new WeakReference<>(Objects.requireNonNull(instance, "instance"));
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.knockMessageFuture = Objects.requireNonNull(knockMessageFuture, "knockMessageFuture");
        this.deathStateSupplier = Objects.requireNonNull(deathStateSupplier, "deathStateSupplier");
        this.defaultStateSupplier = Objects.requireNonNull(defaultStateSupplier, "defaultStateSupplier");
        this.knockedAction = Objects.requireNonNull(knockedAction, "knockedAction");
        this.reviverSupplier = Objects.requireNonNull(reviverSupplier, "reviverSupplier");
        this.tickables = List.copyOf(tickables);
        this.deathTime = deathTime;
        this.ticksUntilDeath = deathTime;
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
        for (Tickable tickable : tickables) {
            tickable.start();
        }
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
                    for (Tickable tickable : tickables) {
                        tickable.deathTick(time, ticksUntilDeath);
                    }
                }
            }
            else if (!reviver.isReviving()) {
                ticksUntilDeath = deathTime;
                reviver.setReviving(true);
                ticksUntilRevive = reviver.getReviveTime();
            }
        }
        else if (reviver.getPlayerView().getPlayer().isEmpty()) {
            reviver = null;
            ticksUntilRevive = -1;
        }
        else if (ticksUntilRevive-- > 0) {
            for (Tickable tickable : tickables) {
                tickable.reviveTick(time, reviver, ticksUntilRevive);
            }
        }

        return Optional.empty();
    }

    @Override
    public void end() {
        for (Tickable tickable : tickables) {
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

    public interface Tickable {

        void start();

        void deathTick(long time, long ticksUntilDeath);

        void reviveTick(long time, @NotNull ZombiesPlayer reviver, long ticksUntilRevive);

        void end();

    }
}
