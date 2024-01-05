package org.phantazm.core.hologram;

import com.github.steanky.toolkit.collection.Containers;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Collection;
import java.util.List;

/**
 * Represents some floating text that renders in an {@link Instance}. The hologram's lines change in accordance to the
 * {@link Component} list this object represents.
 */
public interface Hologram extends List<Component> {
    /**
     * Adds a component, created by the given format string, to this Hologram. The format string should be valid
     * {@link MiniMessage} syntax. Implementations may perform additional custom formatting and substitution. The
     * default implementation delegates to {@link List#add(int, Object)}, using the default {@link MiniMessage} instance
     * to parse the given string.
     *
     * @param index        the index to add at
     * @param formatString the format string
     */
    default void addFormatted(int index, @NotNull String formatString) {
        add(index, MiniMessage.miniMessage().deserialize(formatString));
    }

    default void addAllFormatted(int index, @NotNull Collection<? extends String> formatStrings) {
        addAll(index, Containers.mappedView(string -> MiniMessage.miniMessage().deserialize(string), formatStrings));
    }

    /**
     * Works similarly to {@link Hologram#addFormatted(int, String)}, but always appends the formatted component. Unless
     * providing additional synchronization guarantees, implementations need only override
     * {@link Hologram#addFormatted(int, String)} should they want to add custom formatting.
     *
     * @param formatString the format string
     */
    default void addFormatted(@NotNull String formatString) {
        addFormatted(size(), formatString);
    }

    default void addAllFormatted(@NotNull Collection<? extends String> formatStrings) {
        addAllFormatted(size(), formatStrings);
    }

    /**
     * Sets a component created from the given format string. The default implementation delegates to
     * {@link List#set(int, Object)}, and uses the default {@link MiniMessage} instance to format the given string.
     * <p>
     * Implementations that override this method should also override {@link Hologram#addFormatted(int, String)} if they
     * are variable-length.
     *
     * @param index        the index to set at
     * @param formatString the format string
     */
    default void setFormatted(int index, @NotNull String formatString) {
        set(index, MiniMessage.miniMessage().deserialize(formatString));
    }

    /**
     * Updates the alignment of the hologram.
     *
     * @param alignment the new alignment
     */
    void setAlignment(@NotNull Alignment alignment);

    /**
     * Gets the current location of the hologram. Depending on the alignment used, this is either the center, bottom, or
     * top of the hologram lines.
     *
     * @return the center location of the hologram
     */
    @NotNull Point getLocation();

    /**
     * Sets the current location of the hologram.
     *
     * @param location the current location of the hologram
     */
    void setLocation(@NotNull Point location);

    /**
     * Sets the current instance of this Hologram.
     *
     * @param instance the new instance
     */
    void setInstance(@NotNull Instance instance);

    void setInstance(@NotNull Instance instance, @NotNull Point location);

    /**
     * Trims internal lists to size.
     */
    void trimToSize();

    enum Alignment {
        UPPER,
        CENTERED,
        LOWER
    }
}
