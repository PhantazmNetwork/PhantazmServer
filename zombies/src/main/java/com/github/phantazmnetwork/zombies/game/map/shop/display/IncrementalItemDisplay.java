package com.github.phantazmnetwork.zombies.game.map.shop.display;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.VectorConfigProcessors;
import com.github.phantazmnetwork.core.VecUtils;
import com.github.phantazmnetwork.core.config.processor.ItemStackConfigProcessors;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.display.incremental_item")
@Cache(false)
public class IncrementalItemDisplay extends ItemDisplayBase {
    private final Data data;

    private int displayIndex;

    @FactoryMethod
    public IncrementalItemDisplay(@NotNull Data data) {
        super(data.displayItems.isEmpty() ? ItemStack.AIR : data.displayItems.get(0), data.offset);
        this.data = Objects.requireNonNull(data, "data");
        this.displayIndex = 0;
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<Vec3D> VEC3D_PROCESSOR = VectorConfigProcessors.vec3D();
            private static final ConfigProcessor<List<ItemStack>> ITEM_STACK_LIST_PROCESSOR =
                    ItemStackConfigProcessors.snbt().listProcessor();

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement node) throws ConfigProcessException {
                Vec3D offset = VEC3D_PROCESSOR.dataFromElement(node.getElementOrThrow("offset"));
                List<ItemStack> displayItems =
                        ITEM_STACK_LIST_PROCESSOR.dataFromElement(node.getElementOrThrow("displayItems"));
                return new Data(offset, displayItems);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                return ConfigNode.of("offset", VEC3D_PROCESSOR.elementFromData(data.offset), "displayItems",
                        ITEM_STACK_LIST_PROCESSOR.elementFromData(data.displayItems));
            }
        };
    }

    @Override
    public void update(@NotNull Shop shop, @NotNull PlayerInteraction interaction, boolean interacted) {
        if (interacted) {
            int nextIndex = displayIndex + 1;
            if (nextIndex < data.displayItems.size()) {
                displayIndex = nextIndex;
                itemEntity.setItemStack(data.displayItems.get(nextIndex));
            }
        }
    }

    @DataObject
    public record Data(@NotNull Vec3D offset, @NotNull List<ItemStack> displayItems) {
    }
}
