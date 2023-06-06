package org.phantazm.core.particle.data;

import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

/**
 * Represents specific data for a {@link Particle} that cannot be represented with primitives.
 */
public interface ParticleVariantData {
    /**
     * Checks if a {@link Particle} is applicable to this data.
     *
     * @param particle The particle to check
     * @return True if the particle is applicable to this data
     */
    boolean isValid(@NotNull Particle particle);

    /**
     * Writes the particle data to a {@link BinaryWriter}.
     *
     * @param binaryWriter The {@link BinaryWriter} to write to
     */
    void write(@NotNull BinaryWriter binaryWriter);
}
