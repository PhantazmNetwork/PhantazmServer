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

@Model("particle.variant_data.transition_dust")
@Cache
public class TransitionDustParticleVariantData implements ParticleVariantData {
    private static final Key KEY = Particle.DUST_COLOR_TRANSITION.key();

    private final Data data;

    @FactoryMethod
    public TransitionDustParticleVariantData(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public boolean isValid(@NotNull Particle particle) {
        return particle.key().equals(KEY);
    }

    @Override
    public void write(@NotNull BinaryWriter binaryWriter) {
        binaryWriter.writeFloat(data.fromColor.red());
        binaryWriter.writeFloat(data.fromColor.green());
        binaryWriter.writeFloat(data.fromColor.blue());
        binaryWriter.writeFloat(data.size);
        binaryWriter.writeFloat(data.toColor.red());
        binaryWriter.writeFloat(data.toColor.green());
        binaryWriter.writeFloat(data.toColor.blue());
    }

    @DataObject
    public record Data(@NotNull RGBLike fromColor,
        @NotNull RGBLike toColor,
        float size) {
    }
}
