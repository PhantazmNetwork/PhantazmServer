package com.github.phantazmnetwork.api.config;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.collection.LinkedConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class ItemStackConfigProcessor implements ConfigProcessor<ItemStack> {
    @Override
    public @NotNull ItemStack dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
        Material material = Material.fromNamespaceId(element.getStringOrThrow("material"));
        if (material == null) {
            throw new ConfigProcessException("unknown material");
        }
        return ItemStack.of(material);
    }

    @Override
    public @NotNull ConfigElement elementFromData(@NotNull ItemStack itemStack) {
        ConfigNode element = new LinkedConfigNode();
        element.put("material", new ConfigPrimitive(itemStack.material().namespace().namespace()));

        return element;
    }
}
