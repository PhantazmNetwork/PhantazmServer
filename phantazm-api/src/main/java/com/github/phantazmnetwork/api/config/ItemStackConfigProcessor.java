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

public class ItemStackConfigProcessor implements ConfigProcessor<ItemStack> {
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull ItemStack dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        if (!element.isString()) {
            throw new ConfigProcessException("itemstack is not string");
        }

        String nbtString = element.asString();
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
    public @NotNull ConfigElement elementFromData(@NotNull ItemStack itemStack) {
        return new ConfigPrimitive(itemStack.toItemNBT().toSNBT());
    }
}
