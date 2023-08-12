package org.phantazm.zombies.map.shop.display;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.vector.Vec3D;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.VecUtils;
import org.phantazm.core.item.ItemAnimationFrame;
import org.phantazm.zombies.map.shop.Shop;

import java.util.List;

@Model("zombies.map.shop.display.animated_item")
@Cache(false)
public class AnimatedItemDisplay extends ItemDisplayBase {
    private final Data data;

    private int frameIndex;
    private long lastFrameTicks;
    private ItemAnimationFrame currentFrame;

    @FactoryMethod
    public AnimatedItemDisplay(@NotNull Data data) {
        super(data.frames.isEmpty() ? ItemStack.AIR : data.frames.get(0).itemStack(), VecUtils.toPoint(data.offset));
        this.data = data;
    }

    @Override
    public void tick(long time) {
        if (data.frames.isEmpty()) {
            return;
        }

        if (currentFrame == null) {
            frameIndex = 0;
            currentFrame = data.frames.get(0);
            lastFrameTicks = 0;
            return;
        }

        ++lastFrameTicks;
        if (lastFrameTicks > currentFrame.delayTicks()) {
            if (++frameIndex >= data.frames.size()) {
                frameIndex = 0;
            }

            currentFrame = data.frames.get(frameIndex);
            itemEntity.setItemStack(currentFrame.itemStack());
            lastFrameTicks = 0;
        }
    }

    @Override
    public void destroy(@NotNull Shop shop) {
        super.destroy(shop);
        frameIndex = 0;
        lastFrameTicks = 0;
        currentFrame = null;
    }

    @DataObject
    public record Data(@NotNull Vec3D offset, @NotNull List<ItemAnimationFrame> frames) {
    }
}
