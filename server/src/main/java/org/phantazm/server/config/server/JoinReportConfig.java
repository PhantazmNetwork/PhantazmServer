package org.phantazm.server.config.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public record JoinReportConfig(
    @NotNull Component emptyPlayers,
    @NotNull Component unrecognizedType,
    @NotNull Component alreadyJoining,
    @NotNull Component cannotProvision,
    @NotNull Component internalError) {
    public static JoinReportConfig DEFAULT = new JoinReportConfig(
        Component.text("There are no players to join!", NamedTextColor.RED),
        Component.text("The join type is unrecognized!", NamedTextColor.RED),
        Component.text("Some players are already joining a scene!", NamedTextColor.RED),
        Component.text("Could not find an existing scene, and a new scene could not be created!", NamedTextColor.RED),
        Component.text("An internal server error occurred while trying to fulfill a join. Please report this issue to server administration!", NamedTextColor.RED));
}
