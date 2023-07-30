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
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;

@Model("zombies.sidebar.line_updater.joined_players")
@Cache(false)
public class JoinedPlayersSidebarLineUpdater implements SidebarLineUpdater {
    private final Data data;

    private final Collection<? extends ZombiesPlayer> zombiesPlayers;

    private final int maxPlayers;

    private int currentPlayers = -1;

    @FactoryMethod
    public JoinedPlayersSidebarLineUpdater(@NotNull Data data,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, int maxPlayers) {
        this.data = data;
        this.zombiesPlayers = zombiesPlayers.values();
        this.maxPlayers = maxPlayers;
    }

    @Override
    public void invalidateCache() {
        currentPlayers = -1;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        if (currentPlayers == -1 || currentPlayers != zombiesPlayers.size()) {
            currentPlayers = zombiesPlayers.size();

            TagResolver currentPlayerCountPlaceholder =
                    Placeholder.component("current_player_count", Component.text(currentPlayers));
            TagResolver maxPlayerCountPlaceholder =
                    Placeholder.component("max_player_count", Component.text(maxPlayers));

            return Optional.of(MiniMessage.miniMessage()
                    .deserialize(data.format, currentPlayerCountPlaceholder, maxPlayerCountPlaceholder));
        }

        return Optional.empty();
    }

    @DataObject
    public record Data(@NotNull String format) {

    }
}
