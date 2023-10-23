package org.phantazm.zombies.sidebar.section;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.sidebar.lineupdater.SidebarLineUpdater;
import org.phantazm.zombies.sidebar.lineupdater.creator.PlayerUpdaterCreator;

import java.util.*;

@Model("zombies.sidebar.section.zombies_player")
@Cache(false)
public class ZombiesPlayerSection implements SidebarSection {
    private final Map<PlayerView, ZombiesPlayer> playerMap;
    private final PlayerView playerView;
    private final PlayerUpdaterCreator creator;

    private ZombiesPlayer player;
    private SidebarLineUpdater lineUpdater;

    @FactoryMethod
    public ZombiesPlayerSection(@NotNull Map<PlayerView, ZombiesPlayer> playerMap,
        @NotNull PlayerView playerView, @NotNull @Child("line_updater") PlayerUpdaterCreator creator) {
        this.playerMap = Objects.requireNonNull(playerMap);
        this.playerView = Objects.requireNonNull(playerView);
        this.creator = Objects.requireNonNull(creator);
    }

    @Override
    public void invalidateCache() {
        lineUpdater = null;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public @NotNull List<Optional<Component>> update(long time) {
        if (player == null) {
            player = playerMap.get(playerView);
            if (player == null) {
                return List.of();
            }
        }

        if (lineUpdater == null) {
            lineUpdater = creator.forPlayer(player);
        }

        return List.of(lineUpdater.tick(time));
    }

    @DataObject
    public record Data(@NotNull @ChildPath("line_updater") String creatorPath) {
    }
}
