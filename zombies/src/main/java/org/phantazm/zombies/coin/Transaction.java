package org.phantazm.zombies.coin;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public record Transaction(@NotNull Collection<Modifier> modifiers, @NotNull Collection<Component> extraDisplays,
                          int initialChange) {

    public Transaction(int initialChange) {
        this(List.of(), List.of(), initialChange);
    }

    public Transaction(@NotNull Collection<Modifier> modifiers, int initialChange) {
        this(modifiers, List.of(), initialChange);
    }

    public interface Modifier {

        @NotNull Component getDisplayName();

        int modify(int coins);

        int getPriority();

    }

}
