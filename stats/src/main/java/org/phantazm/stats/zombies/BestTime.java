package org.phantazm.stats.zombies;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record BestTime(@NotNull UUID uuid, long time) {
}
