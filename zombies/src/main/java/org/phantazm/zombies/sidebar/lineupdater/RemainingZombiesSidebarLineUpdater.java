package org.phantazm.zombies.sidebar.lineupdater;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.handler.RoundHandler;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Model("zombies.sidebar.line_updater.remaining_zombies")
@Cache(false)
public class RemainingZombiesSidebarLineUpdater implements SidebarLineUpdater {
    private final Data data;
    private final Supplier<Optional<Round>> roundSupplier;
    private int lastMobCount = -1;

    @FactoryMethod
    public RemainingZombiesSidebarLineUpdater(@NotNull Data data, @NotNull RoundHandler roundHandler) {
        this.data = Objects.requireNonNull(data);
        this.roundSupplier = Objects.requireNonNull(roundHandler::currentRound);
    }

    @Override
    public void invalidateCache() {
        lastMobCount = -1;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        int newMobCount = roundSupplier.get().map(Round::totalMobCount).orElse(0);
        if ((lastMobCount == -1 || lastMobCount != newMobCount)) {
            lastMobCount = newMobCount;

            TagResolver remainingZombiesPlaceholder = Placeholder.component("remaining_zombies",
                Component.text(lastMobCount));
            return Optional.of(MiniMessage.miniMessage().deserialize(data.format, remainingZombiesPlaceholder));
        }

        return Optional.empty();
    }

    @DataObject
    public record Data(@NotNull String format) {
    }
}
