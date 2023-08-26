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
    private int lastRemainingZombies = -1;

    @FactoryMethod
    public RemainingZombiesSidebarLineUpdater(@NotNull Data data, @NotNull RoundHandler roundHandler) {
        this.data = Objects.requireNonNull(data);
        this.roundSupplier = Objects.requireNonNull(roundHandler::currentRound, "roundSupplier");
    }

    @Override
    public void invalidateCache() {
        lastRemainingZombies = -1;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        return roundSupplier.get().map((Round round) -> {
            int totalMobCount = round.getTotalMobCount();
            if ((lastRemainingZombies == -1 || lastRemainingZombies != totalMobCount)) {
                lastRemainingZombies = totalMobCount;

                TagResolver remainingZombiesPlaceholder = Placeholder.component("remaining_zombies",
                    Component.text(lastRemainingZombies));
                return MiniMessage.miniMessage().deserialize(data.format, remainingZombiesPlaceholder);
            }

            return null;
        });
    }

    @DataObject
    public record Data(@NotNull String format) {
    }
}
