package org.phantazm.zombies.scoreboard.sidebar.lineupdater;

import com.github.steanky.element.core.annotation.Dependency;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class DateLineUpdater implements SidebarLineUpdater {

    private final Component date;

    private boolean baked = false;

    public DateLineUpdater(@NotNull @Dependency("zombies.dependency.sidebar.date") Component date) {
        this.date = Objects.requireNonNull(date, "date");
    }

    @Override
    public void invalidateCache() {
        baked = false;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        if (baked) {
            return Optional.empty();
        }

        baked = true;
        return Optional.of(date);
    }
}
