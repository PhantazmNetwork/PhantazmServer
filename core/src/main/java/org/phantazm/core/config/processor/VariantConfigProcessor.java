package org.phantazm.core.config.processor;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ConfigProcessors;

import java.util.Objects;
import java.util.function.Function;

/**
 * A {@link ConfigProcessor} which processes {@link TValue}s.
 * This can be used to process inheritance and identifies variants based on {@link Keyed#key()}.
 *
 * @param <TValue> The type of {@link TValue} to process
 */
public class VariantConfigProcessor<TValue extends Keyed> implements ConfigProcessor<TValue> {

    private static final ConfigProcessor<Key> KEY_PROCESSOR = ConfigProcessors.key();

    private final Function<Key, ConfigProcessor<? extends TValue>> subProcessors;

    /**
     * Creates a new {@link VariantConfigProcessor}.
     *
     * @param subprocessorProvider A {@link Function} that provides {@link ConfigProcessor} based on a {@link Key},
     *                             or null if no such {@link ConfigProcessor} is available
     */
    public VariantConfigProcessor(@NotNull Function<Key, ConfigProcessor<? extends TValue>> subprocessorProvider) {
        this.subProcessors = Objects.requireNonNull(subprocessorProvider, "subProcessors");
    }

    @Override
    public @NotNull TValue dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        ConfigElement serialKeyElement = element.getElementOrThrow("serialKey");
        Key key = KEY_PROCESSOR.dataFromElement(serialKeyElement);
        ConfigProcessor<? extends TValue> processor = subProcessors.apply(key);
        if (processor == null) {
            throw new ConfigProcessException("No subprocessor for key " + key);
        }

        ConfigElement serialData = element.getElement("serialData");
        if (serialData == null || serialData.isNull()) {
            return processor.dataFromElement(element);
        }
        else if (serialData.isNode()) {
            element.asNode().putAll(serialData.asNode());
            return processor.dataFromElement(element);
        }
        else {
            return processor.dataFromElement(serialData);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull ConfigElement elementFromData(@NotNull TValue data) throws ConfigProcessException {
        ConfigProcessor<TValue> processor = (ConfigProcessor<TValue>)subProcessors.apply(data.key());
        if (processor == null) {
            throw new ConfigProcessException("No subprocessor for key " + data.key());
        }

        ConfigElement element = processor.elementFromData(data);
        if (element.isNode()) {
            element.asNode().put("serialKey", KEY_PROCESSOR.elementFromData(data.key()));
            return element;
        }
        else {
            ConfigNode node = new LinkedConfigNode(2);
            node.put("serialKey", KEY_PROCESSOR.elementFromData(data.key()));
            node.put("serialData", element);

            return node;
        }
    }
}
