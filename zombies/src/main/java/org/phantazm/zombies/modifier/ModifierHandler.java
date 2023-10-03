package org.phantazm.zombies.modifier;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ModifierHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModifierHandler.class);

    private final Map<Key, ModifierComponent> components;
    private final InjectionStore injectionStore;

    public ModifierHandler(@NotNull Map<Key, ModifierComponent> components, @NotNull InjectionStore injectionStore) {
        this.components = Map.copyOf(components);
        this.injectionStore = Objects.requireNonNull(injectionStore);
    }

    public void applyModifiers(@NotNull Set<@NotNull Key> modifiers, @NotNull ZombiesScene scene) {
        Objects.requireNonNull(modifiers);
        Objects.requireNonNull(scene);

        for (Key key : modifiers) {
            ModifierComponent component = components.get(key);
            if (component == null) {
                LOGGER.warn("Tried to apply unknown modifier key {}", key);
                return;
            }

            Modifier modifier = component.apply(injectionStore, scene);
            modifier.apply();

            SceneManager.Global.instance().addTickable(scene, modifier);
        }
    }
}
