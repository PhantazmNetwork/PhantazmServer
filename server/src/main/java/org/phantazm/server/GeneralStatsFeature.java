package org.phantazm.server;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.stats.general.GeneralDatabase;
import org.phantazm.stats.general.JooqGeneralSQLFetcher;
import org.phantazm.stats.general.SQLGeneralDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

public class GeneralStatsFeature {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralStatsFeature.class);

    private static GeneralDatabase generalDatabase;

    static void initialize(@NotNull EventNode<Event> eventNode) {
        JooqGeneralSQLFetcher sqlFetcher = new JooqGeneralSQLFetcher();
        generalDatabase =
                new SQLGeneralDatabase(ExecutorFeature.getExecutor(), HikariFeature.getDataSource(), sqlFetcher);

        eventNode.addListener(PlayerSpawnEvent.class, GeneralStatsFeature::onPlayerSpawn);
    }

    private static void onPlayerSpawn(PlayerSpawnEvent event) {
        if (!event.isFirstSpawn()) {
            return;
        }

        generalDatabase.handleJoin(event.getPlayer().getUuid(), ZonedDateTime.now())
                .whenComplete((ignored, throwable) -> {
                    if (throwable != null) {
                        LOGGER.warn("Failed to update join times for {}", event.getPlayer().getUuid(), throwable);
                    }
                });
    }

}
