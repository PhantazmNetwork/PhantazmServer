package org.phantazm.core.time;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("core.tick_formatter.duration")
@Cache
public class DurationTickFormatter implements TickFormatter {

    private final Data data;

    @FactoryMethod
    public DurationTickFormatter(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull String format(long ticks) {
        long elapsedSeconds;
        if (data.ceil) {
            elapsedSeconds = (long) Math.ceil((double) ticks / MinecraftServer.TICK_PER_SECOND);
        } else {
            elapsedSeconds = ticks / MinecraftServer.TICK_PER_SECOND;
        }
        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;

        StringBuilder builder = new StringBuilder();
        if (hours != 0) {
            builder.append(hours);
            if (data.verbose) {
                builder.append(" hours");
            } else {
                builder.append("h");
            }
        }
        if (minutes != 0) {
            builder.append(minutes);
            if (data.verbose) {
                builder.append(" minutes");
            } else {
                builder.append("m");
            }
        }
        if ((hours == 0 && minutes == 0) || seconds != 0) {
            builder.append(seconds);
            if (data.verbose) {
                builder.append(" seconds");
            } else {
                builder.append("s");
            }
        }

        return builder.toString();
    }

    @DataObject
    public record Data(boolean verbose,
        boolean ceil) {
    }
}
