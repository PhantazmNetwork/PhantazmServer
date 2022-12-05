package com.github.phantazmnetwork.core.hologram;

import com.github.steanky.vector.Vec3D;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * A subclass of {@link InstanceHologram} which additionally may be set up to only render for players who satisfy a
 * certain predicate.
 */
public class ViewableHologram extends InstanceHologram {
    private final Predicate<? super Player> canRender;

    /**
     * Creates a new instance of this class, whose holograms will be rendered at the given location, using the given
     * alignment, and only to players who satisfy the given predicate.
     *
     * @param location  the location of the instance
     * @param gap       the distance between separate hologram messages
     * @param alignment the alignment method
     * @param canRender the predicate used to determine if this hologram should be visible
     */
    public ViewableHologram(@NotNull Point location, double gap, @NotNull Alignment alignment,
            @NotNull Predicate<? super Player> canRender) {
        super(location, gap, alignment);
        this.canRender = Objects.requireNonNull(canRender, "canRender");
    }

    /**
     * Creates a new instance of this class, whose holograms will be rendered at the given location, using the default
     * alignment {@link Alignment#UPPER}, and only to players who satisfy the given predicate.
     *
     * @param location  the location to render holograms
     * @param gap       the distance between separate hologram messages
     * @param canRender the predicate used to determine if this hologram should be visible
     */
    public ViewableHologram(@NotNull Point location, double gap, @NotNull Predicate<? super Player> canRender) {
        this(location, gap, Alignment.UPPER, canRender);
    }

    @Override
    protected @NotNull Entity constructEntity(@NotNull Component display) {
        Entity entity = super.constructEntity(display);
        entity.updateViewableRule(canRender);
        return entity;
    }
}
