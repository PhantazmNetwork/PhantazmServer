package org.phantazm.server.config.server;

import org.jetbrains.annotations.NotNull;

public record ZombiesGamereportConfig(
    @NotNull String playerFormat,
    @NotNull String idleStageFormat,
    @NotNull String countdownStageFormat,
    @NotNull String inGameFormat,
    @NotNull String endedFormat,
    @NotNull String gameEntryFormat,
    @NotNull String nextPageFormat,
    @NotNull String previousPageFormat,
    @NotNull String pageFormat) {
    public static final ZombiesGamereportConfig DEFAULT =
        new ZombiesGamereportConfig("", "Idle", "Countdown", "Round <current_round> - <game_time>", "Ended",
            "<current_game>/<total_games> <game_uuid><br>  <player_list><br>  <map_name>: <game_state> " +
                "<warp>[WARP]</warp>", "Click <next_page>here</next_page> for the next page",
            "Click <previous_page>here</previous_page> for the previous page",
            "<current_page>/<max_pages> Zombies Gamereport at <current_time><br><total_games> Active " +
                "Games<br><game_list><next_page_optional>");
}
