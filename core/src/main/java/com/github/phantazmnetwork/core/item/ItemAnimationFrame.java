package com.github.phantazmnetwork.core.item;

import com.github.phantazmnetwork.core.config.processor.ItemStackConfigProcessors;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ItemAnimationFrame(@NotNull ItemStack itemStack, int delayTicks) {
    private static final ConfigProcessor<ItemAnimationFrame> PROCESSOR = new ConfigProcessor<>() {
        private static final ConfigProcessor<ItemStack> ITEM_STACK_PROCESSOR = ItemStackConfigProcessors.snbt();

        @Override
        public ItemAnimationFrame dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
            ItemStack itemStack = ITEM_STACK_PROCESSOR.dataFromElement(element.getElementOrThrow("itemStack"));
            int delayTicks = element.getNumberOrThrow("delayTicks").intValue();
            return new ItemAnimationFrame(itemStack, delayTicks);
        }

        @Override
        public @NotNull ConfigElement elementFromData(ItemAnimationFrame itemAnimationFrame)
                throws ConfigProcessException {
            return ConfigNode.of("itemStack", ITEM_STACK_PROCESSOR.elementFromData(itemAnimationFrame.itemStack),
                    "delayTicks", itemAnimationFrame.delayTicks);
        }
    };

    public static @NotNull ConfigProcessor<ItemAnimationFrame> processor() {
        return PROCESSOR;
    }
}
