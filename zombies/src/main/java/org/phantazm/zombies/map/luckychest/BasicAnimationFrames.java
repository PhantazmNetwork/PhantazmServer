package org.phantazm.zombies.map.luckychest;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.lucky_chest.animation.frames.basic")
@Cache(false)
public class BasicAnimationFrames implements AnimationFrames {
    private final Data data;

    private int currentFrame;

    @FactoryMethod
    public BasicAnimationFrames(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
        this.currentFrame = -1;
    }

    @Override
    public @NotNull ItemStack next() {
        if (data.frames.isEmpty()) {
            return ItemStack.AIR;
        }

        int nextFrame = ++currentFrame;
        if (nextFrame >= data.frames.size()) {
            nextFrame = 0;
        }

        return data.frames.get(nextFrame);
    }

    @DataObject
    public record Data(@NotNull List<ItemStack> frames) {
    }
}
