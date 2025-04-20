package org.phantazm.core.item;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.Gui;
import org.phantazm.core.gui.ItemUpdater;

import java.util.List;
import java.util.Objects;

@Model("item.updating.animated")
@Cache(false)
public class AnimatedUpdatingItem implements ItemUpdater {
    private final Data data;

    private long updateTicks = 0;
    private ItemAnimationFrame currentFrame;
    private int currentFrameIndex;

    @FactoryMethod
    public AnimatedUpdatingItem(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
        if (data.frames.isEmpty()) {
            throw new IllegalArgumentException("must have at least one animation frame");
        }

        this.currentFrame = data.frames.get(0);
    }

    @Override
    public @NotNull ItemStack update(@NotNull Gui gui, long time, @NotNull ItemStack current, int slot) {
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
    public boolean hasUpdate(@NotNull Gui gui, long time, @NotNull ItemStack current, int slot) {
        if (currentFrame == null) {
            return false;
        }

        ++updateTicks;
        if (updateTicks >= currentFrame.delayTicks()) {
            updateTicks = 0;
            return true;
        }

        return false;
    }

    @DataObject
    public record Data(@NotNull List<ItemAnimationFrame> frames) {
    }
}
