package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar;

import com.github.phantazmnetwork.zombies.game.ZombiesSceneState;
import com.github.phantazmnetwork.zombies.game.map.Round;
import com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.linechooser.SidebarLineChooser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public class RemainingZombiesSidebarUpdater extends SidebarUpdaterBase {

    private final Supplier<? extends Round> roundSupplier;

    private int lastRemainingZombies = -1;

    public RemainingZombiesSidebarUpdater(@NotNull String lineName, @NotNull SidebarLineChooser lineChooser,
                                          @NotNull Supplier<? extends Round> roundSupplier) {
        super(lineName, lineChooser);
        this.roundSupplier = Objects.requireNonNull(roundSupplier, "roundSupplier");
    }

    public RemainingZombiesSidebarUpdater(@NotNull SidebarLineChooser lineChooser,
                                          @NotNull Supplier<? extends Round> roundSupplier) {
        this("remaining-zombies-display", lineChooser, roundSupplier);
    }

    @Override
    public void tick(@NotNull ZombiesSceneState state, @NotNull Sidebar sidebar) {
        Round round = roundSupplier.get();
        if (round != null && (lastRemainingZombies == -1 || lastRemainingZombies != round.getTotalMobCount())) {
            lastRemainingZombies = round.getTotalMobCount();
            Component message = Component.textOfChildren(Component.text("Remaining Zombies: "),
                                                         Component.text(lastRemainingZombies, NamedTextColor.GREEN)
            );
            sidebar.updateLineContent(getLineName(), message);
        }
    }
}
