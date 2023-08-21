package org.phantazm.mob2;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public record MobMeta(@Nullable Component customName,
    boolean customNameVisible,
    boolean isInvisible,
    boolean isGlowing,
    boolean isAggressive,
    boolean isBaby) {
}
