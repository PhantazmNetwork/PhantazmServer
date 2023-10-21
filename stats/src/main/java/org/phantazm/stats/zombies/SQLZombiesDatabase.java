package org.phantazm.stats.zombies;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.steanky.toolkit.function.ThrowingFunction;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record3;
import org.jooq.Result;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import static org.jooq.impl.DSL.*;

public class SQLZombiesDatabase implements ZombiesDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLZombiesDatabase.class);

    private final Cache<Pair<UUID, Key>, ZombiesPlayerMapStats> mapStatsCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(10))
        .build();

    private final Cache<MapBestTimesKey, Int2ObjectMap<List<BestTime>>> mapBestTimesCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(10))
        .build();

    private final Cache<MapPlayerBestTimesKey, Int2ObjectMap<BestTime>> mapPlayerBestTimesCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(10))
        .build();

    private final Executor executor;

    private final DataSource dataSource;

    public SQLZombiesDatabase(@NotNull Executor executor, @NotNull DataSource dataSource) {
        this.executor = Objects.requireNonNull(executor);
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    @Override
    public @NotNull CompletableFuture<Void> synchronizeZombiesPlayerMapStats(@NotNull ZombiesPlayerMapStats stats) {
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
        });
    }

    @Override
    @SuppressWarnings("removal") // TODO: fix the bad type stuff with least
    public @NotNull CompletableFuture<Void> synchronizeBestTime(@NotNull UUID playerUUID, @NotNull Key mapKey,
        int playerCount, @NotNull String category, long time) {
        return executeSQL(connection -> {
            using(connection).insertInto(table("zombies_player_map_best_time"), field("player_uuid"), field("map_key"),
                    field("best_time"), field("player_count"), field("category"))
                .values(playerUUID, mapKey.asString(), time, playerCount, category).onDuplicateKeyUpdate()
                .set(field("player_uuid"), playerUUID).set(field("map_key"), mapKey.asString())
                .set(field("best_time", SQLDataType.BIGINT), field("best_time", SQLDataType.BIGINT).least(time))
                .set(field("player_count"), playerCount).set(field("category"), category).execute();
        });
    }

    @Override
    public @NotNull CompletableFuture<ZombiesPlayerMapStats> getMapStats(@NotNull UUID playerUUID,
        @NotNull Key mapKey) {
        ZombiesPlayerMapStats stats = mapStatsCache.getIfPresent(Pair.of(playerUUID, mapKey));
        if (stats != null) {
            return CompletableFuture.completedFuture(stats);
        }

        return executeSQL(connection -> {
            Record result = using(connection).select().from(table("zombies_player_map_stats"))
                .where(field("player_uuid").eq(playerUUID.toString()))
                .and(field("map_key").eq(mapKey.asString()))
                .fetchOne();
            if (result == null) {
                return BasicZombiesPlayerMapStats.createBasicStats(playerUUID, mapKey);
            }

            ZombiesPlayerMapStats newStats = new BasicZombiesPlayerMapStats(playerUUID, mapKey, result.get("games_played", int.class),
                result.get("wins", int.class), result.get("best_round", int.class),
                result.get("rounds_survived", int.class), result.get("kills", int.class),
                result.get("knocks", int.class), result.get("coins_gained", int.class),
                result.get("coins_spent", int.class), result.get("deaths", int.class),
                result.get("revives", int.class), result.get("shots", int.class),
                result.get("regular_hits", int.class), result.get("headshot_hits", int.class));
            mapStatsCache.put(Pair.of(playerUUID, mapKey), newStats);
            return newStats;
        });
    }

    private record MapBestTimesKey(Key mapKey,
        int minPlayerCount,
        int maxPlayerCount,
        String category,
        int maxLength) {

    }

    @Override
    public @NotNull CompletableFuture<Int2ObjectMap<List<BestTime>>> getMapBestTimes(@NotNull Key mapKey,
        int minPlayerCount, int maxPlayerCount, @NotNull String category, int maxLength) {
        MapBestTimesKey cacheKey = new MapBestTimesKey(mapKey, minPlayerCount, maxPlayerCount, category, maxLength);
        Int2ObjectMap<List<BestTime>> times = mapBestTimesCache.getIfPresent(cacheKey);
        if (times != null) {
            return CompletableFuture.completedFuture(times);
        }

        return executeSQL(connection -> {
            Int2ObjectMap<List<BestTime>> bestTimes = new Int2ObjectOpenHashMap<>(maxPlayerCount - minPlayerCount + 1);
            try (ResultSet resultSet = using(connection).select(field("player_uuid"), field("best_time"),
                    field("player_count")).from(table("zombies_player_map_best_time"))
                .where(field("map_key").eq(mapKey.asString()))
                .and(field("player_count").between(minPlayerCount, maxPlayerCount))
                .and(field("category").eq(category))
                .orderBy(field("best_time"), field("player_uuid"))
                .fetchResultSet()) {
                while (resultSet.next()) {
                    int playerCount = resultSet.getInt("player_count");
                    List<BestTime> bestTimeList = bestTimes.computeIfAbsent(playerCount, unused -> new ArrayList<>());
                    if (bestTimeList.size() >= maxLength) {
                        continue;
                    }

                    UUID uuid = UUID.fromString(resultSet.getString("player_uuid"));
                    long bestTime = resultSet.getLong("best_time");
                    bestTimeList.add(new BestTime(bestTimeList.size() + 1, uuid, bestTime));
                }
            }
            for (int i = minPlayerCount; i <= maxPlayerCount; ++i) {
                if (!bestTimes.containsKey(i)) {
                    bestTimes.put(i, new ArrayList<>());
                }
            }

            mapBestTimesCache.put(cacheKey, bestTimes);
            return bestTimes;
        });
    }

    private record MapPlayerBestTimesKey(UUID playerUUID,
        Key mapKey,
        int minPlayerCount,
        int maxPlayerCount,
        String category) {

    }

    @Override
    public @NotNull CompletableFuture<Int2ObjectMap<BestTime>> getMapPlayerBestTimes(@NotNull UUID playerUUID,
        @NotNull Key mapKey, int minPlayerCount, int maxPlayerCount, @NotNull String category) {
        MapPlayerBestTimesKey cacheKey = new MapPlayerBestTimesKey(playerUUID, mapKey, minPlayerCount, maxPlayerCount, category);
        Int2ObjectMap<BestTime> times = mapPlayerBestTimesCache.getIfPresent(cacheKey);
        if (times != null) {
            return CompletableFuture.completedFuture(times);
        }

        return executeSQL(connection -> {
            Result<Record3<Long, Integer, Integer>> result =
                using(connection).select(field("best_time", SQLDataType.BIGINT),
                        field("player_count", SQLDataType.INTEGER), field("rank", SQLDataType.INTEGER))
                    .from(select(field("best_time"), field("player_uuid"), field("player_count"),
                        rowNumber().over(partitionBy(field("player_count")).orderBy(field("best_time"),
                            field("player_uuid"))).as("rank")).from(
                            table("zombies_player_map_best_time")).where(field("map_key").eq(mapKey.asString()))
                        .and(field("player_count").between(minPlayerCount, maxPlayerCount))
                        .and(field("category").eq(category)))
                    .where(field("player_uuid").eq(playerUUID.toString())).fetch();

            Int2ObjectMap<BestTime> newTimes = new Int2ObjectOpenHashMap<>(result.size());
            for (Record3<Long, Integer, Integer> record : result) {
                newTimes.put(record.value2().intValue(), new BestTime(record.value3(), playerUUID, record.value1()));
            }

            mapPlayerBestTimesCache.put(cacheKey, newTimes);
            return newTimes;
        });
    }

    private CompletableFuture<Void> executeSQL(Consumer<Connection> consumer) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                consumer.accept(connection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor).whenComplete(this::logException);
    }

    private <T> CompletableFuture<T> executeSQL(ThrowingFunction<Connection, T, SQLException> function) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                return function.apply(connection);
            } catch (SQLException e) {
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
