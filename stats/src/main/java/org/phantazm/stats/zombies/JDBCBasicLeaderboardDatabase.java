package org.phantazm.stats.zombies;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.FutureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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
        this.teamSizes = Objects.requireNonNull(teamSizes);
        this.validModifierKeys = Objects.requireNonNull(validModifierKeys);
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

    private static String filterModifier(String input) {
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

        return builder.isEmpty() ? "0" : builder.substring(i);
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

    private static String teamMatches(String teamsTable, List<UUID> uuids) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (UUID uuid : uuids) {
            builder.append("%1$s.p%2$s = '%3$s'".formatted(teamsTable, i + 1, uuid));
            if (i < uuids.size() - 1) {
                builder.append(" AND ");
            }

            i++;
        }

        return builder.toString();
    }

    private static List<UUID> key(@NotNull Set<UUID> uuids) {
        List<UUID> key = new ArrayList<>(uuids);
        Collections.sort(key);
        return key;
    }

    private static String teamValues(@NotNull Collection<UUID> uuids) {
        int size = uuids.size();
        StringBuilder builder = new StringBuilder(36 * size + (size - 1) * 2 + size * 2);
        for (UUID uuid : uuids) {
            builder.append('\'');
            builder.append(uuid);
            builder.append('\'');
        }

        return builder.toString();
    }

    @Override
    public @NotNull CompletableFuture<Void> initTables() {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                IntIterator iterator = teamSizes.intIterator();

                List<String> statements = new ArrayList<>();
                while (iterator.hasNext()) {
                    int teamSize = iterator.nextInt();
                    statements.add(teamsTable(teamSize));

                    for (String string : validModifierKeys) {
                        statements.add(mainTable(teamSize, filterModifier(string)));
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
            } catch (SQLException e) {
                LOGGER.warn("Exception in initTables", e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Optional<Long>> fetchBestTime(@NotNull Set<UUID> team, @NotNull Key map,
        @NotNull String modifierKey) {
        int teamSize = team.size();
        String filteredModifierKey = filterModifier(modifierKey);
        if (teamSize == 0 || !validModifierKeys.contains(filteredModifierKey) || !teamSizes.contains(teamSize)) {
            return FutureUtils.completedFuture(Optional.empty());
        }

        return CompletableFuture.supplyAsync(() -> {
            List<UUID> key = key(team);

            String gameTable = gameTableName(teamSize, filteredModifierKey);
            String teamTable = teamTableName(teamSize);

            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("""
                    SELECT time_taken FROM %1$s
                    INNER JOIN %2$s
                    ON %1$s.team_id = %2$s.team_id
                    WHERE %1$s.map_key = '%4$s' AND %3$s
                    ORDER BY time_taken ASC
                    LIMIT 1
                    """.formatted(gameTable, teamTable, teamMatches(teamTable, key), map.asString()));
                if (!result.next()) {
                    return Optional.empty();
                }

                return Optional.of(result.getLong(1));
            } catch (SQLException e) {
                LOGGER.warn("Exception in fetchBestTime", e);
            }

            return Optional.empty();
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<List<LeaderboardEntry>> fetchTimeHistory(@NotNull Set<UUID> team,
        @NotNull Key map, @NotNull String modifierKey) {
        int teamSize = team.size();
        String filteredModifierKey = filterModifier(modifierKey);
        if (teamSize == 0 || !validModifierKeys.contains(filteredModifierKey) || !teamSizes.contains(teamSize)) {
            return FutureUtils.completedFuture(List.of());
        }

        return CompletableFuture.supplyAsync(() -> {
            List<UUID> key = key(team);

            String gameTable = gameTableName(teamSize, filteredModifierKey);
            String teamTable = teamTableName(teamSize);

            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("""
                    SELECT time_taken, time_end FROM %1$s
                    INNER JOIN %2$s
                    ON %1$s.team_id = %2$s.team_id
                    WHERE %1$s.map_key = '%4$s' AND %3$s
                    ORDER BY time_taken ASC, id ASC
                    LIMIT %5$s
                    """.formatted(gameTable, teamTable, teamMatches(teamTable, key), map.asString(),
                    RECORD_HISTORY_SIZE));
                if (!result.next()) {
                    return List.of();
                }

                List<LeaderboardEntry> entries = new ArrayList<>();
                do {
                    entries.add(new LeaderboardEntry(team, result.getLong(1), result.getLong(2)));
                }
                while (result.next());

                return entries;
            } catch (SQLException e) {
                LOGGER.warn("Exception in fetchTimeHistory", e);
            }

            return List.of();
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<List<LeaderboardEntry>> fetchTimes(int teamSize, @NotNull String modifierKey,
        @NotNull Key map, int start, int entries) {
        String filteredModifierKey = filterModifier(modifierKey);
        if (entries <= 0 || teamSize <= 0 || !validModifierKeys.contains(filteredModifierKey) ||
            !teamSizes.contains(teamSize)) {
            return FutureUtils.completedFuture(List.of());
        }

        return CompletableFuture.supplyAsync(() -> {
            String gameTable = gameTableName(teamSize, filteredModifierKey);
            String teamTable = teamTableName(teamSize);

            String teamFields = teamFields(teamSize);

            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                ResultSet result = statement.executeQuery("""
                    SELECT time_taken, time_end, %6$s FROM (SELECT time_taken, time_end, %6$s,
                      DENSE_RANK() OVER (PARTITION BY %1$s.team_id ORDER BY %1$s.time_taken ASC, %1$s.id ASC) AS pos
                    FROM %1$s
                    INNER JOIN %2$s
                    ON %1$s.team_id = %2$s.team_id
                    WHERE %1$s.map_key = '%3$s') AS dummy_table
                    WHERE pos = 1
                    LIMIT %5$s OFFSET %4$s
                    """.formatted(gameTable, teamTable, map.asString(), start, entries, teamFields));
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
            } catch (SQLException e) {
                LOGGER.warn("Exception in fetchEntries", e);
            }

            return List.of();
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> submitGame(@NotNull Set<UUID> team, @NotNull String modifierKey,
        @NotNull Key map, long timeTaken, long timeEnd) {
        int teamSize = team.size();
        String filteredModifierKey = filterModifier(modifierKey);
        if (teamSize == 0 || !validModifierKeys.contains(filteredModifierKey) || !teamSizes.contains(teamSize)) {
            return FutureUtils.nullCompletedFuture();
        }

        return CompletableFuture.runAsync(() -> {
            String gameTable = gameTableName(teamSize, filteredModifierKey);
            String teamTable = teamTableName(teamSize);

            List<UUID> key = key(team);

            String teamMatches = teamMatches(teamTable, key);
            String teamValues = teamValues(key);

            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {

                int oldIsolation = connection.getTransactionIsolation();
                connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                connection.setAutoCommit(false);

                try {
                    statement.execute("SELECT GET_LOCK('phantazm.zombies.submit_game', 10)");

                    statement.execute("""
                        INSERT INTO %1$s (%2$s)
                        SELECT (%3$s) FROM DUAL
                        WHERE NOT EXISTS
                            (SELECT * FROM %1$s
                            WHERE (%2$s) = (%3$s))
                        """.formatted(teamTable, teamFields(teamSize), teamValues));

                    statement.execute("""
                        INSERT INTO %1$s (team_id, time_taken, time_end, map_key)
                        VALUES ((SELECT team_id FROM %2$s WHERE %4$s), %5$s, %6$s, '%7$s')
                        """.formatted(gameTable, teamTable, map.asString(), teamMatches, timeTaken, timeEnd, map));

                    connection.commit();

                    statement.execute("""
                        DELETE FROM %1$s
                        WHERE id IN
                        (SELECT id FROM (SELECT id, DENSE_RANK() over
                            (PARTITION BY %1$s.team_id ORDER BY %1$s.time_taken ASC, id ASC) AS output
                        FROM %1$s
                        INNER JOIN %2$s
                        ON %1$s.team_id = %2$s.team_id
                        WHERE map_key = '%4$s' AND %3$s) AS dummy_table
                        WHERE output > %5$s)
                        """.formatted(gameTable, teamTable, teamMatches, map.asString(), RECORD_HISTORY_SIZE));

                    statement.execute("SELECT RELEASE_LOCK('phantazm.zombies.submit_game')");
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                } finally {
                    connection.setAutoCommit(true);
                    connection.setTransactionIsolation(oldIsolation);
                }
            } catch (SQLException e) {
                LOGGER.warn("Exception in submitGame", e);
            }
        }, executor);
    }
}