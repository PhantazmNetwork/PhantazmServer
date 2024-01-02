package org.phantazm.stats.zombies;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import org.phantazm.commons.FutureUtils;
import org.phantazm.stats.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@SuppressWarnings("SqlSourceToSinkFlow")
public class JDBCBasicLeaderboardDatabase implements LeaderboardDatabase {
    private static final Logger LOGGER = LoggerFactory.getLogger(JDBCBasicLeaderboardDatabase.class);
    private static final int RECORD_HISTORY_SIZE = 100;

    private final Executor executor;
    private final DataSource dataSource;
    private final IntSet teamSizes;
    private final Set<String> validModifierKeys;

    public JDBCBasicLeaderboardDatabase(@NotNull Executor executor, @NotNull DataSource dataSource,
        @NotNull IntSet teamSizes, @NotNull Set<String> validModifierKeys) {
        this.executor = Objects.requireNonNull(executor);
        this.dataSource = Objects.requireNonNull(dataSource);
        this.teamSizes = filterSizes(teamSizes);
        this.validModifierKeys = filterModifierKeys(validModifierKeys);
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

        builder.append("    CONSTRAINT `unique_uuids`\n        unique (");

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
                CONSTRAINT `fk_team_id_%1$s_%2$s`
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
            builder.append("p");
            builder.append(i + 1);
            if (i < teamSize - 1) {
                builder.append(", ");
            }
        }

