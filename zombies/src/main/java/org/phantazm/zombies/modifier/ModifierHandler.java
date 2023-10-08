package org.phantazm.zombies.modifier;

import com.github.steanky.element.core.key.Constants;
import com.github.steanky.toolkit.collection.Wrapper;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.key.Key;
import net.minestom.server.tag.Tag;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.player.PlayerView;
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

    private static final ModifierResult ENABLED = new ModifierResult(ModifierStatus.MODIFIER_ENABLED, null);
    private static final ModifierResult ALREADY_ENABLED = new ModifierResult(ModifierStatus.MODIFIER_ALREADY_ENABLED, null);
    private static final ModifierResult DISABLED = new ModifierResult(ModifierStatus.MODIFIER_DISABLED, null);
    private static final ModifierResult INVALID = new ModifierResult(ModifierStatus.INVALID_MODIFIER, null);

    private static final Tag<List<String>> MODIFIERS_TAG = Tag.String("zombies_modifiers").list();

    private final Map<Key, ModifierComponent> components;
    private final Int2ObjectMap<ModifierComponent> ordinalMap;
    private final InjectionStore injectionStore;

    public static class Global {
        private static ModifierHandler instance;
        private static final Object GLOBAL_INITIALIZATION_LOCK = new Object();

        public static void init(@NotNull Map<Key, ModifierComponent> components, @NotNull InjectionStore injectionStore) {
            synchronized (GLOBAL_INITIALIZATION_LOCK) {
                if (instance != null) {
                    throw new IllegalStateException("The ModifierHandler has already been initialized");
                }

                instance = new ModifierHandler(components, injectionStore);
            }
        }

        public static @NotNull ModifierHandler instance() {
            ModifierHandler instance = Global.instance;
            if (instance == null) {
                throw new IllegalStateException("The ModifierHandler has not yet been initialized");
            }

            return instance;
        }
    }

    private ModifierHandler(@NotNull Map<Key, ModifierComponent> components, @NotNull InjectionStore injectionStore) {
        this.components = Map.copyOf(components);
        this.ordinalMap = new Int2ObjectOpenHashMap<>(components.size());
        for (ModifierComponent component : components.values()) {
            ordinalMap.put(component.ordinal(), component);
        }

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

            scene.addModifier(component, injectionStore);
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

        Wrapper<ModifierResult> result = Wrapper.of(DISABLED);
        player.tagHandler().updateTag(MODIFIERS_TAG, list -> {
            String keyString = key.asString();

            List<String> mutableCopy = list == null ? new ArrayList<>(1) : new ArrayList<>(list);
            if (toggle && mutableCopy.remove(keyString)) {
                return List.copyOf(mutableCopy);
            }

            Set<ModifierComponent> conflictingModifiers = null;
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
                        conflictingModifiers = new HashSet<>();
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

    public ModifierHandler.@NotNull ModifierResult setFromDescriptor(@NotNull PlayerView playerView,
        @NotNull String descriptor) {
        List<ModifierComponent> components = ModifierUtils.fromDescriptor(ordinalMap, descriptor);
        List<ModifierComponent> conflictingModifiers = searchConflicts(components);

        if (conflictingModifiers != null) {
            return new ModifierResult(ModifierStatus.CONFLICTING_MODIFIERS, List.copyOf(conflictingModifiers));
        }

        playerView.setTag(MODIFIERS_TAG, components.stream().map(modifierComponent ->
            modifierComponent.key().asString()).toList());
        return ENABLED;
    }

    private static List<ModifierComponent> searchConflicts(List<ModifierComponent> components) {
        ArrayList<ModifierComponent> conflictingModifiers = null;
        for (int i = 0; i < components.size(); i++) {
            ModifierComponent component = components.get(i);
            for (int j = i + 1; j < components.size(); j++) {
                ModifierComponent otherComponent = components.get(j);
                if (component.exclusiveModifiers().contains(otherComponent.key()) ||
                    otherComponent.exclusiveModifiers().contains(component.key())) {
                    if (conflictingModifiers == null) {
                        conflictingModifiers = new ArrayList<>();
                    }

                    conflictingModifiers.add(component);
                    conflictingModifiers.add(otherComponent);
                }
            }
        }

        return conflictingModifiers;
    }

    public @NotNull ModifierHandler.ModifierResult toggleModifier(@NotNull PlayerView playerView, @NotNull Key key) {
        return modify(playerView, key, true);
    }

    public @NotNull ModifierHandler.ModifierResult setModifier(@NotNull PlayerView playerView, @NotNull Key key) {
        return modify(playerView, key, false);
    }

    public @NotNull @Unmodifiable Set<Key> getModifiers(@NotNull PlayerView player) {
        return Optional.ofNullable(player.getTag(MODIFIERS_TAG)).orElse(List.of()).stream().filter(Key::parseable)
            .map(Key::key).collect(Collectors.toUnmodifiableSet());
    }

    public boolean hasAnyModifiers(@NotNull PlayerView player) {
        List<String> modifiers = player.getTag(MODIFIERS_TAG);
        return modifiers != null && !modifiers.isEmpty();
    }

    public void clearModifiers(@NotNull PlayerView player) {
        player.setTag(MODIFIERS_TAG, List.of());
    }
}
