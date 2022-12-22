package org.phantazm.zombies.coin;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public record Transaction(@NotNull Collection<Modifier> modifiers, int initialChange) {

    public Transaction(int initialChange) {
        this(Collections.emptyList(), initialChange);
    }

    public interface Modifier {

        @NotNull Component getDisplayName();

        int modify(int coins);

        int getPriority();

    }

}
