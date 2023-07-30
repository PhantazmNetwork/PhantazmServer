package org.phantazm.zombies.sidebar.lineupdater;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Model("zombies.sidebar.line_updater.date")
@Cache(false)
public class DateLineUpdater implements SidebarLineUpdater {
    private final Data data;
    private Component cached;

    @FactoryMethod
    public DateLineUpdater(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "date");
        this.cached = null;
    }

    @Override
    public void invalidateCache() {
        cached = null;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        if (cached == null) {
            TagResolver datePlaceholder = Formatter.date("date", LocalDateTime.now());
            cached = MiniMessage.miniMessage().deserialize(data.format, datePlaceholder);
        }

        return Optional.of(cached);
    }

    @DataObject
    public record Data(@NotNull String format) {

    }
}
