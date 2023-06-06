package org.phantazm.core.particle.data;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Model("particle.variant_data.block")
@Cache
public final class BlockParticleVariantData implements ParticleVariantData {
    private static final Set<Key> VALID_KEYS =
            Set.of(Particle.BLOCK.key(), Particle.BLOCK_MARKER.key(), Particle.FALLING_DUST.key());

    private final Data data;

    @FactoryMethod
    public BlockParticleVariantData(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public boolean isValid(@NotNull Particle particle) {
        return VALID_KEYS.contains(particle.key());
    }

    @Override
    public void write(@NotNull BinaryWriter binaryWriter) {
        binaryWriter.writeVarInt(data.block.stateId());
    }

    @DataObject
    public record Data(@NotNull Block block) {
    }
}
