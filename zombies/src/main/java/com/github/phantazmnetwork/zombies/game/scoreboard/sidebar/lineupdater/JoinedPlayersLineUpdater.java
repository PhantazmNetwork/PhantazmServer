package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater;

import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class JoinedPlayersLineUpdater implements SidebarLineUpdater {

    private final Map<UUID, ZombiesPlayer> zombiesPlayers;

    private final int maxPlayers;

    private int currentPlayers = -1;

    public JoinedPlayersLineUpdater(@NotNull Map<UUID, ZombiesPlayer> zombiesPlayers, int maxPlayers) {
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
