package com.github.phantazmnetwork.api.particle;

import com.github.phantazmnetwork.api.config.processor.MinestomConfigProcessors;
import com.github.phantazmnetwork.api.particle.data.ParticleData;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A wrapper around the necessary information to create a {@link ParticlePacket}.
 *
 * @param particle      The type of particle to use
 * @param data          The specific data for the particle
 * @param distance      Whether the particle should be "long-distance"
 * @param offsetX       A delta x offset for the particle's velocity
 * @param offsetY       A delta y offset for the particle's velocity
 * @param offsetZ       A delta z offset for the particle's velocity
 * @param particleData  Float data for the particle
 * @param particleCount The number of particles to spawn
 */
public record ParticleWrapper(@NotNull Particle particle,
                              @NotNull ParticleData data,
                              boolean distance,
                              float offsetX,
                              float offsetY,
                              float offsetZ,
                              float particleData,
                              int particleCount) {

    /**
     * Creates a {@link ParticleWrapper}.
     *
     * @param particle      The type of particle to use
     * @param data          The specific data for the particle
     * @param distance      Whether the particle should be "long-distance"
     * @param offsetX       A delta x offset for the particle's velocity
     * @param offsetY       A delta y offset for the particle's velocity
     * @param offsetZ       A delta z offset for the particle's velocity
     * @param particleData  Float data for the particle
     * @param particleCount The number of particles to spawn
     */
    public ParticleWrapper {
        Objects.requireNonNull(particle, "particle");
        Objects.requireNonNull(data, "data");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link ParticleWrapper}s.
     *
     * @param particleDataProcessor A {@link ConfigProcessor} for {@link ParticleData}
     * @return A {@link ConfigProcessor} for {@link ParticleWrapper}s
     */
    public static @NotNull ConfigProcessor<ParticleWrapper> processor(
            @NotNull ConfigProcessor<ParticleData> particleDataProcessor) {
        ConfigProcessor<Particle> particleProcessor = MinestomConfigProcessors.particle();

        return new ConfigProcessor<>() {
            @Override
            public @NotNull ParticleWrapper dataFromElement(@NotNull ConfigElement element)
                    throws ConfigProcessException {
                Particle particle = particleProcessor.dataFromElement(element);
                ParticleData data = particleDataProcessor.dataFromElement(element.getElementOrThrow("data"));
                if (!data.isValid(particle)) {
                    throw new ConfigProcessException(
                            "Invalid particle data type " + data.key() + " for particle " + particle.key());
                }
                boolean distance = element.getBooleanOrThrow("distance");
                float offsetX = element.getNumberOrThrow("offsetX").floatValue();
                float offsetY = element.getNumberOrThrow("offsetY").floatValue();
                float offsetZ = element.getNumberOrThrow("offsetZ").floatValue();
                float particleData = element.getNumberOrThrow("particleData").floatValue();
                int particleCount = element.getNumberOrThrow("particleCount").intValue();

                return new ParticleWrapper(particle, data, distance, offsetX, offsetY, offsetZ, particleData,
                                           particleCount
                );
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull ParticleWrapper particleWrapper)
                    throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(8);
                node.put("particle", particleProcessor.elementFromData(particleWrapper.particle()));
                node.put("data", particleDataProcessor.elementFromData(particleWrapper.data()));
                node.putBoolean("distance", particleWrapper.distance());
                node.putNumber("offsetX", particleWrapper.offsetX());
                node.putNumber("offsetY", particleWrapper.offsetY());
                node.putNumber("offsetZ", particleWrapper.offsetZ());
                node.putNumber("particleData", particleWrapper.particleData());
                node.putNumber("particleCount", particleWrapper.particleCount());

                return node;
            }
        };
    }

}
