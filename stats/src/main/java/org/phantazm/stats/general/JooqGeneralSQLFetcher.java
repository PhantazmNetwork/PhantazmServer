package org.phantazm.stats.general;

import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.impl.SQLDataType;

import java.sql.Connection;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

public class JooqGeneralSQLFetcher {

    public void handleJoin(@NotNull Connection connection, @NotNull UUID playerUUID, @NotNull ZonedDateTime time) {
        long timestamp = time.toEpochSecond();
        DSLContext context = using(connection);
        context.insertInto(table("phantazm_player_stats"), field("player_uuid"), field("first_join"))
                .values(playerUUID.toString(), timestamp).onDuplicateKeyUpdate().set(field("first_join"),
                        when(field("first_join").isNull(), timestamp).otherwise(field("first_join", SQLDataType.BIGINT)))
                .execute();

        context.update(table("phantazm_player_stats")).set(field("last_join"), timestamp)
                .where(field("player_uuid").eq(playerUUID)).execute();
    }

}
