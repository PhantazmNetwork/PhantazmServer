package org.phantazm.zombies.coin;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public record Transaction(@NotNull Collection<Modifier> modifiers,
    @NotNull Collection<Component> extraDisplays,
    int initialChange) {
    public Transaction(int initialChange) {
        this(List.of(), List.of(), initialChange);
    }

    public Transaction(@NotNull Collection<Modifier> modifiers, int initialChange) {
        this(List.copyOf(modifiers), List.of(), initialChange);
    }

    public Transaction(@NotNull Collection<Modifier> modifiers, @NotNull Collection<Component> extraDisplays,
        int initialChange) {
        this.modifiers = List.copyOf(modifiers);
        this.extraDisplays = List.copyOf(extraDisplays);
        this.initialChange = initialChange;
    }

    public sealed interface Modifier permits ModifierImpl {
        enum Action {
            ADD,
            ABS_ADD,
            MULTIPLY,
            SET
        }

        @NotNull
        Component displayName();

        int modify(int coins);

        int priority();
    }

    private record ModifierImpl(Component displayName, Action action, double amount, int priority) implements Modifier {
        @Override
        public @NotNull Component displayName() {
            return displayName;
        }

        @Override
        public int modify(int coins) {
            return switch (action) {
                case ADD -> (int) Math.rint((double) coins + amount);
                case ABS_ADD -> (int) Math.rint(coins + (coins < 0 ? -amount:amount));
                case MULTIPLY -> (int) Math.rint((double) coins + (coins * amount));
                case SET -> (int) Math.rint(amount);
            };
        }
    }

    public static @NotNull Modifier modifier(@NotNull Component displayName,
        @NotNull Transaction.Modifier.Action action, double amount, int priority) {
        Objects.requireNonNull(displayName);
        Objects.requireNonNull(action);

        return new ModifierImpl(displayName, action, amount, priority);
    }

    public static @NotNull Modifier modifier(@NotNull Component displayName,
        @NotNull Transaction.Modifier.Action action, double amount) {
        return modifier(displayName, action, amount, 0);
    }
}
