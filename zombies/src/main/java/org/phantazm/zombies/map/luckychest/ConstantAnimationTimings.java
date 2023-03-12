package org.phantazm.zombies.map.luckychest;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("zombies.map.lucky_chest.animation.timings.constant")
@Cache(false)
public class ConstantAnimationTimings implements AnimationTimings {
    private final Data data;

    private long lastAdvance;

    @FactoryMethod
    public ConstantAnimationTimings(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
        this.lastAdvance = -1;
    }

    @Override
    public void start(long time) {
        this.lastAdvance = time;
    }

    @Override
    public boolean shouldAdvance(long time) {
        long lastAdvance = this.lastAdvance;
        long tickDiff = (time - lastAdvance) / MinecraftServer.TICK_MS;
        if (tickDiff >= data.interval) {
            this.lastAdvance = time;
            return true;
        }

        return false;
    }

    @DataObject
    public record Data(int interval) {
    }
}
