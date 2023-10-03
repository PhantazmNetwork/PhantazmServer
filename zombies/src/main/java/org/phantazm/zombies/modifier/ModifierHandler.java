package org.phantazm.zombies.modifier;

import net.kyori.adventure.key.Key;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.*;

public final class ModifierHandler {
    public static final Tag<List<String>> MODIFIERS_TAG = Tag.String("zombies_modifiers").list();

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
                continue;
            }

            Modifier modifier = component.apply(injectionStore, scene);
            modifier.apply();

            if (modifier.needsTicking()) {
                SceneManager.Global.instance().addTickable(scene, modifier);
            }
        }
    }

    public @NotNull @Unmodifiable Map<Key, ModifierComponent> modifiers() {
        return components;
    }

    public void toggleModifier(@NotNull PlayerView player, @NotNull Key key) {
        if (!components.containsKey(key)) {
            return;
        }

        player.persistentTags().ifPresent(tagHandler -> {
            tagHandler.updateTag(MODIFIERS_TAG, list -> {
                String keyString = key.asString();

                List<String> mutableCopy = new ArrayList<>(list);
                if (!mutableCopy.contains(keyString)) {
                    mutableCopy.add(keyString);
                    return List.copyOf(mutableCopy);
                }

                mutableCopy.removeIf(value -> value.equals(keyString));
                return List.copyOf(mutableCopy);
            });
        });
    }

    public void setModifiers(@NotNull PlayerView player, @NotNull Set<@NotNull Key> keys) {
        player.persistentTags().ifPresent(tagHandler -> {
            List<String> stringList = new ArrayList<>(keys.size());
            for (Key key : keys) {
                if (!components.containsKey(key)) {
                    continue;
                }

                stringList.add(key.asString());
            }

            tagHandler.setTag(MODIFIERS_TAG, List.copyOf(stringList));
        });
    }

    public void clearModifiers(@NotNull PlayerView player) {
        player.persistentTags().ifPresent(tagHandler -> tagHandler.setTag(MODIFIERS_TAG, List.of()));
    }
}
