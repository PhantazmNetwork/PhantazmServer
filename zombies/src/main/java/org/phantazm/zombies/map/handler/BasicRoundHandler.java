package org.phantazm.zombies.map.handler;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.endless.Endless;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BasicRoundHandler implements RoundHandler {
    private final Collection<? extends ZombiesPlayer> zombiesPlayers;
    private final List<Round> rounds;
    private final Endless endless;

    private Round currentRound;
    private int roundIndex;
    private int lastMobCount = 0;

    private boolean hasEnded;
    private boolean isEndless;

    public BasicRoundHandler(@NotNull Collection<? extends ZombiesPlayer> zombiesPlayers, @NotNull List<Round> rounds,
        @NotNull Endless endless) {
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers);
        this.rounds = Objects.requireNonNull(rounds);
        this.endless = Objects.requireNonNull(endless);

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

        int nextIndex = ++roundIndex;
        if (nextIndex == Integer.MAX_VALUE) { //would cause overflow of the round display
            this.roundIndex = Integer.MAX_VALUE - 1;
            this.hasEnded = true;
            this.currentRound = null;
            return;
        }

        if (nextIndex < rounds.size()) {
            currentRound = rounds.get(nextIndex);
            currentRound.startRound();
            lastMobCount = currentRound.totalMobCount();

            this.currentRound = currentRound;
        } else if (isEndless) {
            currentRound = endless.generateRound(nextIndex);
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
        return isEndless ? this.roundIndex : Math.min(roundIndex, rounds.size() - 1);
    }

    @Override
    public void setCurrentRound(int roundIndex) {
        if (roundIndex < 0 || roundIndex == Integer.MAX_VALUE) {
            return;
        }

        if (!isEndless) {
            Objects.checkIndex(roundIndex, rounds.size());
        }

        if (currentRound != null) {
            currentRound.endRound();
        }

        this.roundIndex = roundIndex;
        currentRound = roundIndex < rounds.size() ? rounds.get(roundIndex) : endless.generateRound(roundIndex);
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
