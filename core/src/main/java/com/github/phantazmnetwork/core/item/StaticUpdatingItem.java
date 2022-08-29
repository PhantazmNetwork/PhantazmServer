package com.github.phantazmnetwork.core.item;

import com.github.phantazmnetwork.core.config.processor.ItemStackConfigProcessors;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.ProcessorMethod;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.updating_item.static")
public class StaticUpdatingItem implements UpdatingItem {
    private final Data data;

    @ProcessorMethod
    public static ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<ItemStack> ITEM_STACK_PROCESSOR = ItemStackConfigProcessors.snbt();

            @Override
            public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                ItemStack item = ITEM_STACK_PROCESSOR.dataFromElement(element.getElementOrThrow("item"));
                return new Data(item);
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data data) throws ConfigProcessException {
                return ConfigNode.of("item", ITEM_STACK_PROCESSOR.elementFromData(data.item));
            }
        };
    }

    @FactoryMethod
    public StaticUpdatingItem(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull ItemStack update(long time, @NotNull ItemStack current) {
        return data.item;
    }

    @Override
    public boolean hasUpdate(long time, @NotNull ItemStack current) {
        return false;
    }

    @Override
    public @NotNull ItemStack currentItem() {
        return data.item;
    }

    @DataObject
    public record Data(@NotNull ItemStack item) {

    }
}
