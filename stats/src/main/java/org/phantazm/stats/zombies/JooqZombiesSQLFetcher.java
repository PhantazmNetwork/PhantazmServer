package org.phantazm.stats.zombies;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.impl.SQLDataType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

public class JooqZombiesSQLFetcher implements ZombiesSQLFetcher {

    @Override
    public void synchronizeZombiesPlayerMapStats(@NotNull Connection connection,
            @NotNull ZombiesPlayerMapStats mapStats) {
        using(connection).insertInto(table("zombies_player_map_stats"), field("player_uuid"), field("map_key"),
                        field("games_played"), field("wins"), field("best_time"), field("best_round"), field("rounds_survived"),
                        field("kills"), field("coins_gained"), field("coins_spent"), field("knocks"), field("deaths"),
                        field("revives"), field("shots"), field("regular_hits"), field("headshot_hits"))
                .values(mapStats.getPlayerUUID().toString(), mapStats.getMapKey().asString(), mapStats.getGamesPlayed(),
                        mapStats.getWins(), mapStats.getBestTime().orElse(null), mapStats.getBestRound(),
                        mapStats.getRoundsSurvived(), mapStats.getKills(), mapStats.getCoinsGained(),
                        mapStats.getCoinsSpent(), mapStats.getKnocks(), mapStats.getDeaths(), mapStats.getRevives(),
                        mapStats.getShots(), mapStats.getRegularHits(), mapStats.getHeadshotHits())
                .onDuplicateKeyUpdate()
                .set(field("games_played"), field("games_played", SQLDataType.INTEGER).plus(mapStats.getGamesPlayed()))
                .set(field("wins"), field("wins", SQLDataType.INTEGER).plus(mapStats.getWins())).set(field("best_time"),
                        mapStats.getBestTime().isPresent()
                        ? when(field("best_time").isNotNull(),
                                least(field("best_time"), mapStats.getBestTime().get())).otherwise(mapStats.getBestTime().get())
                        : field("best_time", SQLDataType.BIGINT)).set(field("best_round"),
                        greatest(field("best_round", SQLDataType.INTEGER), val(mapStats.getBestRound())))
                .set(field("rounds_survived"),
                        field("rounds_survived", SQLDataType.INTEGER).plus(mapStats.getRoundsSurvived()))
                .set(field("kills"), field("kills", SQLDataType.INTEGER).plus(mapStats.getRoundsSurvived()))
                .set(field("coins_gained"), field("coins_gained", SQLDataType.BIGINT).plus(mapStats.getCoinsGained()))
                .set(field("coins_spent"), field("coins_spent", SQLDataType.BIGINT).plus(mapStats.getCoinsSpent()))
                .set(field("knocks"), field("knocks", SQLDataType.INTEGER).plus(mapStats.getKnocks()))
                .set(field("deaths"), field("deaths", SQLDataType.INTEGER).plus(mapStats.getDeaths()))
                .set(field("revives"), field("revives", SQLDataType.INTEGER).plus(mapStats.getRevives()))
                .set(field("shots"), field("shots", SQLDataType.INTEGER).plus(mapStats.getShots()))
                .set(field("regular_hits"), field("regular_hits", SQLDataType.INTEGER).plus(mapStats.getRegularHits()))
                .set(field("headshot_hits"),
                        field("headshot_hits", SQLDataType.INTEGER).plus(mapStats.getHeadshotHits())).execute();
    }

    @Override
    public @NotNull ZombiesPlayerMapStats getMapStats(@NotNull Connection connection, @NotNull UUID playerUUID,
            @NotNull Key mapKey) {
        Record result = using(connection).select().from(table("zombies_player_map_stats"))
                .where(field("player_uuid").eq(playerUUID.toString())).and(field("map_key").eq(mapKey.asString()))
                .fetchOne();
        if (result == null) {
            return BasicZombiesPlayerMapStats.createBasicStats(playerUUID, mapKey);
        }

        return new BasicZombiesPlayerMapStats(playerUUID, mapKey, result.get("games_played", int.class),
                result.get("wins", int.class), result.get("best_time", Long.class), result.get("best_round", int.class),
                result.get("rounds_survived", int.class), result.get("kills", int.class),
                result.get("knocks", int.class), result.get("coins_gained", int.class),
                result.get("coins_spent", int.class), result.get("deaths", int.class), result.get("revives", int.class),
                result.get("shots", int.class), result.get("regular_hits", int.class),
                result.get("headshot_hits", int.class));
    }

    @Override
    public @NotNull List<BestTime> getBestTimes(@NotNull Connection connection, @NotNull Key mapKey, int maxLength)
            throws SQLException {
        List<BestTime> bestTimes = new ArrayList<>();
        try (ResultSet resultSet = using(connection).select(field("player_uuid"), field("best_time"))
                .from(table("zombies_player_map_stats")).where(field("map_key").eq(mapKey.asString()))
                .and(field("best_time").isNotNull()).orderBy(field("best_time"), field("player_uuid")).limit(maxLength)
                .fetchResultSet()) {
            int i = 0;
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("player_uuid"));
                long bestTime = resultSet.getLong("best_time");
                bestTimes.add(new BestTime(++i, uuid, bestTime));
            }
        }

        return bestTimes;
    }

    @Override
    public @NotNull Optional<BestTime> getBestTime(@NotNull Connection connection, @NotNull UUID playerUUID,
            @NotNull Key mapKey) {
        Record2<Long, Integer> result =
                using(connection).select(field("best_time", SQLDataType.BIGINT), field("rank", SQLDataType.INTEGER))
                        .from(select(field("best_time"), field("player_uuid"),
                                rowNumber().over(orderBy(field("best_time"), field("player_uuid"))).as("rank")).from(
                                        table("zombies_player_map_stats")).where(field("map_key").eq(mapKey.asString()))
                                .and(field("best_time").isNotNull()))
                        .where(field("player_uuid").eq(playerUUID.toString())).fetchOne();
        if (result == null) {
            return Optional.empty();
        }

        return Optional.of(new BestTime(result.value2(), playerUUID, result.value1()));
    }

}
