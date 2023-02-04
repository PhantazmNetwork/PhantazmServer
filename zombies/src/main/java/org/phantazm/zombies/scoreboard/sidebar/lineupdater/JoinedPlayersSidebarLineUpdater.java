package org.phantazm.zombies.scoreboard.sidebar.lineupdater;

import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Model("zombies.sidebar.line_updater.joined_players")
public class JoinedPlayersSidebarLineUpdater implements SidebarLineUpdater {

    private final Collection<ZombiesPlayer> zombiesPlayers;

    private final int maxPlayers;

    private int currentPlayers = -1;

    @FactoryMethod
    public JoinedPlayersSidebarLineUpdater(@NotNull Collection<ZombiesPlayer> zombiesPlayers, int maxPlayers) {
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers, "zombiesPlayers");
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
