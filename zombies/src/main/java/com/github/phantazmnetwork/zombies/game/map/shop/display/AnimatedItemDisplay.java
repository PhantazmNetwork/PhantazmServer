package com.github.phantazmnetwork.zombies.game.map.shop.display;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.core.item.ItemAnimationFrame;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Model("zombies.map.shop.display.animated_item")
@Cache(false)
public class AnimatedItemDisplay extends ItemDisplayBase {
    private final Data data;

    private int loopCount;
    private int frameIndex;
    private long timeAtLastFrame;
    private ItemAnimationFrame currentFrame;

    @FactoryMethod
    public AnimatedItemDisplay(@NotNull Data data) {
        super(data.frames.isEmpty() ? ItemStack.AIR : data.frames.get(0).itemStack(), data.offset);
        this.data = data;
    }

    @Override
    public void tick(long time) {
        if (data.frames.isEmpty() || (data.loops >= 0 && loopCount > data.loops)) {
            return;
        }

        if (currentFrame == null) {
            frameIndex = 0;
            currentFrame = data.frames.get(0);
            timeAtLastFrame = time;
            return;
        }

        if (timeAtLastFrame - time > (long)currentFrame.delayTicks() * MinecraftServer.TICK_MS) {
            if (++frameIndex >= data.frames.size()) {
                frameIndex = 0;
                loopCount++;
            }

            currentFrame = data.frames.get(frameIndex);
            itemEntity.setItemStack(currentFrame.itemStack());
            timeAtLastFrame = time;
        }
    }

    @Override
    public void destroy(@NotNull Shop shop) {
        super.destroy(shop);
        loopCount = 0;
        frameIndex = 0;
        timeAtLastFrame = 0;
        currentFrame = null;
    }

    @DataObject
    public record Data(@NotNull Vec3D offset, @NotNull List<ItemAnimationFrame> frames, int loops) {
    }
}
