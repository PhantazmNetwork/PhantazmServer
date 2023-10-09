package org.phantazm.zombies.modifier;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.DualComponent;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Set;

public interface ModifierComponent extends DualComponent<ZombiesScene, Modifier>, Keyed {
    @NotNull Component displayName();

    @NotNull ItemStack displayItem();

    int ordinal();

    @NotNull @Unmodifiable Set<Key> exclusiveModifiers();
}
