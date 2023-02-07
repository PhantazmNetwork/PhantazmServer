package org.phantazm.zombies.sidebar.lineupdater.creator;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.sidebar.lineupdater.SidebarLineUpdater;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;

@Model("zombies.sidebar.line_updater.creator.conditional")
@Cache
public class ConditionalUpdaterCreator implements PlayerUpdaterCreator {
    private final BooleanSupplier condition;
    private final PlayerUpdaterCreator successUpdater;
    private final PlayerUpdaterCreator failureUpdater;

    @FactoryMethod
    public ConditionalUpdaterCreator(@NotNull @Child("condition") BooleanSupplier condition,
            @NotNull @Child("success") PlayerUpdaterCreator successUpdater,
            @NotNull @Child("failure") PlayerUpdaterCreator failureUpdater) {
        this.condition = Objects.requireNonNull(condition, "condition");
        this.successUpdater = Objects.requireNonNull(successUpdater, "successUpdater");
        this.failureUpdater = Objects.requireNonNull(failureUpdater, "failureUpdater");
    }

    @Override
    public @NotNull SidebarLineUpdater forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new Updater(condition, successUpdater.forPlayer(zombiesPlayer), failureUpdater.forPlayer(zombiesPlayer));
    }

    private static class Updater implements SidebarLineUpdater {
        private final BooleanSupplier condition;
        private final SidebarLineUpdater ifTrue;
        private final SidebarLineUpdater ifFalse;

        private Updater(BooleanSupplier condition, SidebarLineUpdater ifTrue, SidebarLineUpdater ifFalse) {
            this.condition = condition;
            this.ifTrue = ifTrue;
            this.ifFalse = ifFalse;
        }

        @Override
        public void invalidateCache() {
            ifTrue.invalidateCache();
            ifFalse.invalidateCache();
        }

        @Override
        public @NotNull Optional<Component> tick(long time) {
            if (condition.getAsBoolean()) {
                return ifTrue.tick(time);
            }

            return ifFalse.tick(time);
        }
    }

    public record Data(@NotNull @ChildPath("condition") String conditionPath,
                       @NotNull @ChildPath("success") String successUpdaterPath,
                       @NotNull @ChildPath("failure") String failureUpdaterPath) {
    }
}
