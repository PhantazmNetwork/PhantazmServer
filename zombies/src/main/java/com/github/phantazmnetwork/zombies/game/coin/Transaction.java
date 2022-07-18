package com.github.phantazmnetwork.zombies.game.coin;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public record Transaction(@NotNull Collection<Modifier> modifiers, int initialChange) {

    public interface Modifier {

        @NotNull Component getDisplayName();

        int modify(int coins);

        int getPriority();

    }

}
