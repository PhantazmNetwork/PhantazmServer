package com.github.phantazmnetwork.zombies.map;

import com.github.steanky.vector.Vec3D;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Represents a hologram (typically, text displayed using an invisible armorstand with interaction and gravity
 * disabled).
 */
public record HologramInfo(@NotNull List<Component> text, @NotNull Vec3D position) {
    /**
     * Creates a new instance of this record.
     *
     * @param text     the text to show
     * @param position the position of the hologram, which may be absolute (world coordinates) or relative to another
     *                 vector
     */
    public HologramInfo {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(position, "position");
    }
}
