package org.phantazm.server.config.server;

import org.jetbrains.annotations.NotNull;

public record ZombiesGamereportConfig(@NotNull String idleStageFormatString,
                                      @NotNull String countdownStageFormatString,
                                      @NotNull String inGameFormatString,
                                      @NotNull String endedFormatString,
                                      @NotNull String gameEntryFormatString,
                                      @NotNull String nextPageFormatString,
                                      @NotNull String pageFormatString) {
    public static final ZombiesGamereportConfig DEFAULT =
            new ZombiesGamereportConfig("Idle", "Countdown", "Round <current_round> - <game_time>", "Ended",
                    "<current_game>/<total_games> <game_uuid><br>  <player_list><br>  <map_name>: <game_state> " +
                            "<warp>[WARP]</warp>", "Click <next_page>here</next_page> for the next page",
                    "<current_page>/<max_pages> Zombies Gamereport at <current_time><br><total_games> Active " +
                            "Games<br><game_list><next_page_optional>");
}
