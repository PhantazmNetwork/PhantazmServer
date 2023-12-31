package org.phantazm.stats.zombies;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface ZombiesPlayerMapStats {
    @NotNull UUID getPlayerUUID();

    @NotNull Key getMapKey();

    int getGamesPlayed();

    void setGamesPlayed(int gamesPlayed);

    int getWins();

    void setWins(int wins);

    int getBestRound();

    void setBestRound(int bestRound);

    int getRoundsSurvived();

    void setRoundsSurvived(int roundsSurvived);

    int getKills();

    void setKills(int kills);

    long getCoinsGained();

    void setCoinsGained(long goldGained);

    long getCoinsSpent();

    void setCoinsSpent(long goldSpent);

    int getKnocks();

    void setKnocks(int knocks);

    int getDeaths();

    void setDeaths(int deaths);

    int getRevives();

    void setRevives(int revives);

    int getShots();

    void setShots(int shots);

    int getRegularHits();

    void setRegularHits(int regularShots);

    int getHeadshotHits();

    void setHeadshotHits(int headshots);
}
