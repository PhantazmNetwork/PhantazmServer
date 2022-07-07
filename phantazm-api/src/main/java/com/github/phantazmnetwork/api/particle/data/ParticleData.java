package com.github.phantazmnetwork.api.particle.data;

import com.github.phantazmnetwork.api.config.processor.VariantConfigProcessor;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface ParticleData extends Keyed {

    static @NotNull ConfigProcessor<ParticleData> processor(@NotNull ConfigProcessor<ItemStack> stackProcessor) {
        return new VariantConfigProcessor<>(Map.of(
                BlockParticleData.SERIAL_KEY, BlockParticleData.processor(),
                DustParticleData.SERIAL_KEY, DustParticleData.processor(),
                ItemStackParticleData.SERIAL_KEY, ItemStackParticleData.processor(stackProcessor),
                NoParticleData.SERIAL_KEY, NoParticleData.processor(),
                TransitionDustParticleData.SERIAL_KEY, TransitionDustParticleData.processor()
        )::get);
    }

    boolean isValid(@NotNull Particle particle);

    void write(@NotNull BinaryWriter binaryWriter);

}
