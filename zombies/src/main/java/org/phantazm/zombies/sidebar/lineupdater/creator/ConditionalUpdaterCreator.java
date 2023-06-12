package org.phantazm.zombies.sidebar.lineupdater.creator;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.sidebar.lineupdater.SidebarLineUpdater;
import org.phantazm.zombies.sidebar.lineupdater.condition.PlayerConditionCreator;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;

@Model("zombies.sidebar.line_updater.creator.conditional")
@Cache(false)
public class ConditionalUpdaterCreator implements PlayerUpdaterCreator {
    private final PlayerConditionCreator condition;
    private final PlayerUpdaterCreator successUpdater;
    private final PlayerUpdaterCreator failureUpdater;

    @FactoryMethod
    public ConditionalUpdaterCreator(@NotNull @Child("condition") PlayerConditionCreator condition,
            @NotNull @Child("success") PlayerUpdaterCreator successUpdater,
            @NotNull @Child("failure") PlayerUpdaterCreator failureUpdater) {
        this.condition = Objects.requireNonNull(condition, "condition");
        this.successUpdater = Objects.requireNonNull(successUpdater, "successUpdater");
        this.failureUpdater = Objects.requireNonNull(failureUpdater, "failureUpdater");
    }

    @Override
    public @NotNull SidebarLineUpdater forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new Updater(condition.forPlayer(zombiesPlayer), successUpdater.forPlayer(zombiesPlayer),
                failureUpdater.forPlayer(zombiesPlayer));
    }

    private static class Updater implements SidebarLineUpdater {
        private final BooleanSupplier condition;
        private final SidebarLineUpdater ifTrue;
        private final SidebarLineUpdater ifFalse;

        private boolean previousState = false;

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
                if (!previousState) {
                    invalidateCache();
                }

                previousState = true;
                return ifTrue.tick(time);
            }


            if (previousState) {
                invalidateCache();
            }

            previousState = false;
            return ifFalse.tick(time);
        }
    }

    @DataObject
    public record Data(@NotNull @ChildPath("condition") String condition,
                       @NotNull @ChildPath("success") String success,
                       @NotNull @ChildPath("failure") String failure) {
    }
}
