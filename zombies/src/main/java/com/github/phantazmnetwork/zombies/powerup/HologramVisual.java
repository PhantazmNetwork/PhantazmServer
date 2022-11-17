package com.github.phantazmnetwork.zombies.powerup;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.core.hologram.Hologram;
import com.github.phantazmnetwork.core.hologram.InstanceHologram;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.powerup.visual.hologram")
public class HologramVisual implements Supplier<PowerupVisual> {
    private final Data data;
    private final Instance instance;

    @FactoryMethod
    public HologramVisual(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.instance") Instance instance) {
        this.data = Objects.requireNonNull(data, "data");
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    @Override
    public PowerupVisual get() {
        return new Visual(data, instance);
    }

    @DataObject
    public record Data(@NotNull List<Component> lines, long timeUntilBlink, @NotNull List<Frame> blinkFrames) {
    }

    public record Frame(@NotNull List<Component> components, long delay) {
    }

    private static class Visual implements PowerupVisual {
        private final Data data;
        private final Instance instance;

        private Hologram hologram;
        private long start;

        private boolean blinking;

        private long lastFrameTime;
        private int currentFrameIndex;
        private Frame currentFrame;

        private Visual(Data data, Instance instance) {
            this.data = data;
            this.instance = instance;
        }

        @Override
        public void tick(long time) {
            Hologram hologram = this.hologram;

            if (hologram == null) {
                return;
            }

            boolean blinking = this.blinking;
            long start = this.start;

            if (!blinking) {
                long elapsed = time - start;
                if (elapsed > data.timeUntilBlink && !data.blinkFrames.isEmpty()) {
                    Frame currentFrame = data.blinkFrames.get(0);
                    hologram.addAll(currentFrame.components());

                    this.blinking = true;
                    this.currentFrame = currentFrame;
                    this.lastFrameTime = time;
                }

                return;
            }

            long timeSinceLastFrame = time - lastFrameTime;
            Frame currentFrame = this.currentFrame;

            if (currentFrame != null && timeSinceLastFrame > currentFrame.delay()) {
                int nextFrameIndex = (++currentFrameIndex) % data.blinkFrames.size();
                this.currentFrame = currentFrame = data.blinkFrames.get(nextFrameIndex);
                this.lastFrameTime = time;

                hologram.clear();
                hologram.addAll(currentFrame.components());
            }
        }

        @Override
        public void spawn(double x, double y, double z) {
            Hologram hologram = this.hologram;
            if (hologram != null) {
                hologram.clear();
            }

            this.hologram = new InstanceHologram(Vec3D.of(x, y, z), 0, Hologram.Alignment.CENTERED);
            this.hologram.addAll(data.lines);
            this.hologram.setInstance(instance);
            this.start = System.currentTimeMillis();
        }

        @Override
        public void despawn() {
            Hologram hologram = this.hologram;
            if (hologram != null) {
                hologram.clear();
                this.hologram = null;
            }

            this.blinking = false;
            this.currentFrame = null;
            this.currentFrameIndex = 0;
        }
    }
}