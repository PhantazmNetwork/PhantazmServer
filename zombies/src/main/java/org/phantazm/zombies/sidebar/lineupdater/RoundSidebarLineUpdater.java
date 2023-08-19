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
import org.phantazm.zombies.map.handler.RoundHandler;

import java.util.Objects;
import java.util.Optional;

@Model("zombies.sidebar.line_updater.round")
@Cache(false)
public class RoundSidebarLineUpdater implements SidebarLineUpdater {

    private final Data data;

    private final RoundHandler roundHandler;

    private int lastRoundIndex = -1;

    @FactoryMethod
    public RoundSidebarLineUpdater(@NotNull Data data, @NotNull RoundHandler roundHandler) {
        this.data = Objects.requireNonNull(data);
        this.roundHandler = Objects.requireNonNull(roundHandler);
    }

    @Override
    public void invalidateCache() {
        lastRoundIndex = -1;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        int newIndex = roundHandler.currentRoundIndex();
        if ((lastRoundIndex == -1 || lastRoundIndex != newIndex) && newIndex != -1) {
            lastRoundIndex = newIndex;
            TagResolver roundPlaceholder = Placeholder.component("round", Component.text(Math.min(lastRoundIndex + 1,
                roundHandler.roundCount())));
            return Optional.of(MiniMessage.miniMessage().deserialize(data.format, roundPlaceholder));
        }

        return Optional.empty();
    }

    @DataObject
    public record Data(@NotNull String format) {
    }
}
