package org.phantazm.stats.zombies;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.CustomTable;
import org.phantazm.commons.FutureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.jooq.impl.DSL.*;
import static org.jooq.impl.SQLDataType.*;

public class BasicLeaderbordDatabase implements LeaderboardDatabase {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicLeaderbordDatabase.class);

    private static final CompletableFuture<Long> NEGATIVE_ONE = FutureUtils.completedFuture(-1L);
    private static final CompletableFuture<List<LeaderboardEntry>> EMPTY_LIST = CompletableFuture.completedFuture(List.of());

    private static final Name TEAM_ID_NAME = name("team_id");
    private static final Name PRIMARY_KEY_NAME = name("PRIMARY");
    private static final Name FK_ID_NAME = name("fk_id");
    private static final Name UUIDS_KEY_NAME = name("key_uuids");

    private static final int MAX_RECORD_HISTORY = 10;

    private static final class MainTable<R extends TableRecord<R>> extends CustomTable<R> {
        private final Class<R> dummyClass;

        private final Field<Long> ID = createField(name("id"), BIGINT.identity(true));
        private final Field<Long> TEAM_ID = createField(TEAM_ID_NAME, BIGINT.nullable(false));
        private final Field<Long> TIME_END = createField(name("time_end"), BIGINT.nullable(false));
        private final Field<Long> TIME_TAKEN = createField(name("time_taken"), BIGINT.nullable(false));
        private final Field<String> MAP_KEY = createField(name("map_key"), VARCHAR(20).nullable(false));

        private MainTable(Name name, Class<R> dummyClass) {
            super(name);
            this.dummyClass = dummyClass;
        }

        @Override
        public @NotNull Class<? extends R> getRecordType() {
            return dummyClass;
        }
    }

    private static final class TeamsTable<R extends TableRecord<R>> extends CustomTable<R> {
        private final Class<R> dummyClass;

        private final Field<Long> TEAM_ID = createField(TEAM_ID_NAME, BIGINT.identity(true));
        private final Field<UUID>[] PLAYERS;

        @SuppressWarnings("unchecked")
        private TeamsTable(Name name, Class<R> dummyClass, int size) {
            super(name);
            this.dummyClass = dummyClass;

            this.PLAYERS = new Field[size];
            for (int i = 0; i < size; i++) {
                PLAYERS[i] = createField(name("p" + (i + 1)), UUID.nullable(false));
            }
        }

        @Override
        public @NotNull Class<? extends R> getRecordType() {
            return dummyClass;
        }
    }

    private final Executor executor;
    private final DataSource dataSource;

    public BasicLeaderbordDatabase(@NotNull Executor executor, @NotNull DataSource dataSource) {
        this.executor = Objects.requireNonNull(executor);
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    private static MainTable<?> mainTable(int teamSize, @NotNull String modifierKey) {
        return new MainTable<>(name("zombies_lb_" + teamSize + "_" + modifierKey), TableRecord.class);
    }

    private static TeamsTable<?> teamsTable(int teamSize) {
        return new TeamsTable<>(name("zombies_lb_" + teamSize + "_teams"), TableRecord.class, teamSize);
    }

    private static String playerFieldName(int index) {
        return "p" + (index + 1);
    }

    private static Condition sameTeam(TeamsTable<?> teams, @NotNull Set<UUID> uuids) {
        if (uuids.isEmpty()) {
            return falseCondition();
        }

        List<UUID> sorted = new ArrayList<>(uuids);
        Collections.sort(sorted);

        Condition condition = null;
        int i = 0;
        for (UUID uuid : sorted) {
            if (condition == null) {
                condition = teams.PLAYERS[i++].eq(uuid);
                continue;
            }

            condition = condition.and(teams.PLAYERS[i++].eq(uuid));
        }

        return condition == null ? falseCondition() : condition;
    }

    @Override
    public @NotNull CompletableFuture<Void> initTables(@NotNull IntSet teamSizes, @NotNull Set<String> validModifierKeys) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Long> fetchBestTime(@NotNull Set<UUID> team, @NotNull Key map,
        @NotNull String modifierKey) {
        if (team.isEmpty()) {
            return NEGATIVE_ONE;
        }

        try (Connection connection = dataSource.getConnection()) {
            DSLContext context = using(connection);

            int teamSize = team.size();
            MainTable<?> main = mainTable(teamSize, modifierKey);
            TeamsTable<?> teams = teamsTable(teamSize);

            Record4<Long, Long, String, Long> result =
                context.select(main.TEAM_ID, main.TIME_TAKEN, main.MAP_KEY, teams.TEAM_ID)
                    .from(main)
                    .join(teams)
                    .on(main.TEAM_ID.eq(teams.TEAM_ID))
                    .where(sameTeam(teams, team).and(main.MAP_KEY.eq(map.toString())))
                    .orderBy(main.TIME_TAKEN.asc())
                    .fetchOne();

            if (result != null) {
                return CompletableFuture.completedFuture(result.value2());
            }
        } catch (SQLException ignored) {
        }

        return NEGATIVE_ONE;
    }

    @Override
    public @NotNull CompletableFuture<List<LeaderboardEntry>> fetchEntries(int teamSize, @NotNull String modifierKey,
        @NotNull Key map, int start, int entries) {
        if (teamSize == 0) {
            return EMPTY_LIST;
        }

        try (Connection connection = dataSource.getConnection()) {
            DSLContext context = using(connection);
            MainTable<?> main = mainTable(teamSize, modifierKey);
            TeamsTable<?> teams = teamsTable(teamSize);

            Field<Integer> rankField = denseRank().over()
                .partitionBy(main.TEAM_ID).orderBy(main.TIME_TAKEN.asc(), main.ID.asc());

            Field<?>[] fields = new Field[teams.PLAYERS.length + 5];
            fields[0] = main.ID;
            fields[1] = main.TEAM_ID;
            fields[2] = main.TIME_TAKEN;
            fields[3] = main.MAP_KEY;
            fields[4] = rankField;
            System.arraycopy(teams.PLAYERS, 0, fields, 5, teams.PLAYERS.length);

            Result<Record> result =
                context.select(fields)
                    .from(main)
                    .join(teams).on(main.TEAM_ID.eq(teams.TEAM_ID))
                    .where(rankField.eq(1).and(main.MAP_KEY.eq(map.toString())))
                    .orderBy(main.TIME_TAKEN.asc(), main.ID.asc())
                    .limit(start, entries)
                    .fetch();

            if (result.isEmpty()) {
                return EMPTY_LIST;
            }

            List<LeaderboardEntry> entryList = new ArrayList<>(result.size());
            for (Record record : result) {
                Set<UUID> teamMembers = new HashSet<>(teams.PLAYERS.length);
                for (int i = 5; i < record.size(); i++) {
                    teamMembers.add((java.util.UUID) record.get(i));
                }

                entryList.add(new LeaderboardEntry(teamMembers, (long) Objects.requireNonNull(record.get(1))));
            }

            return CompletableFuture.completedFuture(entryList);
        } catch (SQLException ignored) {
            return EMPTY_LIST;
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> submitGame(@NotNull Set<UUID> team, @NotNull String modifierKey,
        @NotNull Key map, long timeTaken, long timeEnd) {
        if (team.isEmpty()) {
            return FutureUtils.nullCompletedFuture();
        }

        try (Connection connection = dataSource.getConnection()) {
            DSLContext context = using(connection);

            int teamSize = team.size();
            MainTable<?> main = mainTable(teamSize, modifierKey);
            TeamsTable<?> teams = teamsTable(teamSize);

            List<UUID> players = new ArrayList<>(team);
            Collections.sort(players);

            Condition sameTeam = sameTeam(teams, team);

            context.transaction(trx -> {
                trx.dsl().queries(trx.dsl().createTableIfNotExists(main)
                    .columns(main.fields())
                    .constraints(
                        constraint(PRIMARY_KEY_NAME).primaryKey(main.ID).enforced(),
                        constraint(FK_ID_NAME).foreignKey(main.TEAM_ID).references(teams, teams.TEAM_ID)
                            .onDeleteCascade().onUpdateCascade()
                    ), trx.dsl().createTableIfNotExists(teams)
                    .columns(teams.fields())
                    .constraints(
                        constraint(PRIMARY_KEY_NAME).primaryKey(teams.TEAM_ID).enforced(),
                        constraint(UUIDS_KEY_NAME).unique(teams.PLAYERS).enforced()
                    )).executeBatch();

                trx.dsl().insertInto(teams, teams.PLAYERS).values(players).onDuplicateKeyIgnore().execute();

                trx.dsl().deleteFrom(main)
                    .whereExists(
                        select(main.ID, main.TEAM_ID, teams.TEAM_ID, main.TIME_TAKEN)
                            .from(main)
                            .join(teams).on(main.TEAM_ID.eq(teams.TEAM_ID))
                            .where(sameTeam)
                            .orderBy(main.TIME_TAKEN.asc(), main.ID.asc()).limit(MAX_RECORD_HISTORY, 10)
                    ).execute();
            });
        } catch (SQLException e) {
            LOGGER.warn("Exception when submitting game data", e);
        }

        return FutureUtils.nullCompletedFuture();
    }
}
