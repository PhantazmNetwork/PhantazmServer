package org.phantazm.core.particle.data;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

@Model("particle.variant_data.dust")
@Cache
public class DustParticleVariantData implements ParticleVariantData {
    private static final Key KEY = Particle.DUST.key();
    private final Data data;

    @FactoryMethod
    public DustParticleVariantData(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public boolean isValid(@NotNull Particle particle) {
        return particle.key().equals(KEY);
    }

    @Override
    public void write(@NotNull BinaryWriter binaryWriter) {
        binaryWriter.writeFloat(data.color.red());
        binaryWriter.writeFloat(data.color.green());
        binaryWriter.writeFloat(data.color.blue());
        binaryWriter.writeFloat(data.size);
    }

    @DataObject
    public record Data(@NotNull RGBLike color, float size) {
    }
}
