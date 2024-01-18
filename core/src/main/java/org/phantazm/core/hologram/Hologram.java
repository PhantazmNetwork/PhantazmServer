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
public interface Hologram extends List<Hologram.Line> {
    /**
     * A function that can asynchronously format a string into a {@link Component}, given a {@link Player}.
     */
    interface FormatFunction extends BiFunction<@NotNull String, @NotNull Player, @NotNull CompletableFuture<Component>> {

    }

    /**
     * A function that accepts a {@link String} and {@link Player} and returns a {@link CompletableFuture} that computes
     * a (possibly new) Component that will be shown only to that specific player.
     * <p>
     * Instances of this interface can be obtained by calling {@link Hologram#formatter(BiFunction)} or
     * {@link Hologram#formatter(Component, FormatFunction)}, or by subclassing directly.
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
        Hologram.LineFormatter lineFormatter,
        double gap) {
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

            if (!Double.isFinite(gap)) {
                throw new IllegalArgumentException("Gap must be finite");
            }

            if (gap < 0) {
                throw new IllegalArgumentException("Gap must be >= 0");
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
    static @NotNull Line line(@NotNull String format, @NotNull Hologram.LineFormatter lineFormatter) {
        return new Line(format, null, lineFormatter, 0);
    }

    static @NotNull Line line(@NotNull String format, @NotNull Hologram.LineFormatter lineFormatter,
        double gap) {
        return new Line(format, null, lineFormatter, gap);
    }

    /**
     * Creates a new line that does not have any formatting. In most cases, it is preferable to simply add the
     * {@link Component} to the hologram using {@link Hologram#add(Object)}.
     *
     * @param component the non-formatted component that makes up the line
     * @return a new line
     */
    static @NotNull Line line(@NotNull Component component) {
        return new Line(null, component, null, 0);
    }

    static @NotNull Line line(@NotNull Component component, double gap) {
        return new Line(null, component, null, gap);
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

    static @NotNull LineFormatter formatter(@NotNull Component initialValue, @NotNull FormatFunction function) {
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

    void addComponent(@NotNull Component component, double gap);

    default void addComponent(@NotNull Component component) {
        addComponent(component, 0);
    }

    void addFormat(@NotNull String formatString, @NotNull LineFormatter lineFormatter, double gap);

    default void addFormat(@NotNull String formatString, @NotNull LineFormatter lineFormatter) {
        addFormat(formatString, lineFormatter, 0);
    }

    boolean addAllComponents(int index, @NotNull Collection<? extends Component> components, double gap);

    default boolean addAllComponents(int index, @NotNull Collection<? extends Component> components) {
        return addAllComponents(index, components, 0);
    }

    boolean addAllComponents(@NotNull Collection<? extends Component> components, double gap);

    default boolean addAllComponents(@NotNull Collection<? extends Component> components) {
        return addAllComponents(components, 0);
    }

    boolean addAllFormats(@NotNull Collection<? extends String> formatStrings, @NotNull LineFormatter lineFormatter,
        double gap);

    default boolean addAllFormats(@NotNull Collection<? extends String> formatStrings,
        @NotNull LineFormatter lineFormatter) {
        return addAllFormats(formatStrings, lineFormatter, 0);
    }

    @NotNull Component getComponent(int index);

    void destroy();

    /**
     * Ways that holograms may be aligned.
     */
    enum Alignment {
        /**
         * Hologram lines will extend below the hologram location.
         */
        UPPER,

        /**
         * Hologram lines will be centered on the hologram location.
         */
        CENTERED,

        /**
         * Hologram lines will extend above the hologram location.
         */
        LOWER
    }
}
