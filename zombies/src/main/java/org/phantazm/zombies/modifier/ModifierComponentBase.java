package org.phantazm.zombies.modifier;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Set;

public abstract class ModifierComponentBase implements ModifierComponent {
    private final Key key;
    private final Component displayName;
    private final ItemStack displayItem;
    private final int ordinal;
    private final Set<Key> exclusiveModifiers;

    protected ModifierComponentBase(@NotNull Key key, @Nullable Component displayName, @NotNull ItemStack displayItem,
        int ordinal, @NotNull Set<Key> exclusiveModifiers) {
        this.key = Objects.requireNonNull(key);
        this.displayName = displayName == null ? Component.text(key.asString()) : displayName;
        this.displayItem = Objects.requireNonNull(displayItem);
        this.ordinal = ordinal;
        this.exclusiveModifiers = Set.copyOf(exclusiveModifiers);
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

    @Override
    public @NotNull @Unmodifiable Set<Key> exclusiveModifiers() {
        return exclusiveModifiers;
    }
}
