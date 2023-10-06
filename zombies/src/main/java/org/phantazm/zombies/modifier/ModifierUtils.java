package org.phantazm.zombies.modifier;

import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.Collection;

public final class ModifierUtils {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private ModifierUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Given some collection of modifiers, creates a representative string that can be used to uniquely refer to the set
     * of modifiers. This string is an arbitrary-length hexidecimal number.
     * <p>
     * The number is derived based on the <i>ordinals</i> of the modifier components. Say, for example, that there are 4
     * modifiers available, with ordinals {@code 0-3} inclusive. If the given components collection contains all four,
     * the resulting string would be simply {@code F}. However, if the components array only contains, for example,
     * components with ordinals 0 and 1, the resulting string would be {@code 3}.
     *
     * @return a consistent string representing the particular modifier combination
     */
    public static @NotNull String modifierDescriptor(@NotNull Collection<? extends @NotNull ModifierComponent> components) {
        if (components.isEmpty()) {
            return "0";
        }

        BitSet bits = new BitSet();
        for (ModifierComponent modifierComponent : components) {
            bits.set(modifierComponent.ordinal());
        }

        char[] hexChars = new char[((bits.length() - 1) / 4) + 1];
        for (int i = 0; i < hexChars.length; i++) {
            int mag = 0;
            for (int j = i * 4, k = 0; j < bits.length() && k < 4; j++, k++) {
                if (bits.get(j)) mag |= 1 << k;
            }

            hexChars[hexChars.length - i - 1] = HEX_ARRAY[mag];
        }

        return String.valueOf(hexChars);
    }
}
