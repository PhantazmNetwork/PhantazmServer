package com.github.phantazmnetwork.zombies.game.map.shop.display;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
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
