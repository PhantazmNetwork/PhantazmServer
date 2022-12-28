package org.phantazm.zombies.scoreboard.sidebar.lineupdater;

import com.github.steanky.element.core.ElementFactory;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.ethylene.mapper.type.Token;
import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.time.TickFormatter;

import java.util.Objects;
import java.util.Optional;

@Model("zombies.sidebar.lineupdater.ticks")
public class TicksLineUpdater implements SidebarLineUpdater {

    private static final ElementFactory<Data, TicksLineUpdater> FACTORY =
            (objectData, objectPath, context, dependencyProvider) -> {
                Wrapper<Long> ticksWrapper = dependencyProvider.provide(DependencyProvider.key(new Token<>() {
                }));
                TickFormatter tickFormatter =
                        context.provide(objectPath.resolve(objectData.tickFormatterPath()), dependencyProvider, false);
                return new TicksLineUpdater(ticksWrapper, tickFormatter);
            };
    private final Wrapper<Long> ticksWrapper;
    private final TickFormatter tickFormatter;
    private long lastTicks = -1;

    public TicksLineUpdater(@NotNull Wrapper<Long> ticksWrapper, @NotNull TickFormatter tickFormatter) {
        this.ticksWrapper = Objects.requireNonNull(ticksWrapper, "ticksWrapper");
        this.tickFormatter = Objects.requireNonNull(tickFormatter, "tickFormatter");
    }

    @FactoryMethod
    public static @NotNull ElementFactory<Data, TicksLineUpdater> factory() {
        return FACTORY;
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

    @DataObject
    public record Data(@NotNull String tickFormatterPath) {

        public Data {
            Objects.requireNonNull(tickFormatterPath, "tickFormatterPath");
        }

    }
}
