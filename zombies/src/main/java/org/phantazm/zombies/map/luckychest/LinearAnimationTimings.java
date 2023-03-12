package org.phantazm.zombies.map.luckychest;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.lucky_chest.animation.timings.linear")
@Cache(false)
public class LinearAnimationTimings implements AnimationTimings {
    private final Data data;
    private final double slope;

    private long lastAdvance;

    @FactoryMethod
    public LinearAnimationTimings(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
        this.lastAdvance = -1;
        this.slope = (data.endInterval - data.startInterval) / (double)data.changeDuration;
    }

    @Override
    public void start(long time) {
        this.lastAdvance = time;
    }

    @Override
    public boolean shouldAdvance(long time) {
        long lastAdvance = this.lastAdvance;

        long tickDiff = (time - lastAdvance) / MinecraftServer.TICK_MS;
        if (tickDiff >= interval(tickDiff)) {
            this.lastAdvance = time;
            return true;
        }

        return false;
    }

    private int interval(long tickDiff) {
        return (int)Math.rint((slope * tickDiff) + data.startInterval);
    }

    @DataObject
    public record Data(int startInterval, int endInterval, int changeDuration) {
    }
}
