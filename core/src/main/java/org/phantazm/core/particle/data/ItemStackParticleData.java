package org.phantazm.core.particle.data;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import net.minestom.server.particle.Particle;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;

import java.util.Objects;

/**
 * Particle data that is related to {@link ItemStack}s.
 */
public class ItemStackParticleData implements ParticleData {

    /**
     * The serial {@link Key} for {@link ItemStackParticleData}.
     */
    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "particle_data.item");

    private static final Key KEY = Particle.ITEM.key();
    private final ItemStack stack;

    /**
     * Creates a new {@link ItemStackParticleData} with the given {@link ItemStack}.
     *
     * @param stack The {@link ItemStack} to use
     */
    public ItemStackParticleData(@NotNull ItemStack stack) {
        this.stack = Objects.requireNonNull(stack, "stack");
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link ItemStackParticleData}.
     *
     * @param stackProcessor A {@link ConfigProcessor} for {@link ItemStack}s
     * @return A {@link ConfigProcessor} for {@link ItemStackParticleData}
     */
    public static @NotNull ConfigProcessor<ItemStackParticleData> processor(
            @NotNull ConfigProcessor<ItemStack> stackProcessor) {
        return new ConfigProcessor<>() {
            @Override
            public @NotNull ItemStackParticleData dataFromElement(@NotNull ConfigElement element)
                    throws ConfigProcessException {
                ItemStack stack = stackProcessor.dataFromElement(element.getElementOrThrow("stack"));
                return new ItemStackParticleData(stack);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull ItemStackParticleData itemStackParticleData)
                    throws ConfigProcessException {
                ConfigNode node = new LinkedConfigNode(1);
                node.put("stack", stackProcessor.elementFromData(itemStackParticleData.stack));

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
        binaryWriter.writeItemStack(stack);
    }

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }
}
