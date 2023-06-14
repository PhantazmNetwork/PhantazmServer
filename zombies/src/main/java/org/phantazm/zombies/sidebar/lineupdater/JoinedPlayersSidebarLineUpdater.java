package org.phantazm.zombies.sidebar.lineupdater;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.*;

@Model("zombies.sidebar.line_updater.joined_players")
@Cache(false)
public class JoinedPlayersSidebarLineUpdater implements SidebarLineUpdater {

    private final Collection<? extends ZombiesPlayer> zombiesPlayers;

    private final int maxPlayers;

    private int currentPlayers = -1;

    @FactoryMethod
    public JoinedPlayersSidebarLineUpdater(@NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers,
            int maxPlayers) {
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
            return Optional.of(Component.textOfChildren(Component.text("Players: "),
                    Component.text(currentPlayers + "/" + maxPlayers, NamedTextColor.GREEN)));
        }

        return Optional.empty();
    }
}
