package com.github.phantazmnetwork.zombies.game.map.shop.display;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.commons.vector.VectorConfigProcessors;
import com.github.phantazmnetwork.core.item.ItemAnimationFrame;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessException;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
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
        super(data.animationFrames.isEmpty() ? ItemStack.AIR : data.animationFrames.get(0).itemStack(), data.offset);
        this.data = data;
    }

    @ProcessorMethod
    public static @NotNull ConfigProcessor<Data> processor() {
        return new ConfigProcessor<>() {
            private static final ConfigProcessor<Vec3D> VEC3D_PROCESSOR = VectorConfigProcessors.vec3D();
            private static final ConfigProcessor<List<ItemAnimationFrame>> ITEM_ANIMATION_LIST_PROCESSOR =
                    ItemAnimationFrame.processor().listProcessor();

            @Override
            public @NotNull Data dataFromElement(@NotNull ConfigElement node) throws ConfigProcessException {
                Vec3D offset = VEC3D_PROCESSOR.dataFromElement(node.getElementOrThrow("offset"));
                List<ItemAnimationFrame> animationFrames =
                        ITEM_ANIMATION_LIST_PROCESSOR.dataFromElement(node.getElementOrThrow("animationFrames"));
                int loops = node.getNumberOrThrow("loops").intValue();
                return new Data(offset, animationFrames, loops);
            }

            @Override
            public @NotNull ConfigElement elementFromData(@NotNull Data data) throws ConfigProcessException {
                return ConfigNode.of("offset", VEC3D_PROCESSOR.elementFromData(data.offset), "animationFrames",
                        ITEM_ANIMATION_LIST_PROCESSOR.elementFromData(data.animationFrames), "loops", data.loops);
            }
        };
    }

    @Override
    public void tick(long time) {
        if (data.animationFrames.isEmpty() || (data.loops >= 0 && loopCount > data.loops)) {
            return;
        }

        if (currentFrame == null) {
            frameIndex = 0;
            currentFrame = data.animationFrames.get(0);
            timeAtLastFrame = time;
            return;
        }

        if (timeAtLastFrame - time > (long)currentFrame.delayTicks() * MinecraftServer.TICK_MS) {
            if (++frameIndex >= data.animationFrames.size()) {
                frameIndex = 0;
                loopCount++;
            }

            currentFrame = data.animationFrames.get(frameIndex);
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
    public record Data(@NotNull Vec3D offset, @NotNull List<ItemAnimationFrame> animationFrames, int loops) {
    }
}
