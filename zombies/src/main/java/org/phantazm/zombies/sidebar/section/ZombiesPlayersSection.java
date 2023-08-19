package org.phantazm.zombies.sidebar.section;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.sidebar.lineupdater.SidebarLineUpdater;
import org.phantazm.zombies.sidebar.lineupdater.creator.PlayerUpdaterCreator;

import java.util.*;

@Model("zombies.sidebar.section.zombies_players")
@Cache(false)
public class ZombiesPlayersSection implements SidebarSection {
    private final Collection<? extends ZombiesPlayer> zombiesPlayers;
    private final List<SidebarLineUpdater> lineUpdaters;
    private final PlayerUpdaterCreator playerUpdaterCreator;

    @FactoryMethod
    public ZombiesPlayersSection(@NotNull Collection<? extends ZombiesPlayer> zombiesPlayers,
        @NotNull @Child("creator_path") PlayerUpdaterCreator playerUpdaterCreator) {
        this.zombiesPlayers = Objects.requireNonNull(zombiesPlayers);
        this.lineUpdaters = new ArrayList<>(zombiesPlayers.size());
        this.playerUpdaterCreator = Objects.requireNonNull(playerUpdaterCreator);
    }

    @Override
    public void invalidateCache() {
        lineUpdaters.clear();
        for (ZombiesPlayer zombiesPlayer : zombiesPlayers) {
            lineUpdaters.add(playerUpdaterCreator.forPlayer(zombiesPlayer));
        }
    }

    @Override
    public int getSize() {
        return zombiesPlayers.size();
    }

    @Override
    public @NotNull List<Optional<Component>> update(long time) {
        List<Optional<Component>> updates = new ArrayList<>(lineUpdaters.size());
        for (SidebarLineUpdater lineUpdater : lineUpdaters) {
            updates.add(lineUpdater.tick(time));
        }

        return updates;
    }

    @DataObject
    public record Data(@NotNull @ChildPath("creator_path") String creator) {
    }
}
