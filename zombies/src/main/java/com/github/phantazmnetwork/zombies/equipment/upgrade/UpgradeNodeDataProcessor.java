package com.github.phantazmnetwork.zombies.equipment.upgrade;

import com.github.phantazmnetwork.commons.ConfigProcessors;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class UpgradeNodeDataProcessor<TData extends UpgradeNodeData> implements ConfigProcessor<TData> {
    private static final ConfigProcessor<Key> KEY_PROCESSOR = ConfigProcessors.key();
    private static final ConfigProcessor<Set<Key>> KEY_SET_PROCESSOR = KEY_PROCESSOR.setProcessor();

    @Override
    public TData dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        Key levelKey = KEY_PROCESSOR.dataFromElement(element.getElementOrThrow("levelKey"));
        Set<Key> upgrades = KEY_SET_PROCESSOR.dataFromElement(element.getElementOrThrow("upgrades"));
        return upgradeNodeDataFromNode(element.asNode(), levelKey, upgrades);
    }

    @Override
    public @NotNull ConfigElement elementFromData(TData data) throws ConfigProcessException {
        ConfigNode remainder = nodeFromUpgradeNodeData(data);

        ConfigNode node = new LinkedConfigNode(remainder.size() + 2);
        node.put("levelKey", KEY_PROCESSOR.elementFromData(data.levelKey()));
        node.put("upgrades", KEY_SET_PROCESSOR.elementFromData(data.upgrades()));
        node.putAll(remainder);

        return node;
    }

    public abstract @NotNull TData upgradeNodeDataFromNode(@NotNull ConfigNode node, @NotNull Key levelKey,
            @NotNull Set<Key> upgrades);

    public abstract @NotNull ConfigNode nodeFromUpgradeNodeData(TData data);
}
