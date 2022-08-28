package com.github.phantazmnetwork.zombies.game.map.shop.display;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.VectorConfigProcessors;
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

@Model("zombies.map.shop.display.static_item")
public class StaticItemDisplay extends ItemDisplayBase {
    @FactoryMethod
    public StaticItemDisplay(@NotNull Data data) {
        super(data.displayItem, data.offset);
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<Vec3D> VEC3D_PROCESSOR = VectorConfigProcessors.vec3D();
            private static final ConfigProcessor<ItemStack> ITEM_STACK_PROCESSOR = ItemStackConfigProcessors.snbt();

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement node) throws ConfigProcessException {
                Vec3D offset = VEC3D_PROCESSOR.dataFromElement(node.getElementOrThrow("offset"));
                ItemStack displayItem = ITEM_STACK_PROCESSOR.dataFromElement(node.getElementOrThrow("displayItem"));
                return new Data(offset, displayItem);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                return ConfigNode.of("offset", VEC3D_PROCESSOR.elementFromData(data.offset), "displayItem",
                        ITEM_STACK_PROCESSOR.elementFromData(data.displayItem));
            }
        };
    }

    @DataObject
    public record Data(@NotNull Vec3D offset, @NotNull ItemStack displayItem) {
    }
}
