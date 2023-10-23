package org.phantazm.core.time;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("core.tick_formatter.precision_second")
@Cache
public class PrecisionSecondTickFormatter implements TickFormatter {
    private final Data data;

    @FactoryMethod
    public PrecisionSecondTickFormatter(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull String format(long ticks) {
        return String.format("%." + data.decimalPlaces + "f", ((double) ticks / MinecraftServer.TICK_PER_SECOND));
    }

    @DataObject
    public record Data(int decimalPlaces) {
    }
}
