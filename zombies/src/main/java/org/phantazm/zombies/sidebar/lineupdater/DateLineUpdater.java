package org.phantazm.zombies.sidebar.lineupdater;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@Model("zombies.sidebar.line_updater.date")
@Cache(false)
public class DateLineUpdater implements SidebarLineUpdater {
    private final Data data;
    private final DateTimeFormatter formatter;
    private Component cached;

    @FactoryMethod
    public DateLineUpdater(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "date");
        this.formatter = DateTimeFormatter.ofPattern(data.dateFormat);
        this.cached = null;
    }

    @Override
    public void invalidateCache() {
        cached = null;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        if (cached == null) {
            cached = Component.text(formatter.format(LocalDateTime.now())).style(data.dateStyle);
        }

        return Optional.of(cached);
    }

    @DataObject
    public record Data(@NotNull Style dateStyle, @NotNull String dateFormat) {

    }
}
