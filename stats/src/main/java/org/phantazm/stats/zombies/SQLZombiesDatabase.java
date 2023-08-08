package org.phantazm.stats.zombies;

import com.github.steanky.toolkit.function.ThrowingFunction;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static org.jooq.impl.DSL.*;

public class SQLZombiesDatabase implements ZombiesDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLZombiesDatabase.class);

    private final Executor executor;

    private final DataSource dataSource;

    public SQLZombiesDatabase(@NotNull Executor executor, @NotNull DataSource dataSource) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    @Override
    public @NotNull CompletableFuture<Void> synchronizeZombiesPlayerMapStats(@NotNull ZombiesPlayerMapStats stats,
            int playerCount, @Nullable String category, @Nullable Long time) {
        return executeSQL(connection -> {
            DSLContext context = using(connection);
            context.insertInto(table("zombies_player_map_stats"), field("player_uuid"), field("map_key"),
                            field("games_played"), field("wins"), field("best_round"), field("rounds_survived"), field("kills"),
                            field("coins_gained"), field("coins_spent"), field("knocks"), field("deaths"), field("revives"),
                            field("shots"), field("regular_hits"), field("headshot_hits"))
                    .values(stats.getPlayerUUID(), stats.getMapKey().asString(), stats.getGamesPlayed(),
                            stats.getWins(), stats.getBestRound(), stats.getRoundsSurvived(), stats.getKills(),
                            stats.getCoinsGained(), stats.getCoinsSpent(), stats.getKnocks(), stats.getDeaths(),
                            stats.getRevives(), stats.getShots(), stats.getRegularHits(), stats.getHeadshotHits())
                    .onDuplicateKeyUpdate()
                    .set(field("games_played"), field("games_played", SQLDataType.INTEGER).plus(stats.getGamesPlayed()))
                    .set(field("wins"), field("wins", SQLDataType.INTEGER).plus(stats.getWins()))
                    .set(field("best_round"),
                            greatest(field("best_round", SQLDataType.INTEGER), val(stats.getBestRound())))
                    .set(field("rounds_survived"),
                            field("rounds_survived", SQLDataType.INTEGER).plus(stats.getRoundsSurvived()))
                    .set(field("kills"), field("kills", SQLDataType.INTEGER).plus(stats.getRoundsSurvived()))
                    .set(field("coins_gained"), field("coins_gained", SQLDataType.BIGINT).plus(stats.getCoinsGained()))
                    .set(field("coins_spent"), field("coins_spent", SQLDataType.BIGINT).plus(stats.getCoinsSpent()))
                    .set(field("knocks"), field("knocks", SQLDataType.INTEGER).plus(stats.getKnocks()))
                    .set(field("deaths"), field("deaths", SQLDataType.INTEGER).plus(stats.getDeaths()))
                    .set(field("revives"), field("revives", SQLDataType.INTEGER).plus(stats.getRevives()))
                    .set(field("shots"), field("shots", SQLDataType.INTEGER).plus(stats.getShots()))
                    .set(field("regular_hits"), field("regular_hits", SQLDataType.INTEGER).plus(stats.getRegularHits()))
                    .set(field("headshot_hits"),
                            field("headshot_hits", SQLDataType.INTEGER).plus(stats.getHeadshotHits())).execute();
            context.insertInto(table("zombies_player_map_best_time"), field("player_uuid"), field("map_key"),
                            field("best_time"), field("player_count"), field("category"))
                    .values(stats.getPlayerUUID(), stats.getMapKey().asString(), time, playerCount, category)
                    .onDuplicateKeyUpdate().set(field("player_uuid"), stats.getPlayerUUID())
                    .set(field("map_key"), stats.getMapKey()).set(field("best_time"), time != null
                                                                                      ? when(
                            field("best_time").isNotNull(), least(field("best_time"), time)).otherwise(time)
                                                                                      : field("best_time",
                                                                                              SQLDataType.BIGINT))
                    .set(field("player_count"), 4).execute();
        });
    }

    @Override
    public @NotNull CompletableFuture<ZombiesPlayerMapStats> getMapStats(@NotNull UUID playerUUID,
            @NotNull Key mapKey) {
        return executeSQL(connection -> {
            Record result = using(connection).select().from(table("zombies_player_map_stats"))
                    .where(field("player_uuid").eq(playerUUID.toString())).and(field("map_key").eq(mapKey.asString()))
                    .fetchOne();
            if (result == null) {
                return BasicZombiesPlayerMapStats.createBasicStats(playerUUID, mapKey);
            }

            return new BasicZombiesPlayerMapStats(playerUUID, mapKey, result.get("games_played", int.class),
                    result.get("wins", int.class), result.get("best_round", int.class),
                    result.get("rounds_survived", int.class), result.get("kills", int.class),
                    result.get("knocks", int.class), result.get("coins_gained", int.class),
                    result.get("coins_spent", int.class), result.get("deaths", int.class),
                    result.get("revives", int.class), result.get("shots", int.class),
                    result.get("regular_hits", int.class), result.get("headshot_hits", int.class));
        });
    }

    @Override
    public @NotNull CompletableFuture<List<BestTime>> getBestTimes(@NotNull Key mapKey, int playerCount,
            @Nullable String category, int maxLength) {
        return executeSQL(connection -> {
            List<BestTime> bestTimes = new ArrayList<>();
            try (ResultSet resultSet = using(connection).select(field("player_uuid"), field("best_time"))
                    .from(table("zombies_player_map_best_time")).where(field("map_key").eq(mapKey.asString()))
                    .and(field("player_count").eq(playerCount)).and(field("category").eq(category))
                    .and(field("best_time").isNotNull()).orderBy(field("best_time"), field("player_uuid"))
                    .limit(maxLength).fetchResultSet()) {
                int i = 0;
                while (resultSet.next()) {
                    UUID uuid = UUID.fromString(resultSet.getString("player_uuid"));
                    long bestTime = resultSet.getLong("best_time");
                    bestTimes.add(new BestTime(++i, uuid, bestTime));
                }
            }

            return bestTimes;
        });
    }

    @Override
    public @NotNull CompletableFuture<Optional<BestTime>> getBestTime(@NotNull UUID playerUUID, @NotNull Key mapKey,
            int playerCount, @Nullable String category) {
        return executeSQL(connection -> {
            Record2<Long, Integer> result =
                    using(connection).select(field("best_time", SQLDataType.BIGINT), field("rank", SQLDataType.INTEGER))
                            .from(select(field("best_time"), field("player_uuid"),
                                    rowNumber().over(orderBy(field("best_time"), field("player_uuid")))
                                            .as("rank")).from(table("zombies_player_map_best_time"))
                                    .where(field("map_key").eq(mapKey.asString()))
                                    .and(field("player_count").eq(playerCount)).and(field("category").eq(category))
                                    .and(field("best_time").isNotNull()))
                            .where(field("player_uuid").eq(playerUUID.toString())).fetchOne();
            if (result == null) {
                return Optional.empty();
            }

            return Optional.of(new BestTime(result.value2(), playerUUID, result.value1()));
        });
    }

    private CompletableFuture<Void> executeSQL(Consumer<Connection> consumer) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                consumer.accept(connection);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor).whenComplete(this::logException);
    }

    private <T> CompletableFuture<T> executeSQL(ThrowingFunction<Connection, T, SQLException> function) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                return function.apply(connection);
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor).whenComplete(this::logException);
    }

    private <T> void logException(T ignored, Throwable throwable) {
        if (throwable != null) {
            LOGGER.warn("Exception while querying database", throwable);
        }
    }

}
