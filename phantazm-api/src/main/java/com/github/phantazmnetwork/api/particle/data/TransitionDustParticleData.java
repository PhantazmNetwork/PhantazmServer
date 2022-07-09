package com.github.phantazmnetwork.api.particle.data;

import com.github.phantazmnetwork.commons.AdventureConfigProcessors;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

/**
 * Particle data for a dust particle that transitions between two colors.
 */
public class TransitionDustParticleData implements ParticleData {

    /**
     * The serial {@link Key} for {@link TransitionDustParticleData}.
     */
    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "particle_data.dust_color_transition");

    private static final Key KEY = Particle.DUST_COLOR_TRANSITION.key();

    /**
     * Creates a {@link ConfigProcessor} for {@link TransitionDustParticleData}.
     * @return A {@link ConfigProcessor} for {@link TransitionDustParticleData}
     */
    public static @NotNull ConfigProcessor<TransitionDustParticleData> processor() {
        ConfigProcessor<RGBLike> rgbProcessor = AdventureConfigProcessors.rgbLike();

        return new ConfigProcessor<>() {
            @Override
            public @NotNull TransitionDustParticleData dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                RGBLike from = rgbProcessor.dataFromElement(element.getElementOrThrow("from"));
                RGBLike to = rgbProcessor.dataFromElement(element.getElementOrThrow("to"));
                float size = element.getNumberOrThrow("size").floatValue();
                if (size < 0.01F || size > 4.0F) {
                    throw new ConfigProcessException("size must be between 0.01 and 4.0");
                }

                return new TransitionDustParticleData(from, to, size);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull TransitionDustParticleData transitionDustParticleData) throws ConfigProcessException {
                TextColor from = TextColor.color(transitionDustParticleData.fromRed,
                        transitionDustParticleData.fromGreen, transitionDustParticleData.fromBlue);
                TextColor to = TextColor.color(transitionDustParticleData.toRed,
                        transitionDustParticleData.toGreen, transitionDustParticleData.toBlue);

                LinkedConfigNode node = new LinkedConfigNode(3);
                node.put("from", rgbProcessor.elementFromData(from));
                node.put("to", rgbProcessor.elementFromData(to));
                node.putNumber("size", transitionDustParticleData.size);

                return node;
            }
        };
    }

    private final float fromRed;

    private final float fromGreen;

    private final float fromBlue;

    private final float toRed;

    private final float toGreen;

    private final float toBlue;

    private final float size;

    /**
     * Creates a new {@link TransitionDustParticleData}.
     * @param from The initial color of the particle
     * @param to The final color of the particle
     * @param size The size of the particle
     */
    public TransitionDustParticleData(@NotNull RGBLike from, @NotNull RGBLike to, float size) {
        this.fromRed = from.red() / 255.0F;
        this.fromGreen = from.green() / 255.0F;
        this.fromBlue = from.blue() / 255.0F;
        this.toRed = to.red() / 255.0F;
        this.toGreen = to.green() / 255.0F;
        this.toBlue = to.blue() / 255.0F;
        this.size = size;
    }

    @Override
    public boolean isValid(@NotNull Particle particle) {
        return particle.key().equals(KEY);
    }

    @Override
    public void write(@NotNull BinaryWriter binaryWriter) {
        binaryWriter.writeFloat(fromRed);
        binaryWriter.writeFloat(fromGreen);
        binaryWriter.writeFloat(fromBlue);
        binaryWriter.writeFloat(size);
        binaryWriter.writeFloat(toRed);
        binaryWriter.writeFloat(toGreen);
        binaryWriter.writeFloat(toBlue);
    }

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }
}
