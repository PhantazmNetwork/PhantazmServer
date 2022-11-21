package com.github.phantazmnetwork.commons.properties;

import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BasicProperties implements Properties {
    private static class Property {
        private float value;
        private final Map<Key, Set<Modifier>> modifiers;

        private Property(float value, Map<Key, Set<Modifier>> modifiers) {
            this.value = value;
            this.modifiers = modifiers;
        }
    }

    private final Map<Key, Property> propertyMap;

    public BasicProperties() {
        this.propertyMap = new HashMap<>(4);
    }

    @Override
    public float getProperty(@NotNull Key propertyName, @Nullable Key modifierGroup) {
        Objects.requireNonNull(propertyName, "propertyName");

        Property property = propertyMap.get(propertyName);
        if (property == null) {
            return 0;
        }

        float value = property.value;
        Collection<Modifier> modifiers = property.modifiers.getOrDefault(modifierGroup, Set.of());
        for (Modifier modifier : modifiers) {
            value = modifier.modify(value);
        }

        return value;
    }

    @Override
    public void setValue(@NotNull Key propertyName, float value) {
        Objects.requireNonNull(propertyName, "propertyName");

        if (value != 0) {
            propertyMap.computeIfAbsent(propertyName, ignored -> newProperty()).value = value;
        }
    }

    @Override
    public void addModifier(@NotNull Key propertyName, @Nullable Key modifierGroup, @NotNull Modifier modifier) {
        Objects.requireNonNull(propertyName, "propertyName");
        Objects.requireNonNull(modifier, "modifier");

        propertyMap.computeIfAbsent(propertyName, ignored -> newProperty()).modifiers.computeIfAbsent(modifierGroup,
                ignored -> newSet()).add(modifier);
    }

    @Override
    public void removeModifier(@NotNull Key propertyName, @Nullable Key modifierGroup, @NotNull Modifier modifier) {
        Objects.requireNonNull(propertyName, "propertyName");
        Objects.requireNonNull(modifier, "modifier");

        Property property = propertyMap.get(propertyName);
        if (property == null) {
            return;
        }

        Set<Modifier> modifiers = property.modifiers.get(modifierGroup);
        if (modifiers == null) {
            return;
        }

        modifiers.remove(modifier);
    }

    private static Property newProperty() {
        return new Property(0, new HashMap<>(1));
    }

    private static Set<Modifier> newSet() {
        return new ObjectRBTreeSet<>();
    }
}
