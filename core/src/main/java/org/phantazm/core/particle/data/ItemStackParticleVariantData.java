package org.phantazm.core.particle.data;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

@Model("particle.variant_data.item_stack")
@Cache
public class ItemStackParticleVariantData implements ParticleVariantData {
    private static final Key KEY = Particle.ITEM.key();
    private final Data data;

    @FactoryMethod
    public ItemStackParticleVariantData(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public boolean isValid(@NotNull Particle particle) {
        return particle.key().equals(KEY);
    }

    @Override
    public void write(@NotNull BinaryWriter binaryWriter) {
        binaryWriter.writeItemStack(data.stack);
    }

    @DataObject
    public record Data(@NotNull ItemStack stack) {
    }
}
