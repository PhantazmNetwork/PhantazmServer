package org.phantazm.commons.properties;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Properties {
    float getProperty(@NotNull Key propertyName, @Nullable Key modifierGroup);

    void setValue(@NotNull Key propertyName, float value);

    void addModifier(@NotNull Key property, @Nullable Key modifierGroup, @NotNull Modifier modifier);

    void removeModifier(@NotNull Key property, @Nullable Key modifierGroup, @NotNull Modifier modifier);

    interface Modifier extends Comparable<Modifier> {
        float modify(float value);

        int getPriority();

        @Override
        default int compareTo(@NotNull Properties.Modifier o) {
            return Integer.compare(getPriority(), o.getPriority());
        }
    }
}
