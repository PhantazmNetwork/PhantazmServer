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

    private final Executor executor;
    private final DataSource dataSource;

    public JDBCBasicLeaderboardDatabase(@NotNull Executor executor, @NotNull DataSource dataSource) {
        this.executor = Objects.requireNonNull(executor);
        this.dataSource = Objects.requireNonNull(dataSource);
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

    private static boolean isValidChar(char c) {
        return c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
    }

    private static char toUpperAscii(char c) {
        return (c >= 'a' && c <= 'z') ? (char) (c - 32) : c;
    }

    private static String simpleFilter(String input) {
        StringBuilder builder = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!isValidChar(c)) {
                continue;
            }

            builder.append(toUpperAscii(c));
        }

        return builder.toString();
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
            """.formatted(teamSize, simpleFilter(modifierKey));
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

    @Override
    public @NotNull CompletableFuture<Void> initTables(@NotNull IntSet teamSizes,
        @NotNull Set<String> validModifierKeys) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
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
            } catch (SQLException e) {
                LOGGER.warn("Exception in initTables", e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Long> fetchBestTime(@NotNull Set<UUID> team, @NotNull Key map,
        @NotNull String modifierKey) {
        if (team.isEmpty()) {
            return FutureUtils.completedFuture(-1L);
        }

        List<UUID> key = new ArrayList<>(team);
        Collections.sort(key);

        int teamSize = team.size();
        String gameTable = gameTableName(teamSize, simpleFilter(modifierKey));
        String teamTable = teamTableName(teamSize);

        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                String query = """
                    SELECT time_taken FROM %1$s
                    INNER JOIN %2$s
                    ON %1$s.team_id = %2$s.team_id
                    WHERE %1$s.map_key = '%4$s' AND %3$s
                    ORDER BY time_taken ASC
                    LIMIT 1
                    """.formatted(gameTable, teamTable, teamMatches(teamTable, key), map.asString());

                ResultSet result = statement.executeQuery(query);
                if (!result.next()) {
                    return -1L;
                }

                return result.getLong(1);
            } catch (SQLException e) {
                LOGGER.warn("Exception in fetchBestTime", e);
            }

            return -1L;
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<List<LeaderboardEntry>> fetchEntries(int teamSize, @NotNull String modifierKey,
        @NotNull Key map, int start, int entries) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Void> submitGame(@NotNull Set<UUID> team, @NotNull String modifierKey,
        @NotNull Key map, long timeTaken, long timeEnd) {
        return null;
    }
}