        return builder.toString();
    }

    @VisibleForTesting
    protected static String teamMatches(String teamsTable, int teamSize, String... prefixes) {
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
            Utils.runSql(LOGGER, "initTables", dataSource, (connection, statement) -> {
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

    @Override
    public @NotNull CompletableFuture<Optional<Long>> fetchBestTime(@NotNull Set<UUID> team, @NotNull Key map,
        @NotNull String modifierKey) {
        Objects.requireNonNull(map);

        int teamSize = team.size();
        String filteredModifierKey = filterModifierKey(modifierKey);
        if (teamSize == 0 || !validModifierKeys.contains(filteredModifierKey) || !teamSizes.contains(teamSize)) {
            return FutureUtils.completedFuture(Optional.empty());
        }

        return CompletableFuture.supplyAsync(() -> {
            List<UUID> key = key(team);

            String gameTable = gameTableName(teamSize, filteredModifierKey);
            String teamTable = teamTableName(teamSize);

            return Utils.runPreparedSql(LOGGER, "fetchBestTime", Optional.empty(), dataSource, """
                SELECT time_taken FROM %1$s
                INNER JOIN %2$s
                ON %1$s.team_id = %2$s.team_id
                WHERE %3$s
                ORDER BY time_taken ASC
                LIMIT 1
                """.formatted(gameTable, teamTable, teamMatches(teamTable, key.size(), "map_key")), ((connection, preparedStatement) -> {
                preparedStatement.setString(1, map.asString());
                setUuidParameters(1, key, preparedStatement);

                ResultSet result = preparedStatement.executeQuery();
                if (!result.next()) {
                    return Optional.empty();
                }

                return Optional.of(result.getLong(1));
            }));
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<List<LeaderboardEntry>> fetchTimeHistory(@NotNull Set<UUID> team,
        @NotNull Key map, @NotNull String modifierKey) {
        Objects.requireNonNull(map);

        int teamSize = team.size();
        String filteredModifierKey = filterModifierKey(modifierKey);
        if (teamSize == 0 || !validModifierKeys.contains(filteredModifierKey) || !teamSizes.contains(teamSize)) {
            return FutureUtils.completedFuture(List.of());
        }

        return CompletableFuture.supplyAsync(() -> {
            List<UUID> key = key(team);

            String gameTable = gameTableName(teamSize, filteredModifierKey);
            String teamTable = teamTableName(teamSize);

            return Utils.runPreparedSql(LOGGER, "fetchTimeHistory", List.of(), dataSource, """
                SELECT time_taken, time_end FROM %1$s
                INNER JOIN %2$s
                ON %1$s.team_id = %2$s.team_id
                WHERE %3$s
                ORDER BY time_taken ASC, id ASC
                LIMIT ?
                """.formatted(gameTable, teamTable, teamMatches(teamTable, key.size(), "map_key")), (connection, preparedStatement) -> {
                preparedStatement.setString(1, map.asString());
                setUuidParameters(1, key, preparedStatement);
                preparedStatement.setInt(2 + key.size(), RECORD_HISTORY_SIZE);

                ResultSet result = preparedStatement.executeQuery();
                if (!result.next()) {
                    return List.of();
                }

                List<LeaderboardEntry> entries = new ArrayList<>();
                do {
                    entries.add(new LeaderboardEntry(team, result.getLong(1), result.getLong(2)));
                }
                while (result.next());

                return entries;
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
            return FutureUtils.completedFuture(List.of());
        }

        return CompletableFuture.supplyAsync(() -> {
            String gameTable = gameTableName(teamSize, filteredModifierKey);
            String teamTable = teamTableName(teamSize);

            String teamFields = teamFields(teamSize);

            return Utils.runPreparedSql(LOGGER, "fetchBestTimes", List.of(), dataSource, """
                SELECT time_taken, time_end, %3$s FROM (SELECT time_taken, time_end, %3$s,
                  DENSE_RANK() OVER (PARTITION BY %1$s.team_id ORDER BY %1$s.time_taken ASC, %1$s.id ASC) AS pos
                    FROM %1$s
                    INNER JOIN %2$s
                    ON %1$s.team_id = %2$s.team_id
                    WHERE %1$s.map_key = ?) AS dummy_table
                    WHERE pos = 1
                    LIMIT ? OFFSET ?
                """.formatted(gameTable, teamTable, teamFields), (connection, preparedStatement) -> {
                preparedStatement.setString(1, map.asString());
                preparedStatement.setInt(2, entries);
                preparedStatement.setInt(3, start);

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

                    entryList.add(new LeaderboardEntry(uuids, timeTaken, timeEnd));
                }
                while (result.next());

                return entryList;
            });
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> submitGame(@NotNull Set<UUID> team, @NotNull String modifierKey,
        @NotNull Key map, long timeTaken, long timeEnd) {
        Objects.requireNonNull(map);

        int teamSize = team.size();
        String filteredModifierKey = filterModifierKey(modifierKey);
        if (teamSize == 0 || !validModifierKeys.contains(filteredModifierKey) || !teamSizes.contains(teamSize)) {
            return FutureUtils.nullCompletedFuture();
        }


        return CompletableFuture.runAsync(() -> {
            String gameTable = gameTableName(teamSize, filteredModifierKey);
            String teamTable = teamTableName(teamSize);

            List<UUID> key = key(team);

            Utils.runSql(LOGGER, "submitGame", dataSource, (connection) -> {
                int oldIsolation = connection.getTransactionIsolation();
                connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                connection.setAutoCommit(false);

                try (Statement statement = connection.createStatement();
                     PreparedStatement first = connection.prepareStatement("""
                         INSERT INTO %1$s (%2$s)
                         SELECT (%2$s) FROM DUAL
                         WHERE NOT EXISTS
                         (SELECT %2$s FROM %1$s
                             WHERE %3$s)
                         """.formatted(teamTable, teamFields(key.size()), teamMatches(teamTable, key.size())));
                     PreparedStatement second = connection.prepareStatement("""
                         INSERT INTO %1$s (team_id, time_taken, time_end, map_key)
                         VALUES ((SELECT team_id FROM %2$s WHERE %3$s), ?, ?, ?)
                         """.formatted(gameTable, teamTable, teamMatches(teamTable, key.size())));
                     PreparedStatement third = connection.prepareStatement("""
                         DELETE FROM %1$s
                         WHERE id IN
                         (SELECT id FROM (SELECT id, DENSE_RANK() over
                             (PARTITION BY %1$s.team_id ORDER BY %1$s.time_taken ASC, id ASC) AS output
                         FROM %1$s
                         INNER JOIN %2$s
                         ON %1$s.team_id = %2$s.team_id
                         WHERE %3$s) AS dummy_table
                         WHERE output > ?)
                         """.formatted(gameTable, teamTable, teamMatches(teamTable, key.size(), "map_key")))) {

                    setUuidParameters(0, key, first);

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