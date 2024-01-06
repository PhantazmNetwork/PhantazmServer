package org.phantazm.core.hologram;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * Represents some floating text that renders in an {@link Instance}. The hologram's lines change in accordance to the
 * {@link Component} list this object represents.
 */
public interface Hologram extends List<Component> {
    /**
     * A function that accepts a {@link String} and {@link Player} and returns a {@link CompletableFuture} that computes
     * a (possibly new) Component that will be shown only to that specific player.
     * <p>
     * Instances of this interface can be obtained by calling {@link Hologram#formatter(BiFunction)} or
     * {@link Hologram#formatter(Component, BiFunction)}, or by subclassing directly.
     */
    interface LineFormatter extends BiFunction<@NotNull String, @NotNull Player, @NotNull CompletableFuture<Component>> {
        /**
         * The initial value of the line. If necessary, this will be shown only while the future returned by
         * {@link LineFormatter#apply(Object, Object)} is ongoing.
         *
         * @return the initial format value
         */
        @NotNull Component initialValue();
    }

    /**
     * A line of a hologram. It is recommended to obtain instances of this class through static methods such as
     * {@link Hologram#line(Component)}.
     *
     * @param format        the format string; will be null iff component != null
     * @param component     the component, if non-null format will be null
     * @param lineFormatter non-null when format != null, null in all other cases
     */
    record Line(String format,
        Component component,
        Hologram.LineFormatter lineFormatter) {
        public Line {
            if (format == null) {
                Objects.requireNonNull(component);
                if (lineFormatter != null) {
                    throw new IllegalArgumentException("Cannot define a formatter for a Component");
                }
            } else if (component == null) {
                Objects.requireNonNull(format);
                Objects.requireNonNull(lineFormatter);
            } else {
                throw new IllegalArgumentException("Cannot define both a format string and component");
            }
        }

        /**
         * Determines if this line is a formattable line.
         *
         * @return true if this line can be formatted; false if it cannot
         */
        public boolean isFormat() {
            return format != null;
        }

        /**
         * Determines if this line is non-formattable.
         *
         * @return true if this line cannot be formatted; false if it can
         */
        public boolean isComponent() {
            return component != null;
        }
    }

    /**
     * Creates a new line that, if added to a {@link Hologram} capable of formatting, will be formatted using the given
     * {@link LineFormatter}.
     *
     * @param format        the format string, generally expected to be in MiniMessage format
     * @param lineFormatter the formatter to use
     * @return a new line
     */
    static @NotNull Hologram.Line line(@NotNull String format, @NotNull Hologram.LineFormatter lineFormatter) {
        return new Line(format, null, lineFormatter);
    }

    /**
     * Creates a new line that does not have any formatting. In most cases, it is preferable to simply add the
     * {@link Component} to the hologram using {@link Hologram#add(Object)}.
     *
     * @param component the non-formatted component that makes up the line
     * @return a new line
     */
    static @NotNull Hologram.Line line(@NotNull Component component) {
        return new Line(null, component, null);
    }

    static @NotNull LineFormatter formatter(
        @NotNull BiFunction<? super String, ? super Player, ? extends CompletableFuture<Component>> function) {
        Objects.requireNonNull(function);
        return new LineFormatter() {
            @Override
            public @NotNull Component initialValue() {
                return Component.empty();
            }

            @Override
            public CompletableFuture<Component> apply(@NotNull String string, @NotNull Player player) {
                return function.apply(string, player);
            }
        };
    }

    static @NotNull LineFormatter formatter(@NotNull Component initialValue,
        @NotNull BiFunction<? super String, ? super Player, ? extends CompletableFuture<Component>> function) {
        Objects.requireNonNull(initialValue);
        Objects.requireNonNull(function);
        return new LineFormatter() {
            @Override
            public @NotNull Component initialValue() {
                return initialValue;
            }

            @Override
            public CompletableFuture<Component> apply(@NotNull String string, @NotNull Player player) {
                return function.apply(string, player);
            }
        };
    }

    /**
     * Adds a number of lines to this hologram, which may or may not specify formatting.
     * <p>
     * Generally, it is preferable to use this method instead of calling {@link Hologram#addLine(Line)} in a loop.
     * Implementations of this class will typically rebuild the hologram entities after every line added, but need only
     * do so once when this method is used.
     *
     * @param lines the lines to add
     */
    void addLines(@NotNull Collection<? extends Line> lines);

    /**
     * Adds a single line to this hologram, which may or may not specify formatting.
     *
     * @param line the line to add
     */
    void addLine(@NotNull Hologram.Line line);

    /**
     * Updates the alignment of the hologram.
     *
     * @param alignment the new alignment
     */
    void setAlignment(@NotNull Alignment alignment);

    /**
     * Re-formats the hologram at {@code index} for player {@code player}. If the implementation does not support
     * player-specific formatting, this should be a no-op.
     *
     * @param index  the index
     * @param player the player
     */
    void reformatFor(int index, @NotNull Player player);

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
