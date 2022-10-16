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

    @DataObject
    public record Data(@NotNull Vec3D offset, @NotNull ItemStack displayItem) {
    }
}
