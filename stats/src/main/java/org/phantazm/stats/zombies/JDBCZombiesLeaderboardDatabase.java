package org.phantazm.stats.zombies;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.FutureUtils;
import org.phantazm.stats.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class JDBCZombiesLeaderboardDatabase implements ZombiesLeaderboardDatabase {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCZombiesLeaderboardDatabase.class);
    private static final int RECORD_HISTORY_SIZE = 100;

    private final Executor executor;
    private final DataSource dataSource;
    private final IntSet teamSizes;
    private final Set<String> validModifierKeys;

    private final Cache<BestTimesKey, List<LeaderboardEntry>> bestTimesCache;
    private final Cache<TimeHistoryKey, List<LeaderboardEntry>> timeHistoryCache;
    private final Cache<BestTimeKey, OptionalLong> bestTimeCache;
    private final Cache<BestRankingKey, Optional<RankingEntry>> bestRankingCache;

    private record BestTimesKey(int teamSize,
        @NotNull String modifierKey,
        @NotNull Key map,
        int start,
        int entries) {
    }

    private record TimeHistoryKey(@NotNull Set<UUID> team,
        @NotNull Key map,
        @NotNull String modifierKey) {
    }

    private record BestTimeKey(@NotNull Set<UUID> team,
        @NotNull Key map,
        @NotNull String modifierKey) {
    }

    private record BestRankingKey(@NotNull UUID player,
        int teamSize,
        @NotNull Key map,
        @NotNull String modifierKey) {
    }

    public JDBCZombiesLeaderboardDatabase(@NotNull Executor executor, @NotNull DataSource dataSource,
        @NotNull IntSet teamSizes, @NotNull Set<String> validModifierKeys) {
        this.executor = Objects.requireNonNull(executor);
        this.dataSource = Objects.requireNonNull(dataSource);
        this.teamSizes = filterSizes(teamSizes);
        this.validModifierKeys = filterModifierKeys(validModifierKeys);

        Caffeine<Object, Object> builder = Caffeine.newBuilder().maximumSize(1024)
            .expireAfterWrite(Duration.ofMinutes(10));

        this.bestTimesCache = builder.build();
        this.timeHistoryCache = builder.build();
        this.bestTimeCache = builder.build();
        this.bestRankingCache = builder.build();
    }

    private static IntSet filterSizes(IntSet input) {
        Objects.requireNonNull(input);

        IntIterator iterator = input.intIterator();
        IntSet set = new IntOpenHashSet(input.size());
        while (iterator.hasNext()) {
            int i = iterator.nextInt();
            if (i >= 0) {
                set.add(i);
            }
        }

        return set;
    }

    private static Set<String> filterModifierKeys(Set<String> input) {
        Objects.requireNonNull(input);

        Set<String> keys = new HashSet<>(input.size());
        for (String modifierKey : input) {
            keys.add(filterModifierKey(modifierKey));
        }

        return keys;
    }

    private static CharSequence generatePlayerColumns(int teamSize) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < teamSize; i++) {
            builder.append("    p%1$s UUID NOT NULL,\n".formatted(i + 1));
        }

        builder.append("    CONSTRAINT unique_uuids\n        unique (");

        for (int i = 0; i < teamSize; i++) {
            builder.append("p%1$s".formatted(i + 1));
            builder.append(i < teamSize - 1 ? ", " : ")");
        }

        return builder;
    }

    private static boolean isValidModifierChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
    }

    private static char toUpperAscii(char c) {
        return (c >= 'a' && c <= 'z') ? (char) (c - 32) : c;
    }

    private static String filterModifierKey(String input) {
        if (input == null) {
            return "0";
        }

        StringBuilder builder = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!isValidModifierChar(c)) {
                continue;
            }

            builder.append(toUpperAscii(c));
        }

        int i = 0;
        while (i < builder.length() && builder.charAt(i) == '0') {
            i++;
        }

        String substring = builder.substring(i);
        return substring.isEmpty() ? "0" : substring;
    }

    private static String mainTable(int teamSize, String modifierKey) {
        return """
            CREATE TABLE IF NOT EXISTS zombies_lb_%1$s_%2$s (
                id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                team_id BIGINT NOT NULL,
                time_taken BIGINT NOT NULL,
                time_end BIGINT NOT NULL,
                map_key VARCHAR(64) NOT NULL,
                CONSTRAINT fk_team_id_%1$s_%2$s
                    FOREIGN KEY (team_id) REFERENCES zombies_lb_%1$s_teams (team_id)
                    ON DELETE CASCADE
                    ON UPDATE CASCADE
            );
            """.formatted(teamSize, modifierKey);
    }

    private static String teamsTable(int teamSize) {
        return """
            CREATE TABLE IF NOT EXISTS zombies_lb_%1$s_teams (
                team_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
            %2$s
            );
            """.formatted(teamSize, generatePlayerColumns(teamSize));
    }

    private static String gameTableName(int teamSize, String modifierKey) {
        return "zombies_lb_" + teamSize + "_" + modifierKey;
    }

    private static String teamTableName(int teamSize) {
        return "zombies_lb_" + teamSize + "_teams";
    }

    private static String teamFields(int teamSize) {
        StringBuilder builder = new StringBuilder(teamSize * 2 + (teamSize - 1) * 2);

        for (int i = 0; i < teamSize; i++) {
            builder.append('p');
            builder.append(i + 1);
            if (i < teamSize - 1) {
                builder.append(", ");
            }
        }

        return builder.toString();
    }

    private static String parameters(int size) {
        StringBuilder builder = new StringBuilder(size + 2 * (size - 1));
        for (int i = 0; i < size; i++) {
            builder.append('?');
            if (i < size - 1) {
                builder.append(", ");
            }
        }

        return builder.toString();
    }

    private static String teamMatches(String teamsTable, int teamSize, String... prefixes) {
        int prefixLengthSum = 0;
        for (String prefix : prefixes) {
            prefixLengthSum += prefix.length();
        }

        int allParameters = prefixLengthSum + teamSize;
        StringBuilder builder = new StringBuilder(7 + prefixLengthSum
            + teamSize * (teamsTable.length() + 3)
            + 2 * (allParameters + 2 * (allParameters - 1)));

        builder.append('(');
        for (String prefix : prefixes) {
            builder.append(prefix);
            builder.append(", ");
        }

        for (int i = 0; i < teamSize; i++) {
            builder.append("%1$s.p%2$s".formatted(teamsTable, i + 1));

            if (i < teamSize - 1) {
                builder.append(", ");
            }
        }
        builder.append(") = (");
        for (int i = 0; i < teamSize + prefixes.length; i++) {
            builder.append('?');
            if (i < teamSize + prefixes.length - 1) {
                builder.append(", ");
            }
        }

        builder.append(')');

        return builder.toString();
    }

    private static List<UUID> key(@NotNull Set<UUID> uuids) {
        List<UUID> key = new ArrayList<>(uuids);
        Collections.sort(key);
        return key;
    }

    private static void setUuidParameters(int offset, @NotNull List<UUID> uuids,
        @NotNull PreparedStatement statement) throws SQLException {
        for (int i = 0; i < uuids.size(); i++) {
            statement.setString(i + 1 + offset, uuids.get(i).toString());
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> initTables() {
        return CompletableFuture.runAsync(() -> {
            DatabaseUtils.runSql(LOGGER, "initTables", dataSource, (connection, statement) -> {
                IntIterator iterator = teamSizes.intIterator();

                List<String> statements = new ArrayList<>();
                while (iterator.hasNext()) {
                    int teamSize = iterator.nextInt();
                    statements.add(teamsTable(teamSize));

                    for (String string : validModifierKeys) {
                        statements.add(mainTable(teamSize, string));
                    }
                }

                connection.setAutoCommit(false);

                try {
                    for (String sql : statements) {
                        statement.execute(sql);
                    }

                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                } finally {
                    connection.setAutoCommit(true);
                }
            });
        }, executor);
    }

    @SuppressWarnings("OptionalAssignedToNull")
    @Override
    public @NotNull CompletableFuture<Optional<RankingEntry>> fetchBestRanking(@NotNull UUID player, int teamSize,
        @NotNull Key map, @NotNull String modifierKey) {
        Objects.requireNonNull(map);

        String filteredModifierKey = filterModifierKey(modifierKey);
        if (teamSize == 0 || !validModifierKeys.contains(filteredModifierKey) || !teamSizes.contains(teamSize)) {
            return FutureUtils.emptyOptionalCompletedFuture();
        }

        BestRankingKey bestRankingKey = new BestRankingKey(player, teamSize, map, filteredModifierKey);
        Optional<RankingEntry> cachedRankingEntry = bestRankingCache.getIfPresent(bestRankingKey);
        if (cachedRankingEntry != null) {
            return FutureUtils.completedFuture(cachedRankingEntry);
        }

        return CompletableFuture.supplyAsync(() -> {
            String gameTable = gameTableName(teamSize, filteredModifierKey);
            String teamTable = teamTableName(teamSize);
            String teamFields = teamFields(teamSize);

            return bestRankingCache.get(bestRankingKey, ignored -> {
                return DatabaseUtils.runPreparedSql(LOGGER, "fetchBestRanking", Optional::empty, dataSource, """
                    SELECT time_taken, row_num
                    FROM
                        (
                        SELECT time_taken, id, %3$s, ROW_NUMBER() OVER (ORDER BY time_taken, id) AS row_num
                        FROM
                            (
                            SELECT id, time_taken, %3$s
                            FROM
                                (
                                SELECT id, time_taken, %3$s, DENSE_RANK() OVER (PARTITION BY %1$s.team_id ORDER BY time_taken, id) AS pos
                                FROM %1$s
                                INNER JOIN %2$s
                                ON %1$s.team_id = %2$s.team_id
                                WHERE map_key = ?
                                ) AS dummy_table
                            WHERE pos = 1
                            ) AS dummy_table_2
                        ) AS dummy_table_3
                    WHERE ? IN (%3$s)
                    ORDER BY time_taken, id
                    LIMIT 1
                    """.formatted(gameTable, teamTable, teamFields), (connection, preparedStatement) -> {
                    preparedStatement.setString(1, map.asString());
                    preparedStatement.setString(2, player.toString());
                    preparedStatement.setFetchSize(1);

                    ResultSet result = preparedStatement.executeQuery();
                    if (!result.next()) {
                        return Optional.empty();
                    }

                    return Optional.of(new RankingEntry(result.getInt(2), result.getLong(1)));
                });
            });
        }, executor);
    }

    @SuppressWarnings("OptionalAssignedToNull")
    @Override
    public @NotNull CompletableFuture<OptionalLong> fetchBestTime(@NotNull Set<UUID> team, @NotNull Key map,
        @NotNull String modifierKey) {
        Objects.requireNonNull(map);

        Set<UUID> teamCopy = Set.copyOf(team);

        int teamSize = teamCopy.size();
        String filteredModifierKey = filterModifierKey(modifierKey);
        if (teamSize == 0 || !validModifierKeys.contains(filteredModifierKey) || !teamSizes.contains(teamSize)) {
            return FutureUtils.emptyOptionalLongCompletedFuture();
        }

        BestTimeKey bestTimeKey = new BestTimeKey(teamCopy, map, filteredModifierKey);
        OptionalLong bestTimeCached = bestTimeCache.getIfPresent(bestTimeKey);
        if (bestTimeCached != null) {
            return FutureUtils.completedFuture(bestTimeCached);
        }

        return CompletableFuture.supplyAsync(() -> {
            List<UUID> key = key(teamCopy);

            String gameTable = gameTableName(teamSize, filteredModifierKey);
            String teamTable = teamTableName(teamSize);

            return bestTimeCache.get(bestTimeKey, ignored -> {
                return DatabaseUtils.runPreparedSql(LOGGER, "fetchBestTime", OptionalLong::empty, dataSource,
                    """
                        SELECT time_taken FROM %1$s
                        INNER JOIN %2$s
                        ON %1$s.team_id = %2$s.team_id
                        WHERE %3$s
                        ORDER BY time_taken
                        LIMIT 1
                        """.formatted(gameTable, teamTable, teamMatches(teamTable, key.size(), "map_key")),
                    ((connection, preparedStatement) -> {
                        preparedStatement.setString(1, map.asString());
                        setUuidParameters(1, key, preparedStatement);

                        ResultSet result = preparedStatement.executeQuery();
                        result.setFetchSize(1);
                        if (!result.next()) {
                            return OptionalLong.empty();
                        }

                        return OptionalLong.of(result.getLong(1));
                    }));
            });
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<List<LeaderboardEntry>> fetchTimeHistory(@NotNull Set<UUID> team,
        @NotNull Key map, @NotNull String modifierKey) {
        Objects.requireNonNull(map);

        Set<UUID> teamCopy = Set.copyOf(team);

        int teamSize = teamCopy.size();
        String filteredModifierKey = filterModifierKey(modifierKey);
        if (teamSize == 0 || !validModifierKeys.contains(filteredModifierKey) || !teamSizes.contains(teamSize)) {
            return FutureUtils.emptyUnmodifiableListCompletedFuture();
        }

        TimeHistoryKey timeHistoryKey = new TimeHistoryKey(teamCopy, map, filteredModifierKey);
        List<LeaderboardEntry> cachedHistory = timeHistoryCache.getIfPresent(timeHistoryKey);
        if (cachedHistory != null) {
            return FutureUtils.completedFuture(cachedHistory);
        }

        return CompletableFuture.supplyAsync(() -> {
            List<UUID> key = key(teamCopy);

            String gameTable = gameTableName(teamSize, filteredModifierKey);
            String teamTable = teamTableName(teamSize);

            return timeHistoryCache.get(timeHistoryKey, ignored -> {
                return DatabaseUtils.runPreparedSql(LOGGER, "fetchTimeHistory", List::of, dataSource, """
                        SELECT time_taken, time_end FROM %1$s
                        INNER JOIN %2$s
                        ON %1$s.team_id = %2$s.team_id
                        WHERE %3$s
                        ORDER BY time_taken, id
                        LIMIT ?
                        """.formatted(gameTable, teamTable, teamMatches(teamTable, key.size(), "map_key")),
                    (connection, preparedStatement) -> {
                        preparedStatement.setString(1, map.asString());
                        setUuidParameters(1, key, preparedStatement);
                        preparedStatement.setInt(2 + key.size(), RECORD_HISTORY_SIZE);
                        preparedStatement.setFetchSize(RECORD_HISTORY_SIZE);

                        ResultSet result = preparedStatement.executeQuery();
                        if (!result.next()) {
                            return List.of();
                        }

                        List<LeaderboardEntry> entries = new ArrayList<>();
                        do {
                            entries.add(new LeaderboardEntry(teamCopy, result.getLong(1), result
                                .getLong(2)));
                        }
                        while (result.next());

                        return List.copyOf(entries);
                    });
            });
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<List<LeaderboardEntry>> fetchBestTimes(int teamSize, @NotNull String modifierKey,
        @NotNull Key map, int start, int entries) {
        Objects.requireNonNull(map);

        String filteredModifierKey = filterModifierKey(modifierKey);
        if (entries <= 0 || teamSize <= 0 || !validModifierKeys.contains(filteredModifierKey) ||
            !teamSizes.contains(teamSize)) {
            return FutureUtils.emptyUnmodifiableListCompletedFuture();
        }

        BestTimesKey bestTimesKey = new BestTimesKey(teamSize, filteredModifierKey, map, start, entries);
        List<LeaderboardEntry> cachedEntry = bestTimesCache.getIfPresent(bestTimesKey);
        if (cachedEntry != null) {
            return FutureUtils.completedFuture(cachedEntry);
        }

        return CompletableFuture.supplyAsync(() -> {
            String gameTable = gameTableName(teamSize, filteredModifierKey);
            String teamTable = teamTableName(teamSize);

            String teamFields = teamFields(teamSize);

            @Language("sql")
            String query = """
                SELECT time_taken, time_end, %3$s FROM (SELECT id, time_taken, time_end, %3$s,
                  DENSE_RANK() OVER (PARTITION BY %1$s.team_id ORDER BY %1$s.time_taken, %1$s.id) AS pos
                    FROM %1$s
                    INNER JOIN %2$s
                    ON %1$s.team_id = %2$s.team_id
                    WHERE %1$s.map_key = ?) AS dummy_table
                    WHERE pos = 1
                    ORDER BY time_taken, id
                    LIMIT ? OFFSET ?
                """.formatted(gameTable, teamTable, teamFields);

            return bestTimesCache.get(bestTimesKey, ignored -> {
                return DatabaseUtils.runPreparedSql(LOGGER, "fetchBestTimes", List::of, dataSource, query,
                    (connection, preparedStatement) -> {
                        preparedStatement.setString(1, map.asString());
                        preparedStatement.setInt(2, entries);
                        preparedStatement.setInt(3, start);
                        preparedStatement.setFetchSize(entries);

                        ResultSet result = preparedStatement.executeQuery();
                        if (!result.next()) {
                            return List.of();
                        }

                        List<LeaderboardEntry> entryList = new ArrayList<>(entries);
                        do {
                            long timeTaken = result.getLong(1);
                            long timeEnd = result.getLong(2);

                            Set<UUID> uuids = new HashSet<>(teamSize);
                            for (int i = 0; i < teamSize; i++) {
                                uuids.add(UUID.fromString(result.getString(i + 3)));
                            }

                            entryList.add(new LeaderboardEntry(Set.copyOf(uuids), timeTaken, timeEnd));
                        }
                        while (result.next());

                        return List.copyOf(entryList);
                    });
            });
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> submitGame(@NotNull Set<UUID> team, @NotNull String modifierKey,
        @NotNull Key map, long timeTaken, long timeEnd) {
        Objects.requireNonNull(map);

        Set<UUID> teamCopy = Set.copyOf(team);

        int teamSize = teamCopy.size();
        String filteredModifierKey = filterModifierKey(modifierKey);
        if (teamSize == 0 || !validModifierKeys.contains(filteredModifierKey) || !teamSizes.contains(teamSize)) {
            return FutureUtils.nullCompletedFuture();
        }

        return CompletableFuture.runAsync(() -> {
            String gameTable = gameTableName(teamSize, filteredModifierKey);
            String teamTable = teamTableName(teamSize);

            List<UUID> key = key(teamCopy);

            DatabaseUtils.runSql(LOGGER, "submitGame", dataSource, (connection) -> {
                int oldIsolation = connection.getTransactionIsolation();
                connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                connection.setAutoCommit(false);

                String teamMatches = teamMatches(teamTable, key.size());
                String firstQuery = """
                    INSERT INTO %1$s (%2$s)
                    SELECT %4$s FROM DUAL
                    WHERE NOT EXISTS
                    (SELECT 1 FROM %1$s WHERE %3$s)
                    """.formatted(teamTable, teamFields(key.size()), teamMatches, parameters(teamSize));

                String secondQuery = """
                    INSERT INTO %1$s (team_id, time_taken, time_end, map_key)
                    VALUES ((SELECT team_id FROM %2$s WHERE %3$s), ?, ?, ?)
                    """.formatted(gameTable, teamTable, teamMatches);

                String thirdQuery = """
                    DELETE FROM %1$s
                    WHERE id IN
                    (SELECT id FROM (SELECT id, DENSE_RANK() over
                        (PARTITION BY %1$s.team_id ORDER BY %1$s.time_taken, id) AS output
                    FROM %1$s
                    INNER JOIN %2$s
                    ON %1$s.team_id = %2$s.team_id
                    WHERE %3$s) AS dummy_table
                    WHERE output > ?)
                    """.formatted(gameTable, teamTable, teamMatches(teamTable, key.size(), "map_key"));

                try (Statement statement = connection.createStatement();
                     PreparedStatement first = connection.prepareStatement(firstQuery);
                     PreparedStatement second = connection.prepareStatement(secondQuery);
                     PreparedStatement third = connection.prepareStatement(thirdQuery)) {

                    setUuidParameters(0, key, first);
                    setUuidParameters(key.size(), key, first);

                    setUuidParameters(0, key, second);
                    second.setLong(1 + key.size(), timeTaken);
                    second.setLong(2 + key.size(), timeEnd);
                    second.setString(3 + key.size(), map.asString());

                    third.setString(1, map.asString());
                    setUuidParameters(1, key, third);
                    third.setInt(2 + key.size(), RECORD_HISTORY_SIZE);

                    statement.execute("SELECT GET_LOCK('phantazm.zombies.submit_game', 60)");

                    first.execute();
                    second.execute();

                    connection.commit();

                    third.execute();
                    statement.execute("SELECT RELEASE_LOCK('phantazm.zombies.submit_game')");

                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                } finally {
                    connection.setAutoCommit(true);
                    connection.setTransactionIsolation(oldIsolation);
                }
            });
        }, executor);
    }
}