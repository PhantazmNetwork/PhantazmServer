package com.github.phantazmnetwork.core.config.processor;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

/**
 * {@link ConfigProcessor}s that process {@link ItemStack}s.
 */
public class ItemStackConfigProcessors {

    private static final ConfigProcessor<ItemStack> SNBT = new ConfigProcessor<>() {

        private static final ConfigProcessor<NBT> NBT_COMPOUND_PROCESSOR = MinestomConfigProcessors.nbt();

        @SuppressWarnings("UnstableApiUsage")
        @Override
        public ItemStack dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            NBT nbt = NBT_COMPOUND_PROCESSOR.dataFromElement(element);
            if (!(nbt instanceof NBTCompound compound)) {
                throw new ConfigProcessException("NBT is not a compound");
            }

            return ItemStack.fromItemNBT(compound);
        }

        @SuppressWarnings("UnstableApiUsage")
        @Override
        public @NotNull ConfigElement elementFromData(@NotNull ItemStack itemStack) throws ConfigProcessException {
            return NBT_COMPOUND_PROCESSOR.elementFromData(itemStack.toItemNBT());
        }
    };

    private ItemStackConfigProcessors() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new {@link ConfigProcessor} for {@link ItemStack}s based on String NBT format.
     *
     * @return A new {@link ConfigProcessor} for {@link ItemStack}s based on String NBT format
     */
    public static @NotNull ConfigProcessor<ItemStack> snbt() {
        return SNBT;
    }

}
