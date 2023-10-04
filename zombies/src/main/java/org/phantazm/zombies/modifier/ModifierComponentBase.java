package org.phantazm.zombies.modifier;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class ModifierComponentBase implements ModifierComponent {
    private final Key key;
    private final Component displayName;
    private final ItemStack displayItem;
    private final int ordinal;

    protected ModifierComponentBase(@NotNull Key key, @Nullable Component displayName, @NotNull ItemStack displayItem,
        int ordinal) {
        this.key = Objects.requireNonNull(key);
        this.displayName = displayName == null ? Component.text(key.asString()) : displayName;
        this.displayItem = Objects.requireNonNull(displayItem);
        this.ordinal = ordinal;
    }

    @Override
    public final @NotNull Key key() {
        return key;
    }

    @Override
    public @NotNull Component displayName() {
        return displayName;
    }

    @Override
    public @NotNull ItemStack displayItem() {
        return displayItem;
    }

    @Override
    public int ordinal() {
        return ordinal;
    }
}
