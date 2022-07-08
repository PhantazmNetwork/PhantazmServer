package com.github.phantazmnetwork.api.particle.data;

import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Particle data for sculk vibrations.
 */
public class VibrationParticleData implements ParticleData {

    /**
     * The serial {@link Key} for {@link VibrationParticleData}.
     */
    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "particle_data.vibration");

    private static final Key KEY = Particle.VIBRATION.key();

    /**
     * A position source for sculk vibrations.
     */
    public sealed interface PositionSource extends Keyed permits BlockPositionSource, EntityPositionSource {

        /**
         * Writes the {@link PositionSource} to the given {@link BinaryWriter}.
         * @param binaryWriter The {@link BinaryWriter} to write to
         */
        void write(@NotNull BinaryWriter binaryWriter);

    }

    /**
     * A block position source for sculk vibrations.
     * @param blockPosition The block position source
     */
    public record BlockPositionSource(@NotNull Point blockPosition) implements PositionSource {

        private static final Key KEY = Key.key("block");

        /**
         * Creates a new {@link BlockPositionSource}.
         * @param blockPosition The block position source
         */
        public BlockPositionSource {
            Objects.requireNonNull(blockPosition, "blockPosition");
        }

        @Override
        public void write(@NotNull BinaryWriter binaryWriter) {
            binaryWriter.writeBlockPosition(blockPosition);
        }

        @Override
        public @NotNull Key key() {
            return KEY;
        }
    }

    /**
     * An entity position source for sculk vibrations.
     */
    public static final class EntityPositionSource implements PositionSource {

        private static final Key KEY = Key.key("entity");

        private final int entityId;

        private final float eyeHeight;

        /**
         * Creates a new {@link EntityPositionSource}.
         * @param entity The entity source
         */
        public EntityPositionSource(@NotNull Entity entity) {
            Objects.requireNonNull(entity, "entity");
            this.entityId = entity.getEntityId();
            this.eyeHeight = (float) entity.getEyeHeight();
        }

        @Override
        public void write(@NotNull BinaryWriter binaryWriter) {
            binaryWriter.writeVarInt(entityId);
            binaryWriter.writeFloat(eyeHeight);
        }

        @Override
        public @NotNull Key key() {
            return KEY;
        }
    }

    private final PositionSource positionSource;

    private final int ticks;

    /**
     * Creates a new {@link VibrationParticleData}.
     * @param positionSource The position source of the vibration
     * @param ticks The arrival ticks for the vibration
     */
    public VibrationParticleData(@NotNull PositionSource positionSource, int ticks) {
        this.positionSource = Objects.requireNonNull(positionSource, "positionSource");
        this.ticks = ticks;
    }

    @Override
    public boolean isValid(@NotNull Particle particle) {
        return particle.key().equals(KEY);
    }

    @Override
    public void write(@NotNull BinaryWriter binaryWriter) {
        binaryWriter.writeSizedString(positionSource.key().asString());
        positionSource.write(binaryWriter);
        binaryWriter.writeVarInt(ticks);
    }

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }

}
