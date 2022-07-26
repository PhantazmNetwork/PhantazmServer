package com.github.phantazmnetwork.zombies.game.stage;

import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CountdownStage implements Stage {

    private final Map<UUID, ZombiesPlayer> zombiesPlayers;

    private final long countdownDuration;

    private long countdownTicksRemaining;

    public CountdownStage(@NotNull Map<UUID, ZombiesPlayer> zombiesPlayers, long countdownDuration) {
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
        this.countdownDuration = countdownDuration;
        this.countdownTicksRemaining = countdownDuration;
    }

    @Override
    public void tick(long time) {
        long previousTicks = countdownTicksRemaining--;

        // TODO: delegate to another class
        if (previousTicks == 400 || previousTicks == 200 || previousTicks == 100 || previousTicks == 80 ||
                previousTicks == 60 || previousTicks == 40 || previousTicks == 20) {
            for (ZombiesPlayer zombiesPlayer : zombiesPlayers.values()) {
                Component message =
                        Component.text(previousTicks / 20 + " seconds until the game starts.", NamedTextColor.YELLOW);
                zombiesPlayer.getPlayerView().getPlayer().ifPresent(player -> {
                    player.sendMessage(message);
                });
            }
        }
    }

    @Override
    public void start() {
        countdownTicksRemaining = countdownDuration;
    }

    @Override
    public void end() {

    }

    @Override
    public boolean shouldEnd() {
        return countdownTicksRemaining <= 0;
    }

    @Override
    public boolean hasPermanentPlayers() {
        return false;
    }

    public long getCountdownTicksRemaining() {
        return countdownTicksRemaining;
    }
}
