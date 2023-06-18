package org.phantazm.zombies.stats;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record BestTime(@NotNull UUID uuid, long time) {
}
