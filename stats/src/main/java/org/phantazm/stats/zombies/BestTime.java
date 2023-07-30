package org.phantazm.stats.zombies;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record BestTime(int rank, @NotNull UUID uuid, long time) {
}
