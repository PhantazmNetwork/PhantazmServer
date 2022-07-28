package com.github.phantazmnetwork.core.particle.data;

import com.github.phantazmnetwork.core.config.processor.VariantConfigProcessor;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Represents specific data for a {@link Particle} that cannot be represented with primitives.
 */
public interface ParticleData extends Keyed {

    /**
     * Creates a {@link ConfigProcessor} for {@link ParticleData}.
     *
     * @param stackProcessor A {@link ConfigProcessor} for {@link ItemStack}s
     * @return A {@link ConfigProcessor} for {@link ParticleData}
     */
    static @NotNull ConfigProcessor<ParticleData> processor(@NotNull ConfigProcessor<ItemStack> stackProcessor) {
        return new VariantConfigProcessor<>(
                Map.of(BlockParticleData.SERIAL_KEY, BlockParticleData.processor(), DustParticleData.SERIAL_KEY,
                        DustParticleData.processor(), ItemStackParticleData.SERIAL_KEY,
                        ItemStackParticleData.processor(stackProcessor), NoParticleData.SERIAL_KEY,
                        NoParticleData.processor(), TransitionDustParticleData.SERIAL_KEY,
                        TransitionDustParticleData.processor())::get);
    }

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
