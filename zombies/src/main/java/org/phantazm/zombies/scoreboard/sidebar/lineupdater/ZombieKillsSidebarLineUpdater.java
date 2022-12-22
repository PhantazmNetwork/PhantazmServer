package org.phantazm.zombies.scoreboard.sidebar.lineupdater;

import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.kill.PlayerKills;

import java.util.Objects;
import java.util.Optional;

@Model("zombies.sidebar.lineupdater.zombie_kills")
public class ZombieKillsSidebarLineUpdater implements SidebarLineUpdater {

    private final PlayerKills playerKills;

    private int killCount = -1;

    @FactoryMethod
    public ZombieKillsSidebarLineUpdater(
            @Dependency("zombies.dependency.player.kills") @NotNull PlayerKills playerKills) {
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
                    Component.text(killCount, NamedTextColor.GREEN)));
        }

        return Optional.empty();
    }
}
