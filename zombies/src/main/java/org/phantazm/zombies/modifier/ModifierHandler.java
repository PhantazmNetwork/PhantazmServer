package org.phantazm.zombies.modifier;

import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.key.Key;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.*;
import java.util.stream.Collectors;

public final class ModifierHandler {
    private static final Tag<List<String>> MODIFIERS_TAG = Tag.String("zombies_modifiers").list();

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

    public @NotNull @Unmodifiable Map<Key, ModifierComponent> componentMap() {
        return components;
    }

    public boolean toggleModifier(@NotNull PlayerView player, @NotNull Key key) {
        if (!components.containsKey(key)) {
            return false;
        }

        Optional<TagHandler> persistentTagsOptional = player.persistentTags();
        if (persistentTagsOptional.isEmpty()) {
            return false;
        }

        TagHandler tagHandler = persistentTagsOptional.get();

        Wrapper<Boolean> result = Wrapper.of(false);
        tagHandler.updateTag(MODIFIERS_TAG, list -> {
            String keyString = key.asString();

            List<String> mutableCopy = list == null ? new ArrayList<>(1) : new ArrayList<>(list);
            if (!mutableCopy.contains(keyString)) {
                mutableCopy.add(keyString);
                result.set(true);
                return List.copyOf(mutableCopy);
            }

            mutableCopy.remove(keyString);
            return List.copyOf(mutableCopy);
        });

        return result.get();
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

    public @NotNull @Unmodifiable Set<Key> getModifiers(@NotNull PlayerView player) {
        return player.persistentTags().map(tagHandler -> tagHandler.getTag(MODIFIERS_TAG))
            .orElse(List.of()).stream().filter(Key::parseable).map(Key::key).collect(Collectors.toUnmodifiableSet());
    }

    public void clearModifiers(@NotNull PlayerView player) {
        player.persistentTags().ifPresent(tagHandler -> tagHandler.setTag(MODIFIERS_TAG, List.of()));
    }
}
