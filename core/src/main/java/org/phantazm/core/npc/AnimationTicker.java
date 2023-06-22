package org.phantazm.core.npc;

import com.github.steanky.element.core.annotation.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

@Model("npc.entity.ticker.animation")
@Cache(false)
public class AnimationTicker implements EntityTicker {
    private final List<Frame> frames;

    private Frame current;
    private int currentIndex;
    private long lastFrameTime;

    @FactoryMethod
    public AnimationTicker(@NotNull @Child("frames") List<Frame> frames) {
        this.frames = frames;
        this.current = frames.isEmpty() ? null : frames.get(0);
        this.lastFrameTime = System.currentTimeMillis();
    }

    @Override
    public void accept(long time, @NotNull Entity entity) {
        Frame current = this.current;
        if (current == null) {
            return;
        }

        long ticksSinceLastFrame = (time - lastFrameTime) / MinecraftServer.TICK_MS;
        if (ticksSinceLastFrame >= current.data.ticks) {
            current.effect.accept(entity);

            int newIndex = (currentIndex + 1) % frames.size();

            this.current = frames.get(newIndex);
            currentIndex = newIndex;
            lastFrameTime = time;
        }
    }

    @Model("npc.entity.ticker.animation.frame")
    @Cache(false)
    public static class Frame {
        private final Data data;
        private final Consumer<? super Entity> effect;

        @FactoryMethod
        public Frame(@NotNull Data data, @NotNull @Child("effect") Consumer<? super Entity> effect) {
            this.data = data;
            this.effect = effect;
        }

        @DataObject
        public record Data(long ticks, @NotNull @ChildPath("effect") String effect) {
        }
    }

    @DataObject
    public record Data(@NotNull @ChildPath("frames") List<String> frames) {

    }
}
