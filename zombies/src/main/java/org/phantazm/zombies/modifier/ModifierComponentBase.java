package org.phantazm.zombies.modifier;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class ModifierComponentBase implements ModifierComponent {
    private final Key key;

    protected ModifierComponentBase(@NotNull Key key) {
        this.key = Objects.requireNonNull(key);
    }

    @Override
    public final @NotNull Key key() {
        return key;
    }
}
