package org.phantazm.core.particle;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.particle.Particle;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.particle.data.ParticleVariantData;

@Model("particle.wrapper")
@Cache
public class ParticleWrapper {
    private final Data data;
    private final ParticleVariantData particleVariantData;

    @FactoryMethod
    public ParticleWrapper(@NotNull Data data,
            @NotNull @Child("variant_data") ParticleVariantData particleVariantData) {
        this.data = data;
        this.particleVariantData = particleVariantData;
    }

    public @NotNull Data data() {
        return data;
    }

    public @NotNull ParticleVariantData variantData() {
        return particleVariantData;
    }

    @DataObject
    public record Data(@NotNull Particle particle,
                       @NotNull @ChildPath("variant_data") String variantData,
                       boolean distance,
                       float offsetX,
                       float offsetY,
                       float offsetZ,
                       float data,
                       int particleCount) {
    }
}
