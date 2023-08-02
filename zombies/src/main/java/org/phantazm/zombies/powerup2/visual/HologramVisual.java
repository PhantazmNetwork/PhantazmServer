package org.phantazm.zombies.powerup2.visual;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.core.hologram.InstanceHologram;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Model("zombies.powerup.visual.hologram")
@Cache(false)
public class HologramVisual implements PowerupVisualComponent {
    private final Data data;

    @FactoryMethod
    public HologramVisual(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PowerupVisual apply(@NotNull ZombiesScene scene) {
        return new Visual(data, scene.instance());
    }

    @DataObject
    public record Data(@NotNull List<Component> lines,
                       long timeUntilBlink,
                       @NotNull List<Frame> blinkFrames,
                       double heightOffset) {
        @Default("heightOffset")
        public static @NotNull ConfigElement heightOffsetDefault() {
            return ConfigPrimitive.of(0.0);
        }
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
                long elapsed = (time - start) / MinecraftServer.TICK_MS;
                if (elapsed > data.timeUntilBlink && !data.blinkFrames.isEmpty()) {
                    Frame currentFrame = data.blinkFrames.get(0);
                    hologram.clear();
                    hologram.addAll(currentFrame.components());

                    this.blinking = true;
                    this.currentFrame = currentFrame;
                    this.lastFrameTime = time;
                }

                return;
            }

            long timeSinceLastFrame = (time - lastFrameTime) / MinecraftServer.TICK_MS;
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

            this.hologram = new InstanceHologram(new Vec(x, y + data.heightOffset, z), 0, Hologram.Alignment.CENTERED);
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
