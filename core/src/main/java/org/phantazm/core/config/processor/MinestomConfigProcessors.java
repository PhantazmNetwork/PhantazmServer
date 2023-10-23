package org.phantazm.core.config.processor;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

import java.io.StringReader;

/**
 * {@link ConfigProcessor}s for Minestom-specific objects that are serializable.
 */
public class MinestomConfigProcessors {
    private static final ConfigProcessor<NBT> NBT = new ConfigProcessor<>() {
        @Override
        public @NotNull NBT dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            String nbtString = ConfigProcessor.STRING.dataFromElement(element);
            NBT nbt;
            try {
                nbt = new SNBTParser(new StringReader(nbtString)).parse();
            } catch (NBTException e) {
                throw new ConfigProcessException(e);
            }

            return nbt;
        }

        @Override
        public @NotNull ConfigElement elementFromData(@NotNull NBT compound) {
            return ConfigPrimitive.of(compound.toSNBT());
        }
    };

    private MinestomConfigProcessors() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull ConfigProcessor<NBT> nbt() {
        return NBT;
    }
}
