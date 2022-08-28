package com.github.phantazmnetwork.core.particle.data;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Particle data for dust particles.
 */
public class DustParticleData implements ParticleData {

    /**
     * The serial {@link Key} for {@link DustParticleData}.
     */
    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "particle_data.dust");

    private static final Key KEY = Particle.DUST.key();
    private final float red;
    private final float green;
    private final float blue;
    private final float size;

    /**
     * Creates a new {@link DustParticleData}.
     *
     * @param color The color of the particle
     * @param size  The size of the particle
     */
    public DustParticleData(@NotNull RGBLike color, float size) {
        Objects.requireNonNull(color, "color");
        this.red = color.red() / 255.0F;
        this.green = color.green() / 255.0F;
        this.blue = color.blue() / 255.0F;
        this.size = size;
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link DustParticleData}.
     *
     * @return A {@link ConfigProcessor} for {@link DustParticleData}
     */
    public static @NotNull ConfigProcessor<DustParticleData> processor() {
        ConfigProcessor<RGBLike> rgbProcessor = ConfigProcessors.rgbLike();

        return new ConfigProcessor<>() {
            @Override
            public @NotNull DustParticleData dataFromElement(@NotNull ConfigElement element)
                    throws ConfigProcessException {
                RGBLike rgb = rgbProcessor.dataFromElement(element.getElementOrThrow("color"));
                float size = element.getNumberOrThrow("size").floatValue();
                if (size < 0.01F || size > 4.0F) {
                    throw new ConfigProcessException("Size must be between 0.01 and 4.0");
                }

                return new DustParticleData(rgb, size);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull DustParticleData dustParticleData)
                    throws ConfigProcessException {
                RGBLike color = TextColor.color(dustParticleData.red, dustParticleData.green, dustParticleData.blue);

                ConfigNode node = new LinkedConfigNode(2);
                node.put("color", rgbProcessor.elementFromData(color));
                node.putNumber("size", dustParticleData.size);

                return node;
            }
        };
    }

    @Override
    public boolean isValid(@NotNull Particle particle) {
        return particle.key().equals(KEY);
    }

    @Override
    public void write(@NotNull BinaryWriter binaryWriter) {
        binaryWriter.writeFloat(red);
        binaryWriter.writeFloat(green);
        binaryWriter.writeFloat(blue);
        binaryWriter.writeFloat(size);
    }

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }
}
