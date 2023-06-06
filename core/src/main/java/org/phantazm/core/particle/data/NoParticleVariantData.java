package org.phantazm.core.particle.data;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

@Model("particle.variant_data.none")
@Cache
public class NoParticleVariantData implements ParticleVariantData {
    @FactoryMethod
    public NoParticleVariantData() {
    }

    @Override
    public boolean isValid(@NotNull Particle particle) {
        return true;
    }

    @Override
    public void write(@NotNull BinaryWriter binaryWriter) {

    }
}
