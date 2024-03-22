package org.phantazm.zombies.modifier;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

public final class ModifierUtils {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private ModifierUtils() {
        throw new UnsupportedOperationException();
    }

    private static boolean isDescriptorCharacter(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F');
    }

    private static int fromHex(char hex) {
        if (hex >= '0' && hex <= '9') {
            return hex - 48;
        } else if (hex >= 'A' && hex <= 'F') {
            return (hex - 65) + 10;
        }

        throw new IllegalStateException("Unexpected value: " + hex);
    }

    private static char toUpperAscii(char hex) {
        return (hex >= 'a' && hex <= 'z') ? ((char) (hex - 32)) : hex;
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

    public static @NotNull List<@NotNull ModifierComponent> fromDescriptor(
        @NotNull Int2ObjectMap<? extends ModifierComponent> ordinalMap, @NotNull String descriptor) {
        if (ordinalMap.isEmpty()) {
            return List.of();
        }

        char[] descriptorChars = descriptor.toCharArray();

        List<ModifierComponent> modifierComponents = new ArrayList<>();
        for (int i = 0; i < descriptorChars.length; i++) {
            char c = toUpperAscii(descriptorChars[descriptorChars.length - i - 1]);
            if (!isDescriptorCharacter(c)) {
                return List.of();
            }

            int mag = fromHex(c);
            int offset = i * 4;
            for (int j = 0; j < 4; j++) {
                if ((mag & (1 << j)) == 0) {
                    continue;
                }

                ModifierComponent component = ordinalMap.get(offset + j);
                if (component != null) {
                    modifierComponents.add(component);
                }
            }
        }

        return modifierComponents;
    }
}
