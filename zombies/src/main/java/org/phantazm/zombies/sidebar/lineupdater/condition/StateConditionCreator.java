package org.phantazm.zombies.sidebar.lineupdater.condition;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.function.BooleanSupplier;

@Model("zombies.sidebar.condition.state")
@Cache(false)
public class StateConditionCreator implements PlayerConditionCreator {
    private final Data data;

    @FactoryMethod
    public StateConditionCreator(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull BooleanSupplier forPlayer(@NotNull ZombiesPlayer zombiesPlayer) {
        return new Condition(data, zombiesPlayer);
    }

    private static class Condition implements BooleanSupplier {
        private final Data data;
        private final ZombiesPlayer zombiesPlayer;

        private Condition(Data data, ZombiesPlayer zombiesPlayer) {
            this.data = Objects.requireNonNull(data, "data");
            this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer, "zombiesPlayer");
        }

        @Override
        public boolean getAsBoolean() {
            return zombiesPlayer.module().getStateSwitcher().getState().key().equals(data.state) != data.requireAbsent;
        }
    }

    @DataObject
    public record Data(@NotNull Key state, boolean requireAbsent) {
    }
}
