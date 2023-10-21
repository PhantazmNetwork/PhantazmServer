package org.phantazm.stats.zombies;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class BasicZombiesPlayerMapStats implements ZombiesPlayerMapStats {

    private final UUID playerUUID;

    private final Key mapKey;

    private int gamesPlayed;

    private int wins;

    private int bestRound;

    private int roundsSurvived;

    private int kills;

    private long coinsGained;

    private long coinsSpent;

    private int knocks;

    private int deaths;

    private int revives;

    private int shots;

    private int regularHits;

    private int headshotHits;

    public BasicZombiesPlayerMapStats(@NotNull UUID playerUUID, @NotNull Key mapKey, int gamesPlayed, int wins,
        int bestRound, int roundsSurvived, int kills, int knocks, long coinsGained,
        long coinsSpent, int deaths, int revives, int shots, int regularHits, int headshotHits) {
        this.playerUUID = Objects.requireNonNull(playerUUID);
        this.mapKey = Objects.requireNonNull(mapKey);
        this.gamesPlayed = gamesPlayed;
        this.wins = wins;
        this.bestRound = bestRound;
        this.roundsSurvived = roundsSurvived;
        this.kills = kills;
        this.coinsGained = coinsGained;
        this.coinsSpent = coinsSpent;
        this.knocks = knocks;
        this.deaths = deaths;
        this.revives = revives;
        this.shots = shots;
        this.regularHits = regularHits;
        this.headshotHits = headshotHits;
    }

    public static ZombiesPlayerMapStats createBasicStats(@NotNull UUID playerUUID, @NotNull Key mapKey) {
        return new BasicZombiesPlayerMapStats(playerUUID, mapKey, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Override
    public @NotNull UUID getPlayerUUID() {
        return playerUUID;
    }

    @Override
    public @NotNull Key getMapKey() {
        return mapKey;
    }

    @Override
    public int getGamesPlayed() {
        return gamesPlayed;
    }

    @Override
    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    @Override
    public int getWins() {
        return wins;
    }

    @Override
    public void setWins(int wins) {
        this.wins = wins;
    }

    @Override
    public int getBestRound() {
        return bestRound;
    }

    @Override
    public void setBestRound(int bestRound) {
        this.bestRound = bestRound;
    }

    @Override
    public int getRoundsSurvived() {
        return roundsSurvived;
    }

    @Override
    public void setRoundsSurvived(int roundsSurvived) {
        this.roundsSurvived = roundsSurvived;
    }

    @Override
    public int getKills() {
        return kills;
    }

    @Override
    public void setKills(int kills) {
        this.kills = kills;
    }

    @Override
    public long getCoinsGained() {
        return coinsGained;
    }

    @Override
    public void setCoinsGained(long coinsGained) {
        this.coinsGained = coinsGained;
    }

    @Override
    public long getCoinsSpent() {
        return coinsSpent;
    }

    @Override
    public void setCoinsSpent(long coinsSpent) {
        this.coinsSpent = coinsSpent;
    }

    @Override
    public int getKnocks() {
        return knocks;
    }

    @Override
    public void setKnocks(int knocks) {
        this.knocks = knocks;
    }

    @Override
    public int getDeaths() {
        return deaths;
    }

    @Override
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    @Override
    public int getRevives() {
        return revives;
    }

    @Override
    public void setRevives(int revives) {
        this.revives = revives;
    }

    @Override
    public int getShots() {
        return shots;
    }

    @Override
    public void setShots(int shots) {
        this.shots = shots;
    }

    @Override
    public int getRegularHits() {
        return regularHits;
    }

    @Override
    public void setRegularHits(int regularShots) {
        this.regularHits = regularShots;
    }

    @Override
    public int getHeadshotHits() {
        return headshotHits;
    }

    @Override
    public void setHeadshotHits(int headshots) {
        this.headshotHits = headshots;
    }
}
