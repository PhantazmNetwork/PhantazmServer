package org.phantazm.zombies.modifier;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.DualComponent;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Objects;
import java.util.Set;

public class ModifierSource implements ModifierComponent {
    private final ModifierData data;
    private final DualComponent<ZombiesScene, Modifier> component;

    public ModifierSource(@NotNull ModifierData data, @NotNull DualComponent<ZombiesScene, Modifier> component) {
        this.data = Objects.requireNonNull(data);
        this.component = Objects.requireNonNull(component);
    }

    @Override
    public @NotNull Modifier apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesScene zombiesScene) {
        return component.apply(injectionStore, zombiesScene);
    }

    @Override
    public @NotNull Key key() {
        return data.key();
    }

    @Override
    public @NotNull Component displayName() {
        return data.displayName();
    }

    @Override
    public @NotNull String webhookEmoji() {
        return data.webhookEmoji();
    }

    @Override
    public @NotNull ItemStack displayItem() {
        return data.displayItem();
    }

    @Override
    public int ordinal() {
        return data.ordinal();
    }

    @Override
    public @NotNull @Unmodifiable Set<Key> exclusiveModifiers() {
        return data.exclusiveModifiers();
    }

    public @NotNull DualComponent<ZombiesScene, Modifier> component() {
        return component;
    }
}
