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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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

        builder.append("""
                CONSTRAINT `unique_uuids`
                    unique (
            """);

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

    private static CharSequence simpleFilter(String input) {
        StringBuilder builder = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!isValidChar(c)) {
                continue;
            }

            builder.append(toUpperAscii(c));
        }

        return builder;
    }

    private static CharSequence mainTable(int teamSize, String modifierKey) {
        return """
            CREATE TABLE IF NOT EXISTS zombies_lb_%1$s_%2$s (
                id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                team_id BIGINT NOT NULL,
                time_taken BIGINT NOT NULL,
                time_end BIGINT NOT NULL,
                map_key VARCHAR(64) NOT NULL,
                CONSTRAINT `fk_team_id`
                    FOREIGN KEY (team_id) REFERENCES zombies_lb_%1$s_teams (team_id)
                    ON DELETE CASCADE
                    ON UPDATE CASCADE
            );
            """.formatted(teamSize, simpleFilter(modifierKey));
    }

    private static CharSequence teamsTable(int teamSize) {
        return """
            CREATE TABLE IF NOT EXISTS zombies_lb_%1$s_teams (
                team_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
            %2$s
            );
            """.formatted(teamSize, generatePlayerColumns(teamSize));
    }

    @Override
    public void initTables(@NotNull IntSet teamSizes, @NotNull Set<String> validModifierKeys) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            IntIterator iterator = teamSizes.intIterator();
            StringBuilder query = new StringBuilder();
            while (iterator.hasNext()) {
                int teamSize = iterator.nextInt();
                for (String string : validModifierKeys) {
                    query.append(mainTable(teamSize, string));
                }

                query.append(teamsTable(teamSize));
            }

            connection.setAutoCommit(false);

            try {
                statement.execute(query.toString());
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public @NotNull CompletableFuture<Long> fetchBestTime(@NotNull Set<UUID> team, @NotNull Key map,
        @NotNull String modifierKey) {
        return null;
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
