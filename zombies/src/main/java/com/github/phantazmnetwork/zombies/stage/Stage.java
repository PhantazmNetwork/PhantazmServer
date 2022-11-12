package com.github.phantazmnetwork.zombies.stage;

import com.github.phantazmnetwork.commons.Activable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public interface Stage extends Activable {

    boolean shouldContinue();

    boolean shouldRevert();

    boolean hasPermanentPlayers();

    class Builder {

        private final Collection<Activable> activables = new ArrayList<>();

        private BooleanSupplier continueCondition;

        private BooleanSupplier revertCondition;

        private boolean hasPermanentPlayers;

        public @NotNull Builder setContinueCondition(@NotNull BooleanSupplier continueCondition) {
            this.continueCondition = Objects.requireNonNull(continueCondition, "continueCondition");
            return this;
        }

        public @NotNull Builder setRevertCondition(@NotNull BooleanSupplier revertCondition) {
            this.revertCondition = Objects.requireNonNull(revertCondition, "revertCondition");
            return this;
        }

        public @NotNull Builder setPermanentPlayers(boolean hasPermanentPlayers) {
            this.hasPermanentPlayers = hasPermanentPlayers;
            return this;
        }

        public @NotNull Builder addActivable(@NotNull Activable activable) {
            activables.add(Objects.requireNonNull(activable, "activable"));
            return this;
        }

        public @NotNull Builder addActivables(@NotNull Collection<Activable> activables) {
            this.activables.addAll(List.copyOf(activables));
            return this;
        }

        public @NotNull Stage build() {
            Objects.requireNonNull(continueCondition, "continueCondition");
            Objects.requireNonNull(revertCondition, "revertCondition");

            return new Stage() {

                @Override
                public void start() {
                    for (Activable activable : activables) {
                        activable.start();
                    }
                }

                @Override
                public void tick(long time) {
                    for (Activable activable : activables) {
                        activable.tick(time);
                    }
                }

                @Override
                public void end() {
                    for (Activable activable : activables) {
                        activable.end();
                    }
                }

                @Override
                public boolean shouldContinue() {
                    return continueCondition.getAsBoolean();
                }

                @Override
                public boolean shouldRevert() {
                    return revertCondition.getAsBoolean();
                }

                @Override
                public boolean hasPermanentPlayers() {
                    return hasPermanentPlayers;
                }
            };
        }

    }

}
