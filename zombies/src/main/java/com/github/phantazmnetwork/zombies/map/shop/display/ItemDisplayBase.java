package com.github.phantazmnetwork.zombies.map.shop.display;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.core.VecUtils;
import com.github.phantazmnetwork.zombies.map.shop.Shop;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class ItemDisplayBase implements ShopDisplay {
    protected final ItemStack itemStack;
    protected final Vec3D offset;

    protected ItemEntity itemEntity;

    public ItemDisplayBase(@NotNull ItemStack itemStack, @NotNull Vec3D offset) {
        this.itemStack = Objects.requireNonNull(itemStack, "itemStack");
        this.offset = Objects.requireNonNull(offset, "offset");
    }

    @Override
    public void initialize(@NotNull Shop shop) {
        removeItem();

        itemEntity = new ItemEntity(itemStack);
        itemEntity.setMergeable(false);
        itemEntity.setPickable(false);
        itemEntity.setNoGravity(true);
        itemEntity.setInstance(shop.getInstance(), VecUtils.toPoint(shop.computeAbsolutePosition(offset)));
    }

    @Override
    public void destroy(@NotNull Shop shop) {
        removeItem();
    }

    private void removeItem() {
        if (itemEntity != null) {
            itemEntity.remove();
            itemEntity = null;
        }
    }
}
