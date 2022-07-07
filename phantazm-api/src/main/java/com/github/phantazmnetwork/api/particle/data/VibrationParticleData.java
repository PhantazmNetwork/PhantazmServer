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

public class VibrationParticleData implements ParticleData {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "particle_data.vibration");

    private static final Key KEY = Particle.VIBRATION.key();

    public sealed interface PositionSource extends Keyed permits BlockPositionSource, EntityPositionSource {

        void write(@NotNull BinaryWriter binaryWriter);

    }

    public record BlockPositionSource(@NotNull Point blockPosition) implements PositionSource {

        private static final Key KEY = Key.key("block");

        @Override
        public void write(@NotNull BinaryWriter binaryWriter) {
            binaryWriter.writeBlockPosition(blockPosition);
        }

        @Override
        public @NotNull Key key() {
            return KEY;
        }
    }

    public static final class EntityPositionSource implements PositionSource {

        private static final Key KEY = Key.key("entity");

        private final int entityId;

        private final float eyeHeight;

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
