package com.github.phantazmnetwork.zombies.mapeditor.client.render;

import net.kyori.adventure.key.Key;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Simple rendering interface used to visually display named objects.
 */
public interface ObjectRenderer {
    /**
     * The amount by which to expand bounding boxes to reduce Z-fighting.
     */
    float EPSILON = 1E-3F;

    /**
     * EPSILON * 2
     */
    float DOUBLE_EPSILON = EPSILON * 2;

    /**
     * Removes a {@link RenderObject} with the given name from the render list, stopping it from appearing.
     *
     * @param key the id of the object to remove
     */
    void removeObject(@NotNull Key key);

    /**
     * Removes all {@link RenderObject} instances whose key matches the given predicate.
     *
     * @param keyPredicate the predicate determining which objects to remove
     */
    void removeIf(@NotNull Predicate<? super Key> keyPredicate);

    /**
     * Enumerates every {@link RenderObject}, calling the given Consumer with every instance.
     *
     * @param consumer the consumer to call
     */
    void forEach(@NotNull Consumer<? super RenderObject> consumer);

    /**
     * Adds an object to this renderer.
     *
     * @param value the object to add
     */
    void putObject(@NotNull RenderObject value);

    /**
     * If true, this renderer will render ALL objects through even opaque blocks. If false, this renderer will only
     * render through opaque blocks for RenderObject instances which have that parameter enabled.
     *
     * @param rendersThroughWalls true if this renderer should force render through walls, false otherwise
     */
    void setRenderThroughWalls(boolean rendersThroughWalls);

    /**
     * Determines if a RenderObject with the given key exists in this renderer.
     *
     * @param key the key to check for
     * @return true if an object with this key exists, false otherwise
     */
    boolean hasObject(@NotNull Key key);

    /**
     * Determines if this ObjectRenderer is currently enabled or disabled.
     *
     * @return true if enabled, false if disabled
     */
    boolean isEnabled();

    /**
     * Enables or disables the renderer. When the renderer is disabled, nothing will render.
     *
     * @param enabled true to enabled, false to disabled
     */
    void setEnabled(boolean enabled);

    /**
     * Returns the "size" of this renderer (number of registered RenderObjects). Invisible RenderObjects will count
     * towards this total.
     *
     * @return the number of registered RenderObjects
     */
    int size();

    /**
     * Removes all RenderObject instances from this renderer.
     */
    void clear();

    /**
     * Describes how a RenderObject should be rendered (either filled or wireframe outline).
     */
    enum RenderType {
        /**
         * Filled (object renders as a solid cube)
         */
        FILLED,

        /**
         * Outline (object renders as a wireframe)
         */
        OUTLINE
    }

    /**
     * A named, renderable object.
     */
    final class RenderObject {
        /**
         * The name of this object.
         */
        public final Key key;

        /**
         * How this object should be rendered.
         */
        public final RenderType type;

        /**
         * The {@link Color} of this object.
         */
        public final Color color;

        /**
         * If this object should render. If true, this object is visible. If false, this object is invisible.
         */
        public final boolean shouldRender;

        /**
         * If true, this object should be visible even through opaque blocks. If false, this object will be blocked from
         * view when behind opaque objects.
         */
        public final boolean renderThroughWalls;

        /**
         * An array of {@link Vec3d} objects representing the corners of all bounding boxes to be displayed by this
         * object.
         */
        public final Vec3d[] bounds;

        /**
         * Creates a new instance of this class, with the specified initial values.
         *
         * @param key                the name of this object
         * @param type               the initial RenderType
         * @param color              the initial Color
         * @param shouldRender       the initial visibility state (true if visible, false if invisible)
         * @param renderThroughWalls if this object should initially render through walls or not
         * @param bounds             the initial bounds array representing the corners of the bounding boxes to be displayed by this
         *                           object
         */
        public RenderObject(@NotNull Key key, @NotNull RenderType type, @NotNull Color color, boolean shouldRender,
                            boolean renderThroughWalls, Vec3d... bounds) {
            this.key = Objects.requireNonNull(key, "key");
            this.type = Objects.requireNonNull(type, "type");
            this.color = Objects.requireNonNull(color, "color");
            this.shouldRender = shouldRender;
            this.renderThroughWalls = renderThroughWalls;
            this.bounds = Objects.requireNonNull(bounds, "bounds");
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (obj instanceof RenderObject other) {
                return key.equals(other.key);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }
}
