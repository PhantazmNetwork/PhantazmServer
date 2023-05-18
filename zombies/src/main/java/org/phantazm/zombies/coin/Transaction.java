package org.phantazm.zombies.coin;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public record Transaction(@NotNull Collection<Modifier> modifiers, int initialChange) {

    public Transaction(int initialChange) {
        this(List.of(), initialChange);
    }

    public interface Modifier {

        @NotNull Component getDisplayName();

        int modify(int coins);

        int getPriority();

    }

}
