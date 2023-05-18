package org.phantazm.core.particle.data;

import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;

/**
 * Particle data without any extra data.
 */
public class NoParticleData implements ParticleData {

    /**
     * The serial {@link Key} for {@link NoParticleData}.
     */
    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "particle_data.none");

    /**
     * Gets a {@link ConfigProcessor} for {@link NoParticleData}.
     *
     * @return A {@link ConfigProcessor} for {@link NoParticleData}
     */
    public static @NotNull ConfigProcessor<NoParticleData> processor() {
        return ConfigProcessor.emptyProcessor(NoParticleData::new);
    }

    @Override
    public boolean isValid(@NotNull Particle particle) {
        return true;
    }

    @Override
    public void write(@NotNull BinaryWriter binaryWriter) {

    }

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }
}
