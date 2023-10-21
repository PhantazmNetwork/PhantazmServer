package org.phantazm.zombies.map.shop.display;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.vector.Vec3D;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.VecUtils;

@Model("zombies.map.shop.display.static_item")
public class StaticItemDisplay extends ItemDisplayBase {
    @FactoryMethod
    public StaticItemDisplay(@NotNull Data data) {
        super(data.displayItem, VecUtils.toPoint(data.offset));
    }

    @DataObject
    public record Data(@NotNull Vec3D offset,
        @NotNull ItemStack displayItem) {
    }
}
