package org.phantazm.zombies.stage;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public interface Stage extends Activable, Keyed {

    boolean shouldContinue();

    boolean shouldRevert();

    void onJoin(@NotNull ZombiesPlayer zombiesPlayer);

    boolean hasPermanentPlayers();

    class Builder {

        private final Key key;

        private final Collection<Activable> activables = new ArrayList<>();

        private BooleanSupplier continueCondition;

        private BooleanSupplier revertCondition = () -> false;

        private Consumer<ZombiesPlayer> playerJoinHandler = unused -> {
        };

        private boolean hasPermanentPlayers;

        public Builder(@NotNull Key key) {
            this.key = Objects.requireNonNull(key, "key");
        }

        public @NotNull Builder setContinueCondition(@NotNull BooleanSupplier continueCondition) {
            this.continueCondition = Objects.requireNonNull(continueCondition, "continueCondition");
            return this;
        }

        public @NotNull Builder setRevertCondition(@NotNull BooleanSupplier revertCondition) {
            this.revertCondition = Objects.requireNonNull(revertCondition, "revertCondition");
            return this;
        }

        public @NotNull Builder setPlayerJoinHandler(@NotNull Consumer<ZombiesPlayer> playerJoinHandler) {
            this.playerJoinHandler = Objects.requireNonNull(playerJoinHandler, "playerJoinHandler");
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

            return new Stage() {

                @Override
                public @NotNull Key key() {
                    return key;
                }

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
                public void onJoin(@NotNull ZombiesPlayer zombiesPlayer) {
                    playerJoinHandler.accept(Objects.requireNonNull(zombiesPlayer, "zombiesPlayer"));
                }

                @Override
                public boolean hasPermanentPlayers() {
                    return hasPermanentPlayers;
                }
            };
        }

    }

}
