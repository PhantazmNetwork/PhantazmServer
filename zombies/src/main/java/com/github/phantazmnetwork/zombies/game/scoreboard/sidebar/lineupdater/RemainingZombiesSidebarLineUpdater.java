package com.github.phantazmnetwork.zombies.game.scoreboard.sidebar.lineupdater;

import com.github.phantazmnetwork.zombies.game.map.Round;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class RemainingZombiesSidebarLineUpdater implements SidebarLineUpdater {

    private final Supplier<? extends Round> roundSupplier;

    private int lastRemainingZombies = -1;

    public RemainingZombiesSidebarLineUpdater(@NotNull Supplier<? extends Round> roundSupplier) {
        this.roundSupplier = Objects.requireNonNull(roundSupplier, "roundSupplier");
    }

    @Override
    public void invalidateCache() {
        lastRemainingZombies = -1;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        Round round = roundSupplier.get();
        if (round != null) {
            int totalMobCount = round.getTotalMobCount();
            if ((lastRemainingZombies == -1 || lastRemainingZombies != totalMobCount)) {
                lastRemainingZombies = totalMobCount;
                return Optional.of(Component.textOfChildren(Component.text("Remaining Zombies: "),
                        Component.text(lastRemainingZombies, NamedTextColor.GREEN)));
            }
        }

        return Optional.empty();
    }
}
