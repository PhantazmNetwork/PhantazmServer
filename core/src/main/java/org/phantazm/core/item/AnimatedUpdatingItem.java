package org.phantazm.core.item;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Model("item.updating.animated")
@Cache(false)
public class AnimatedUpdatingItem implements UpdatingItem {
    private final Data data;

    private long lastUpdateTime;
    private ItemAnimationFrame currentFrame;
    private int currentFrameIndex;

    @FactoryMethod
    public AnimatedUpdatingItem(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
        if (data.frames.isEmpty()) {
            throw new IllegalArgumentException("must have at least one animation frame");
        }

        this.currentFrame = data.frames.get(0);
    }

    @Override
    public @NotNull ItemStack update(long time, @NotNull ItemStack current) {
        if (data.frames.isEmpty()) {
            return current;
        }

        int nextFrameIndex = currentFrameIndex + 1;
        if (nextFrameIndex >= data.frames.size()) {
            nextFrameIndex = 0;
        }
        currentFrameIndex = nextFrameIndex;

        ItemAnimationFrame frame = data.frames.get(nextFrameIndex);
        currentFrame = frame;

        return frame.itemStack();
    }

    @Override
    public boolean hasUpdate(long time, @NotNull ItemStack current) {
        if (currentFrame == null) {
            return false;
        }

        long timeSinceLastUpdate = time - lastUpdateTime;
        if (timeSinceLastUpdate * MinecraftServer.TICK_MS >= currentFrame.delayTicks()) {
            lastUpdateTime = time;
            return true;
        }

        return false;
    }

    @Override
    public @NotNull ItemStack currentItem() {
        return currentFrame.itemStack();
    }

    @DataObject
    public record Data(@NotNull List<ItemAnimationFrame> frames) {
    }
}
