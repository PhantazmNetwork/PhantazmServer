package com.github.phantazmnetwork.zombies.powerup;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.core.hologram.Hologram;
import com.github.phantazmnetwork.core.hologram.InstanceHologram;
import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.text.Component;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Model("zombies.powerup.visual.hologram")
@Cache(false)
public class HologramPowerupVisual implements PowerupVisual {
    private final Data data;
    private final Instance instance;

    private Hologram hologram;
    private long start;

    private boolean blinking;

    private long lastFrameTime;
    private int currentFrameIndex;
    private Frame currentFrame;

    @FactoryMethod
    public HologramPowerupVisual(@NotNull Data data,
            @NotNull @Dependency("zombies.dependency.map_object.instance") Instance instance) {
        this.data = Objects.requireNonNull(data, "data");
        this.instance = Objects.requireNonNull(instance, "instance");
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
                this.blinking = true;

                this.currentFrame = data.blinkFrames.get(0);
                this.lastFrameTime = time;
                hologram.addAll(currentFrame.components);
            }

            return;
        }

        long timeSinceLastFrame = time - lastFrameTime;
        Frame currentFrame = this.currentFrame;

        if (currentFrame != null && timeSinceLastFrame > currentFrame.delay) {
            hologram.clear();

            int nextFrameIndex = (++currentFrameIndex) % data.blinkFrames.size();
            this.currentFrame = currentFrame = data.blinkFrames.get(nextFrameIndex);
            this.lastFrameTime = time;

            hologram.addAll(currentFrame.components);
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

    @DataObject
    public record Data(@NotNull List<Component> lines, long timeUntilBlink, @NotNull List<Frame> blinkFrames) {
    }

    public record Frame(@NotNull List<Component> components, long delay) {
    }
}
