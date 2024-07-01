package org.phantazm.core.particle.data;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

@Model("particle.variant_data.vibration")
@Cache
public class VibrationParticleVariantData implements ParticleVariantData {
    private static final Key KEY = Particle.VIBRATION.key();
    private final Data data;
    private final PositionSource positionSource;

    @FactoryMethod
    public VibrationParticleVariantData(@NotNull Data data,
        @NotNull @Child("positionSource") PositionSource positionSource) {
        this.data = data;
        this.positionSource = positionSource;
    }

    @Override
    public boolean isValid(@NotNull Particle particle) {
        return particle.key().equals(KEY);
    }

    @Override
    public void write(@NotNull BinaryWriter binaryWriter) {
        binaryWriter.writeSizedString(positionSource.key().toString());
        positionSource.write(binaryWriter);
        binaryWriter.writeVarInt(data.ticks);
    }

    @DataObject
    public record Data(int ticks) {
    }
}
