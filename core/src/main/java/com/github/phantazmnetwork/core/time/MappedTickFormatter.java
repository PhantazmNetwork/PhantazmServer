package com.github.phantazmnetwork.core.time;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class MappedTickFormatter implements TickFormatter {

    private final TickFormatter tickFormatter;

    public MappedTickFormatter(@NotNull TickFormatter tickFormatter) {
        this.tickFormatter = Objects.requireNonNull(tickFormatter, "tickFormatter");
    }

    @Override
    public @NotNull Component format(long ticks) {
        return map(tickFormatter.format(ticks));
    }

    protected abstract @NotNull Component map(@NotNull Component ticksComponent);

}
