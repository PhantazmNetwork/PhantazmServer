package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater;

import com.github.phantazmnetwork.zombies.game.kill.PlayerKills;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class ZombieKillsSidebarLineUpdater implements SidebarLineUpdater {

    private final PlayerKills playerKills;

    private int killCount = -1;

    public ZombieKillsSidebarLineUpdater(@NotNull PlayerKills playerKills) {
        this.playerKills = Objects.requireNonNull(playerKills, "playerKills");
    }

    @Override
    public void invalidateCache() {
        killCount = -1;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        if (killCount == -1 || killCount != playerKills.getKills()) {
            killCount = playerKills.getKills();
            return Optional.of(Component.textOfChildren(Component.text("Zombie Kills: "),
                                                        Component.text(killCount, NamedTextColor.GREEN)
            ));
        }

        return Optional.empty();
    }
}
