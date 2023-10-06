package org.phantazm.zombies.modifier;

import com.github.steanky.element.core.key.Constants;
import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.key.Key;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagHandler;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.player.PlayerView;
import org.phantazm.core.scene2.SceneManager;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.*;
import java.util.stream.Collectors;

public final class ModifierHandler {
    public enum ModifierStatus {
        MODIFIER_ENABLED,
        MODIFIER_DISABLED,
        MODIFIER_ALREADY_ENABLED,
        CONFLICTING_MODIFIERS,
        INVALID_MODIFIER
    }

    public record ModifierResult(@NotNull ModifierHandler.ModifierStatus status,
        List<ModifierComponent> conflictingModifiers) {
    }

    public static ModifierResult ENABLED = new ModifierResult(ModifierStatus.MODIFIER_ENABLED, null);
    public static ModifierResult ALREADY_ENABLED = new ModifierResult(ModifierStatus.MODIFIER_ALREADY_ENABLED, null);
    public static ModifierResult DISABLED = new ModifierResult(ModifierStatus.MODIFIER_DISABLED, null);
    public static ModifierResult INVALID = new ModifierResult(ModifierStatus.INVALID_MODIFIER, null);

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

            scene.addModifier(component);

            if (modifier.needsTicking()) {
                SceneManager.Global.instance().addTickable(scene, modifier);
            }
        }
    }

    public @NotNull @Unmodifiable Map<Key, ModifierComponent> componentMap() {
        return components;
    }

    private ModifierHandler.ModifierResult modify(PlayerView player, Key key, boolean toggle) {
        ModifierComponent toggledComponent = components.get(key);
        if (toggledComponent == null) {
            return INVALID;
        }

        Optional<TagHandler> persistentTagsOptional = player.persistentTags();
        if (persistentTagsOptional.isEmpty()) {
            return INVALID;
        }

        TagHandler tagHandler = persistentTagsOptional.get();

        Wrapper<ModifierResult> result = Wrapper.of(DISABLED);

        tagHandler.updateTag(MODIFIERS_TAG, list -> {
            String keyString = key.asString();

            List<String> mutableCopy = list == null ? new ArrayList<>(1) : new ArrayList<>(list);
            if (toggle && mutableCopy.remove(keyString)) {
                return List.copyOf(mutableCopy);
            }

            List<ModifierComponent> conflictingModifiers = null;
            for (@Subst(Constants.NAMESPACE_OR_KEY) String activeModifier : mutableCopy) {
                if (!Key.parseable(activeModifier)) {
                    continue;
                }

                Key activeModifierKey = Key.key(activeModifier);
                ModifierComponent activeComponent = components.get(activeModifierKey);
                if (activeComponent == null) {
                    continue;
                }

                if (!toggle && activeModifierKey.equals(key)) {
                    result.set(ALREADY_ENABLED);
                    return list;
                }

                if (activeComponent.exclusiveModifiers().contains(key) || toggledComponent.exclusiveModifiers()
                    .contains(activeModifierKey)) {
                    if (conflictingModifiers == null) {
                        conflictingModifiers = new ArrayList<>(5);
                    }

                    conflictingModifiers.add(activeComponent);
                }
            }

            if (conflictingModifiers != null) {
                result.set(new ModifierResult(ModifierStatus.CONFLICTING_MODIFIERS, List.copyOf(conflictingModifiers)));
                return list;
            }

            mutableCopy.add(keyString);
            result.set(ENABLED);
            return List.copyOf(mutableCopy);
        });

        return result.get();
    }

    public @NotNull ModifierHandler.ModifierResult toggleModifier(@NotNull PlayerView playerView, @NotNull Key key) {
        return modify(playerView, key, true);
    }

    public @NotNull ModifierHandler.ModifierResult setModifier(@NotNull PlayerView playerView, @NotNull Key key) {
        return modify(playerView, key, false);
    }

    public @NotNull @Unmodifiable Set<Key> getModifiers(@NotNull PlayerView player) {
        return player.persistentTags().map(tagHandler -> tagHandler.getTag(MODIFIERS_TAG))
            .orElse(List.of()).stream().filter(Key::parseable).map(Key::key).collect(Collectors.toUnmodifiableSet());
    }

    public void clearModifiers(@NotNull PlayerView player) {
        player.persistentTags().ifPresent(tagHandler -> tagHandler.setTag(MODIFIERS_TAG, List.of()));
    }
}
