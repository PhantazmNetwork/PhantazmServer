package com.github.phantazmnetwork.zombies.game.map.shop.gui;

import com.github.phantazmnetwork.core.gui.ItemUpdater;
import com.github.phantazmnetwork.core.item.ItemAnimationFrame;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Model("zombies.map.shop.gui.item_updater.animated")
@Cache(false)
public class AnimatedItemUpdater implements ItemUpdater {
    @ProcessorMethod
    public static ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<List<ItemAnimationFrame>> ITEM_ANIMATION_LIST_PROCESSOR =
                    ItemAnimationFrame.processor().listProcessor();

            @Override
            public Data dataFromElement(@NotNull ConfigElement element) throws ConfigProcessException {
                List<ItemAnimationFrame> frames =
                        ITEM_ANIMATION_LIST_PROCESSOR.dataFromElement(element.getElementOrThrow("frames"));
                return new Data(frames);
            }

            @Override
            public @NotNull ConfigElement elementFromData(Data data) throws ConfigProcessException {
                return ConfigNode.of("frames", ITEM_ANIMATION_LIST_PROCESSOR.elementFromData(data.frames));
            }
        };
    }

    private final Data data;

    private long lastUpdateTime;
    private ItemAnimationFrame currentFrame;
    private int currentFrameIndex;

    @FactoryMethod
    public AnimatedItemUpdater(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
        this.currentFrame = data.frames.isEmpty() ? null : data.frames.get(0);
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
        if (timeSinceLastUpdate * MinecraftServer.TICK_MS > currentFrame.delayTicks()) {
            lastUpdateTime = time;
        }
        return false;
    }

    @DataObject
    public record Data(@NotNull List<ItemAnimationFrame> frames) {
    }
}
