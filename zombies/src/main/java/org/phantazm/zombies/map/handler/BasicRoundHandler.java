package org.phantazm.zombies.map.handler;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BasicRoundHandler implements RoundHandler {
    private final Collection<? extends ZombiesPlayer> zombiesPlayers;
    private final List<Round> rounds;

    private Round currentRound;
    private int roundIndex;
    private int lastMobCount = 0;

    private boolean hasEnded;
    private boolean isEndless;

    public BasicRoundHandler(@NotNull Collection<? extends ZombiesPlayer> zombiesPlayers, @NotNull List<Round> rounds) {
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers);
        this.rounds = Objects.requireNonNull(rounds);

        if (rounds.isEmpty()) {
            hasEnded = true;
        }
    }

    @Override
    public void tick(long time) {
        boolean hasEnded = this.hasEnded;
        Round currentRound = this.currentRound;

        if (currentRound == null || hasEnded) {
            return;
        }

        currentRound.tick(time);
        if (currentRound.isActive()) {
            lastMobCount = currentRound.totalMobCount();
            return;
        }

        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            if (zombiesPlayer.hasQuit()) {
                continue;
            }

            zombiesPlayer.module().getStats()
                .setRoundsSurvived(zombiesPlayer.module().getStats().getRoundsSurvived() + 1);
        }

        if (++roundIndex < rounds.size()) {
            currentRound = rounds.get(roundIndex);
            currentRound.startRound();
            lastMobCount = currentRound.totalMobCount();

            this.currentRound = currentRound;
        } else {
            this.hasEnded = true;
            this.currentRound = null;
        }
    }

    @Override
    public int roundCount() {
        return rounds.size();
    }

    @Override
    public int currentRoundIndex() {
        return Math.min(roundIndex, rounds.size() - 1);
    }

    @Override
    public void setCurrentRound(int roundIndex) {
        Objects.checkIndex(roundIndex, rounds.size());

        if (currentRound != null) {
            currentRound.endRound();
        }

        this.roundIndex = roundIndex;
        currentRound = rounds.get(roundIndex);
        currentRound.startRound();
        lastMobCount = currentRound.totalMobCount();
    }

    @Override
    public @NotNull Optional<Round> currentRound() {
        return Optional.ofNullable(currentRound);
    }

    @Override
    public boolean hasEnded() {
        return hasEnded;
    }

    @Override
    public int lastMobCount() {
        return lastMobCount;
    }

    @Override
    public void end() {
        hasEnded = true;
        currentRound = null;
    }

    @Override
    public boolean isEndless() {
        return isEndless;
    }

    @Override
    public void enableEndless() {
        this.isEndless = true;
    }
}
