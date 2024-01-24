package org.phantazm.zombies.modifier;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import net.minestom.server.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.Test;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModifierUtilsTest {
    public static @NotNull Collection<ModifierComponent> makeComponents(@NotNull IntSet ordinals) {
        List<ModifierComponent> componentList = new ArrayList<>(ordinals.size());
        for (int ord : ordinals) {
            componentList.add(new ModifierComponent() {
                @Override
                public @NotNull Component displayName() {
                    return Component.empty();
                }

                @Override
                public @NotNull Component abbreviatedDisplayName() {
                    return Component.empty();
                }

                @Override
                public @NotNull String webhookEmoji() {
                    return ":skull:";
                }

                @Override
                public @NotNull ItemStack displayItem() {
                    return ItemStack.AIR;
                }

                @Override
                public int ordinal() {
                    return ord;
                }

                @Override
                public @NotNull @Unmodifiable Set<Key> exclusiveModifiers() {
                    return Set.of();
                }

                @Override
                public @NotNull Set<Permission> requiredPermissions() {
                    return Set.of();
                }

                @Override
                public @NotNull Modifier apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesScene scene) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public @NotNull Key key() {
                    return Key.key("phantazm:test_modifier");
                }
            });
        }

        return componentList;
    }

    @Test
    void emptyList() {
        assertEquals("0", ModifierUtils.modifierDescriptor(List.of()));
    }

    @Test
    void digitF() {
        assertEquals("F", ModifierUtils.modifierDescriptor(makeComponents(IntSet.of(0, 1, 2, 3))));
    }

    @Test
    void digitA() {
        assertEquals("A", ModifierUtils.modifierDescriptor(makeComponents(IntSet.of(1, 3))));
    }

    @Test
    void digitsFA() {
        assertEquals("FA", ModifierUtils.modifierDescriptor(makeComponents(IntSet.of(1, 3, 4, 5, 6, 7))));
    }
}