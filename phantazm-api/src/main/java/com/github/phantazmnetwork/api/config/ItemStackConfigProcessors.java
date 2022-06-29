package com.github.phantazmnetwork.api.config;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

import java.io.StringReader;

/**
 * {@link ConfigProcessor}s that process {@link ItemStack}s.
 */
public class ItemStackConfigProcessors {

    private ItemStackConfigProcessors() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new {@link ConfigProcessor} for {@link ItemStack}s based on String NBT format.
     * @return A new {@link ConfigProcessor} for {@link ItemStack}s based on String NBT format
     */
    public static @NotNull ConfigProcessor<ItemStack> snbt() {
        return new ConfigProcessor<>() {
            @SuppressWarnings("UnstableApiUsage")
            @Override
            public ItemStack dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                String nbtString = ConfigProcessor.STRING.dataFromElement(element);
                NBTCompound itemCompound;
                try {
                    itemCompound = (NBTCompound) new SNBTParser(new StringReader(nbtString)).parse();
                } catch (NBTException e) {
                    throw new ConfigProcessException(e);
                }

                return ItemStack.fromItemNBT(itemCompound);
            }

            @SuppressWarnings("UnstableApiUsage")
            @Override
            public @NotNull ConfigElement elementFromData(@NotNull ItemStack itemStack) throws ConfigProcessException {
                return new ConfigPrimitive(itemStack.toItemNBT().toSNBT());
            }
        };
    }

}
