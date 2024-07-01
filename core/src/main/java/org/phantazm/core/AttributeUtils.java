package org.phantazm.core;

import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class AttributeUtils {
    public static float computeWithBase(float base, @NotNull AttributeInstance instance) {
        final Collection<AttributeModifier> modifiers = instance.getModifiers();

        for (AttributeModifier modifier : modifiers) {
            if (modifier.getOperation() == AttributeOperation.ADDITION) {
                base += (float) modifier.getAmount();
            }
        }

        float result = base;
        for (AttributeModifier modifier : modifiers) {
            if (modifier.getOperation() == AttributeOperation.MULTIPLY_BASE) {
                result += (float) (base * modifier.getAmount());
            }
        }

        for (AttributeModifier modifier : modifiers) {
            if (modifier.getOperation() == AttributeOperation.MULTIPLY_TOTAL) {
                result *= (float) (1.0f + modifier.getAmount());
            }
        }

        return result;
    }
}
