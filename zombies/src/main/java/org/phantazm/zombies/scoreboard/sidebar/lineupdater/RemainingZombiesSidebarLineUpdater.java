package org.phantazm.zombies.scoreboard.sidebar.lineupdater;

import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Round;
import org.phantazm.zombies.map.handler.RoundHandler;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Model("zombies.sidebar.lineupdater.remaining_zombies")
public class RemainingZombiesSidebarLineUpdater implements SidebarLineUpdater {

    private final Supplier<? extends Optional<Round>> roundSupplier;

    private int lastRemainingZombies = -1;

    @FactoryMethod
    public RemainingZombiesSidebarLineUpdater(
            @NotNull @Dependency("zombies.dependency.sidebar.round_handler") RoundHandler roundHandler) {
        this.roundSupplier = Objects.requireNonNull(roundHandler::currentRound, "roundSupplier");
    }

    @Override
    public void invalidateCache() {
        lastRemainingZombies = -1;
    }

    @Override
    public @NotNull Optional<Component> tick(long time) {
        return roundSupplier.get().map((Round round) -> {
            int totalMobCount = round.getTotalMobCount();
            if ((lastRemainingZombies == -1 || lastRemainingZombies != totalMobCount)) {
                lastRemainingZombies = totalMobCount;
                return Component.textOfChildren(Component.text("Remaining Zombies: "),
                        Component.text(lastRemainingZombies, NamedTextColor.GREEN));
            }

            return null;
        });
    }
}
