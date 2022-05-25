package com.github.phantazmnetwork.api;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.minestom.server.adventure.MinestomAdventure;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;

public final class MinestomConfigProcessors {
    private static final ConfigProcessor<ItemStack> itemStack = new ConfigProcessor<>() {
        @Override
        public @NotNull ItemStack dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            try {
                return ItemStack.fromItemNBT((NBTCompound) MinestomAdventure.NBT_CODEC.decode(element.asString()));
            } catch (NBTException e) {
                throw new ConfigProcessException(e);
            }
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull ItemStack itemStack) {
            return new ConfigPrimitive(itemStack.toItemNBT().toSNBT());
        }
    };

    private static final ConfigProcessor<Key> key = new ConfigProcessor<>() {
        @Override
        public Key dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            if(!element.isString()) {
                throw new ConfigProcessException("Element must be a string");
            }

            try {
                //noinspection PatternValidation
                return Key.key(element.asString());
            }
            catch (InvalidKeyException keyException) {
                throw new ConfigProcessException(keyException);
            }
        }

        @Override
        public @NotNull ConfigElement elementFromData(Key key) {
            return new ConfigPrimitive(key.asString());
        }
    };

    private MinestomConfigProcessors() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull ConfigProcessor<ItemStack> itemStack() {
        return itemStack;
    }

    public static @NotNull ConfigProcessor<Key> key() {
        return key;
    }
}
