package org.phantazm.zombies.sidebar.lineupdater;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.time.TickFormatter;

import java.util.Objects;
import java.util.Optional;

@Model("zombies.sidebar.line_updater.ticks")
@Cache(false)
public class TicksLineUpdater implements SidebarLineUpdater {
    private final Data data;
    private final Wrapper<Long> ticksWrapper;
    private final TickFormatter tickFormatter;
    private long lastTicks = -1;

    @FactoryMethod
    public TicksLineUpdater(@NotNull Data data, @NotNull Wrapper<Long> ticksWrapper,
            @NotNull @Child("tick_formatter") TickFormatter tickFormatter) {
        this.data = Objects.requireNonNull(data, "data");
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

            return Optional.of(Component.join(JoinConfiguration.noSeparators(), data.prefix,
                    tickFormatter.format(ticksWrapper.get())));
        }

        return Optional.empty();
    }

    @DataObject
    public record Data(@NotNull Component prefix, @NotNull @ChildPath("tick_formatter") String tickFormatterPath) {

        public Data {
            Objects.requireNonNull(tickFormatterPath, "tickFormatterPath");
        }

    }
}
