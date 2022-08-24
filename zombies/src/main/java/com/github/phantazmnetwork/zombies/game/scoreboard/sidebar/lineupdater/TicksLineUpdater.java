package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater;

import com.github.phantazmnetwork.commons.Wrapper;
import com.github.phantazmnetwork.core.time.TickFormatter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class TicksLineUpdater implements SidebarLineUpdater {

    private final Wrapper<Long> ticksWrapper;

    private final TickFormatter tickFormatter;

    private long lastTicks = -1;

    public TicksLineUpdater(@NotNull Wrapper<Long> ticksWrapper, @NotNull TickFormatter tickFormatter) {
        this.ticksWrapper = Objects.requireNonNull(ticksWrapper, "ticksWrapper");
        this.tickFormatter = Objects.requireNonNull(tickFormatter, "tickFormatter");
    }

    @Override
    public void invalidateCache() {
        lastTicks = -1;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        if (lastTicks == -1 || lastTicks != ticksWrapper.get()) {
            lastTicks = ticksWrapper.get();
            return Optional.of(tickFormatter.format(ticksWrapper.get()));
        }

        return Optional.empty();
    }
}
