package com.github.phantazmnetwork.zombies.game.stage;

import com.github.phantazmnetwork.commons.Activable;
import com.github.phantazmnetwork.commons.Wrapper;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class CountdownStage extends StageBase {

    private final Collection<ZombiesPlayer> zombiesPlayers;

    private final long countdownDuration;

    private final Wrapper<Long> countdownTicksRemaining;

    public CountdownStage(@NotNull Collection<Activable> activables, @NotNull Collection<ZombiesPlayer> zombiesPlayers,
            @NotNull Wrapper<Long> countdownTicksRemaining, long countdownDuration) {
        super(activables);
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.countdownTicksRemaining = Objects.requireNonNull(countdownTicksRemaining, "countdownTicksRemaining");
        this.countdownDuration = countdownDuration;
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        long previousTicks = countdownTicksRemaining.get();

        // TODO: delegate to another class
        if (previousTicks == 400 || previousTicks == 200 || previousTicks == 100 || previousTicks == 80 ||
                previousTicks == 60 || previousTicks == 40 || previousTicks == 20) {
            for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
                Component message = Component.text(
                        previousTicks / MinecraftServer.TICK_PER_SECOND + " seconds until the game starts.",
                        NamedTextColor.YELLOW);
                zombiesPlayer.getModule().getPlayerView().getPlayer().ifPresent(player -> {
                    player.sendMessage(message);
                });
            }
        }
        countdownTicksRemaining.apply(ticks -> ticks - 1);
    }

    @Override
    public void start() {
        super.start();
        countdownTicksRemaining.set(countdownDuration);
    }

    @Override
    public boolean shouldEnd() {
        return countdownTicksRemaining.get() <= 0;
    }

    @Override
    public boolean hasPermanentPlayers() {
        return false;
    }

}
