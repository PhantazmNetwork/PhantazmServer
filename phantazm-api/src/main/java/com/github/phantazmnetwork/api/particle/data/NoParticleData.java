package com.github.phantazmnetwork.api.particle.data;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

public class NoParticleData implements ParticleData {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "particle_data.none");

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